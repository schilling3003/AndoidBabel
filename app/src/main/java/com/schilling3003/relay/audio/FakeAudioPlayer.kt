package com.schilling3003.relay.audio

import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * Fake audio player used for testing and UI prototyping.
 */
class FakeAudioPlayer : AudioPlayer {
    override suspend fun play(audio: ByteArray, sampleRateHz: Int) {
        delay((audio.size / (2 * sampleRateHz) * 1000).coerceAtLeast(100).toLong().milliseconds)
    }

    override suspend fun stop() {}
    override fun release() {}
}
