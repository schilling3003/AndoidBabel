package com.schilling3003.relay.audio

import com.schilling3003.relay.domain.RecordedAudio
import kotlinx.coroutines.flow.Flow

/**
 * Captures microphone audio on a background thread and emits volume levels.
 */
interface AudioRecorder {
    val level: Flow<Float>
    val elapsedMillis: Flow<Long>

    suspend fun start()
    suspend fun stop(): RecordedAudio
    fun release()
}
