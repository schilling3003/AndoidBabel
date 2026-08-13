package com.schilling3003.relay.engines

import com.schilling3003.relay.domain.EngineReadiness
import com.schilling3003.relay.domain.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Abstracts on-device text-to-speech. Implementations must emit progress and
 * respect cancellation.
 */
interface SpeechSynthesizer {
    val readiness: StateFlow<EngineReadiness>

    /** Stream of progress events from 0..1 and completion/error. */
    fun progress(): Flow<SynthesisProgress>

    /** Speak [text] in [language]. Suspends until finished or cancelled. */
    suspend fun speak(text: String, language: Language)

    /** Stop playback immediately. */
    suspend fun stop()
}

sealed class SynthesisProgress {
    data object Started : SynthesisProgress()
    data class ChunkReady(val index: Int) : SynthesisProgress()
    data class Playing(val fraction: Float) : SynthesisProgress()
    data object Completed : SynthesisProgress()
    data class Error(val message: String) : SynthesisProgress()
}
