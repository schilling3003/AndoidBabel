package com.schilling3003.relay.domain

/**
 * High-level finite conversation state machine used by the UI and ViewModel.
 */
sealed class ConversationState {
    data object Setup : ConversationState()
    data class Warming(
        val overall: EngineReadiness,
        val sourceStt: EngineReadiness,
        val targetTts: EngineReadiness,
        val translator: EngineReadiness
    ) : ConversationState()
    data class Ready(val sourceLanguage: Language, val targetLanguage: Language) : ConversationState()
    data class Recording(
        val sourceLanguage: Language,
        val targetLanguage: Language,
        val elapsedMs: Long = 0L,
        val level: Float = 0f
    ) : ConversationState()
    data class Transcribing(
        val sourceLanguage: Language,
        val targetLanguage: Language,
        val partial: Transcript? = null
    ) : ConversationState()
    data class Translating(
        val sourceLanguage: Language,
        val targetLanguage: Language,
        val transcript: Transcript
    ) : ConversationState()
    data class Speaking(
        val sourceLanguage: Language,
        val targetLanguage: Language,
        val turn: ConversationTurn
    ) : ConversationState()
    data class Error(
        val previousState: ConversationState,
        val error: ConversationError
    ) : ConversationState()

    val isProcessing: Boolean
        get() = this is Recording || this is Transcribing || this is Translating || this is Speaking
}
