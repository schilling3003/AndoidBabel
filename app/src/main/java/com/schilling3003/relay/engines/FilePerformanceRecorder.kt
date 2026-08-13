package com.schilling3003.relay.engines

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.util.Log
import com.schilling3003.relay.BuildConfig
import com.schilling3003.relay.domain.BenchmarkReport
import com.schilling3003.relay.domain.DeviceProfile
import com.schilling3003.relay.domain.PipelineEvent
import com.schilling3003.relay.domain.StageSummary
import com.schilling3003.relay.domain.TimedEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Collections
import kotlin.math.ceil

/**
 * Records pipeline stage timestamps in memory and writes a JSON benchmark report
 * to the app's external files directory after each completed or cancelled turn.
 *
 * All file I/O happens off the UI thread. No spoken text or transcripts are logged.
 */
class FilePerformanceRecorder(private val context: Context) : PerformanceRecorder {

    private val events = Collections.synchronizedList(mutableListOf<TimedEvent>())
    private val json = Json { prettyPrint = true }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val TAG_PERF = "RelayPerf"
        const val TAG_BENCH = "RelayBenchmark"
    }

    override fun mark(event: PipelineEvent) {
        val now = SystemClock.elapsedRealtimeNanos()
        val timed = TimedEvent(event.stage, now, turnId = null)
        events.add(timed)
        Log.d(TAG_PERF, "${timed.stage} ns=${timed.timestampNs}")

        if (timed.stage in TERMINAL_STAGES) {
            scope.launch { writeReport() }
        }
    }

    override fun export(): BenchmarkReport = buildReport()

    override fun reset() {
        events.clear()
    }

    private fun writeReport() {
        try {
            val report = buildReport()
            val text = json.encodeToString(report)
            val baseDir = context.getExternalFilesDir(null)
                ?: context.filesDir
            val file = File(baseDir, "relay_benchmark_${System.currentTimeMillis()}.json")
            val latest = File(baseDir, "relay_benchmark_latest.json")
            file.writeText(text)
            latest.writeText(text)
            Log.i(TAG_BENCH, "Benchmark report written to ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG_BENCH, "Failed to write benchmark report", e)
        }
    }

    private fun buildReport(): BenchmarkReport {
        val snapshot = events.toList()
        val summaries = STAGE_METRICS.mapNotNull { (metric, pair) ->
            val durations = extractDurations(snapshot, pair.first, pair.second)
            if (durations.isEmpty()) null
            else StageSummary(
                stage = metric,
                p50Ms = percentile(durations, 0.50),
                p95Ms = percentile(durations, 0.95),
                maxMs = durations.maxOrNull() ?: 0L,
                sampleCount = durations.size
            )
        }

        val completedTurns = snapshot.count { it.stage == "tts_complete" }
        val cancelledTurns = snapshot.count { it.stage == "cancellation_completed" }

        return BenchmarkReport(
            commit = BuildConfig.GIT_COMMIT,
            buildType = BuildConfig.BUILD_TYPE,
            deviceProfile = buildDeviceProfile(),
            modelVersions = emptyMap(),
            pipelineImplementation = "staged",
            corpusId = "manual_user_test",
            isWarm = true,
            sampleCount = completedTurns + cancelledTurns,
            failures = cancelledTurns,
            events = snapshot,
            summaries = summaries
        )
    }

    private fun buildDeviceProfile(): DeviceProfile {
        val memoryInfo = ActivityManager.MemoryInfo()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        activityManager?.getMemoryInfo(memoryInfo)
        return DeviceProfile(
            model = Build.MODEL ?: "unknown",
            soc = Build.HARDWARE ?: "unknown",
            androidVersion = Build.VERSION.RELEASE ?: "unknown",
            freeMemoryBytes = memoryInfo.availMem,
            thermalState = null
        )
    }

    private fun extractDurations(events: List<TimedEvent>, startStage: String, endStage: String): List<Long> {
        val result = mutableListOf<Long>()
        var startNs: Long? = null
        for (event in events) {
            if (event.stage == startStage) {
                startNs = event.timestampNs
            } else if (event.stage == endStage && startNs != null) {
                val durationNs = event.timestampNs - startNs
                result.add(durationNs / 1_000_000L)
                startNs = null
            }
        }
        return result
    }

    private fun percentile(sortedInput: List<Long>, p: Double): Long {
        if (sortedInput.isEmpty()) return 0L
        val sorted = sortedInput.sorted()
        val index = ceil(sorted.size * p).toInt() - 1
        return sorted[index.coerceIn(0, sorted.lastIndex)]
    }
}

private val TERMINAL_STAGES = setOf("tts_complete", "cancellation_completed")

private val STAGE_METRICS = linkedMapOf(
    "press_to_feedback_ms" to ("press_received" to "recording_feedback_rendered"),
    "stt_latency_ms" to ("stt_started" to "stt_final"),
    "translation_latency_ms" to ("translation_queued" to "translation_final"),
    "tts_latency_ms" to ("tts_queued" to "tts_complete"),
    "stop_to_translation_ms" to ("recorder_stopped" to "translation_final"),
    "stop_to_tts_ms" to ("recorder_stopped" to "tts_complete"),
    "total_pipeline_ms" to ("press_received" to "tts_complete")
)
