package com.schilling3003.relay.domain

/**
 * A recognized or edited speech transcript.
 */
data class Transcript(
    val text: String,
    val isFinal: Boolean = true,
    val confidence: Float? = null
)

/**
 * A partial STT result that may arrive while the user is still speaking.
 */
data class PartialTranscript(
    val text: String,
    val isStable: Boolean = false
)
