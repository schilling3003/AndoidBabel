package com.schilling3003.relay.audio

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.schilling3003.relay.domain.RecordedAudio
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Records mono 16-bit PCM at 16 kHz using [AudioRecord]. Callers must hold
 * [Manifest.permission.RECORD_AUDIO]; this class checks it and throws if absent.
 *
 * [start] suspends until [stop] is called, matching the contract used by
 * [com.schilling3003.relay.viewmodel.ConversationViewModel]. The actual capture
 * runs on a dedicated IO coroutine so [stop] can collect the recorded bytes.
 */
class RealAudioRecorder(private val context: android.content.Context) : AudioRecorder {

    private val _level = MutableStateFlow(0f)
    override val level: StateFlow<Float> = _level.asStateFlow()

    private val _elapsedMillis = MutableStateFlow(0L)
    override val elapsedMillis: StateFlow<Long> = _elapsedMillis.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var recorder: AudioRecord? = null
    private var recordJob: Job? = null
    private var completion: CompletableDeferred<RecordedAudio>? = null

    private val sampleRate = 16_000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val minBufferSize: Int by lazy {
        AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            .coerceAtLeast(sampleRate / 5) // ~0.1s to keep cancellation responsive
    }

    override suspend fun start() = withContext(Dispatchers.IO) {
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            throw SecurityException("RECORD_AUDIO permission not granted")
        }

        val deferred = CompletableDeferred<RecordedAudio>()
        completion = deferred

        recordJob = scope.launch {
            val output = ByteArrayOutputStream()
            var record: AudioRecord? = null
            try {
                record = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    minBufferSize
                )
                if (record.state != AudioRecord.STATE_INITIALIZED) {
                    throw IllegalStateException("AudioRecord could not be initialized")
                }
                recorder = record

                val buffer = ShortArray(minBufferSize / 2)
                val startTime = System.currentTimeMillis()
                record.startRecording()

                while (isActive) {
                    val read = record.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        output.writeShorts(buffer, read)
                        updateLevel(buffer, read)
                        _elapsedMillis.value = System.currentTimeMillis() - startTime
                    }
                }
                deferred.complete(RecordedAudio(output.toByteArray(), sampleRate))
            } catch (e: Throwable) {
                deferred.completeExceptionally(e)
            } finally {
                try {
                    record?.stop()
                } catch (_: IllegalStateException) {
                }
                record?.release()
                recorder = null
            }
        }

        try {
            deferred.await()
        } catch (e: kotlinx.coroutines.CancellationException) {
            // Expected when the conversation view model cancels the recording job.
            recordJob?.cancel()
            throw e
        }

        Unit
    }

    override suspend fun stop(): RecordedAudio = withContext(Dispatchers.IO) {
        recordJob?.cancel()
        val result = try {
            completion?.await() ?: RecordedAudio(ByteArray(0), sampleRate)
        } catch (_: Exception) {
            RecordedAudio(ByteArray(0), sampleRate)
        } finally {
            completion = null
            recordJob = null
            _level.value = 0f
            _elapsedMillis.value = 0L
        }
        result
    }

    override fun release() {
        scope.cancel()
        recorder?.release()
        recorder = null
        recordJob?.cancel()
        recordJob = null
        completion?.cancel()
        completion = null
    }

    private fun ByteArrayOutputStream.writeShorts(shorts: ShortArray, length: Int) {
        val bb = ByteBuffer.allocate(length * 2).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until length) {
            bb.putShort(shorts[i])
        }
        write(bb.array())
    }

    private fun updateLevel(buffer: ShortArray, length: Int) {
        if (length == 0) return
        var sum = 0L
        for (i in 0 until length) {
            val v = buffer[i].toInt()
            sum += v * v
        }
        val rms = kotlin.math.sqrt(sum.toDouble() / length).toFloat()
        _level.value = (rms / Short.MAX_VALUE).coerceIn(0f, 1f)
    }
}
