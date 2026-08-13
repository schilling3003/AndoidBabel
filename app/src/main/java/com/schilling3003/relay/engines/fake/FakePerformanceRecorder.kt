package com.schilling3003.relay.engines.fake

import com.schilling3003.relay.domain.BenchmarkReport
import com.schilling3003.relay.domain.DeviceProfile
import com.schilling3003.relay.domain.PipelineEvent
import com.schilling3003.relay.domain.StageSummary
import com.schilling3003.relay.domain.TimedEvent
import com.schilling3003.relay.engines.PerformanceRecorder
import java.util.Collections

/**
 * In-memory performance recorder. Safe for unit tests; the real recorder writes
 * to disk and uses monotonic clocks.
 */
class FakePerformanceRecorder : PerformanceRecorder {

    private val events = Collections.synchronizedList(mutableListOf<TimedEvent>())

    override fun mark(event: PipelineEvent) {
        events.add(TimedEvent(event.stage, System.nanoTime(), turnId = null))
    }

    override fun export(): BenchmarkReport = BenchmarkReport(
        commit = "fake-commit",
        buildType = "fake",
        deviceProfile = DeviceProfile("unknown", "unknown", "unknown"),
        modelVersions = emptyMap(),
        pipelineImplementation = "fake",
        corpusId = "fake",
        isWarm = true,
        sampleCount = events.size,
        failures = 0,
        events = events.toList(),
        summaries = listOf(StageSummary("total", 0, 0, 0, events.size))
    )

    override fun reset() {
        events.clear()
    }
}
