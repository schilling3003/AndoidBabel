package com.schilling3003.relay.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.ceil

/**
 * Plays 16-bit mono PCM through [AudioTrack].
 */
class AudioTrackAudioPlayer : AudioPlayer {

    private var audioTrack: AudioTrack? = null

    override suspend fun play(audio: ByteArray, sampleRateHz: Int) = withContext(Dispatchers.Default) {
        stop()

        val minBuffer = AudioTrack.getMinBufferSize(
            sampleRateHz,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = minBuffer.coerceAtLeast(audio.size).coerceAtMost(MAX_STATIC_BUFFER)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRateHz)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack = track
        track.play()

        val frameSize = 2 // mono 16-bit
        val chunkFrames = 1024
        val chunkBytes = chunkFrames * frameSize
        var offset = 0
        while (offset < audio.size && isActive) {
            val written = track.write(audio, offset, (audio.size - offset).coerceAtMost(chunkBytes))
            if (written < 0) break
            offset += written
        }

        // Wait for playback to finish.
        val durationMs = (audio.size / (frameSize * sampleRateHz.toFloat())) * 1000
        val startMs = System.currentTimeMillis()
        while (isActive && (track.playbackHeadPosition < audio.size / frameSize)) {
            if (System.currentTimeMillis() - startMs > durationMs + 500) break
            delay(20)
        }
    }

    override suspend fun stop() = withContext(Dispatchers.Default) {
        audioTrack?.let { track ->
            try {
                track.stop()
            } catch (_: IllegalStateException) {
            }
            track.release()
        }
        audioTrack = null
    }

    override fun release() {
        audioTrack?.release()
        audioTrack = null
    }

    companion object {
        // AudioTrack static mode has a hard size limit; stream for longer audio.
        private const val MAX_STATIC_BUFFER = 1_000_000
    }
}
