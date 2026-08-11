package com.schilling3003.relay.domain

import java.util.UUID

/**
 * One completed turn in the conversation. The transcript is the source-language
 * text as recognized (or corrected); the translation is the target-language result.
 */
data class ConversationTurn(
    val id: String = UUID.randomUUID().toString(),
    val sourceLanguage: Language,
    val targetLanguage: Language,
    val transcript: Transcript,
    val translation: Translation?,
    val wasPlayed: Boolean = false,
    val error: ConversationError? = null
)

sealed class ConversationError {
    abstract val message: String
    data class Transcription(override val message: String) : ConversationError()
    data class Translation(override val message: String) : ConversationError()
    data class Synthesis(override val message: String) : ConversationError()
    data class Cancelled(override val message: String) : ConversationError()
}
