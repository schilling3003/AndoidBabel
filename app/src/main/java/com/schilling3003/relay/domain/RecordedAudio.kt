package com.schilling3003.relay.domain

/**
 * PCM audio captured by the microphone. Format is mono 16-bit PCM at 16 kHz
 * unless the recognizer advertises a different rate.
 */
data class RecordedAudio(
    val pcmBytes: ByteArray,
    val sampleRateHz: Int = 16_000,
    val channelCount: Int = 1,
    val encodingBits: Int = 16
) {
    val durationSeconds: Float
        get() = pcmBytes.size.toFloat() / (2 * channelCount * sampleRateHz)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RecordedAudio) return false
        return sampleRateHz == other.sampleRateHz &&
            channelCount == other.channelCount &&
            encodingBits == other.encodingBits &&
            pcmBytes.contentEquals(other.pcmBytes)
    }

    override fun hashCode(): Int {
        var result = pcmBytes.contentHashCode()
        result = 31 * result + sampleRateHz
        result = 31 * result + channelCount
        result = 31 * result + encodingBits
        return result
    }
}
