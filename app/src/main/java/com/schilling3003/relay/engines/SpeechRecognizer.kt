package com.schilling3003.relay.engines

import com.schilling3003.relay.domain.EngineReadiness
import com.schilling3003.relay.domain.Language
import com.schilling3003.relay.domain.PartialTranscript
import com.schilling3003.relay.domain.RecordedAudio
import com.schilling3003.relay.domain.Transcript
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Abstracts on-device speech-to-text. Implementations may use Moonshine, native
 * Gemma audio, or any other STT engine. Callers must not assume a specific API.
 */
interface SpeechRecognizer {
    val readiness: StateFlow<EngineReadiness>

    /** A stream of provisional partial transcripts while audio is being captured. */
    fun partials(): Flow<PartialTranscript>

    /**
     * Transcribe a complete utterance. The implementation may resample and run
     * off the calling thread; callers provide a dedicated dispatcher.
     */
    suspend fun transcribe(audio: RecordedAudio, language: Language): Transcript

    /** Cancel any in-flight transcription. */
    suspend fun cancel()
}
