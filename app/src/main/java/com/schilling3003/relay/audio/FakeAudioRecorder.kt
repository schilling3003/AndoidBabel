package com.schilling3003.relay.audio

import com.schilling3003.relay.domain.RecordedAudio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Fake microphone for deterministic UI and unit tests. It suspends on [start]
 * until [stop] is called, avoiding real-time looping and infinite-delay issues
 * under test dispatchers.
 */
class FakeAudioRecorder : AudioRecorder {

    private val _level = MutableStateFlow(0f)
    override val level: Flow<Float> = _level.asStateFlow()

    private val _elapsed = MutableStateFlow(0L)
    override val elapsedMillis: Flow<Long> = _elapsed.asStateFlow()

    @Volatile
    private var running = false

    private var startContinuation: Continuation<Unit>? = null

    override suspend fun start() {
        running = true
        _level.value = 0.5f
        _elapsed.value = 0L
        suspendCoroutine { continuation ->
            startContinuation = continuation
        }
    }

    override suspend fun stop(): RecordedAudio {
        running = false
        startContinuation?.resume(Unit)
        startContinuation = null
        return RecordedAudio(
            pcmBytes = ByteArray(16_000) { 0 },
            sampleRateHz = 16_000,
            channelCount = 1,
            encodingBits = 16
        )
    }

    override fun release() {
        running = false
        startContinuation?.resume(Unit)
        startContinuation = null
    }
}
