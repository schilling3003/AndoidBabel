package com.schilling3003.relay.engines.fake

import com.schilling3003.relay.domain.EngineReadiness
import com.schilling3003.relay.domain.Language
import com.schilling3003.relay.domain.PartialTranscript
import com.schilling3003.relay.domain.RecordedAudio
import com.schilling3003.relay.domain.Transcript
import com.schilling3003.relay.engines.SpeechRecognizer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.milliseconds

/**
 * Deterministic fake STT used for UI prototyping and screenshot tests.
 * It never loads a model and returns a predictable transcript based on the
 * requested language so all UX states can be exercised without a real model.
 */
class FakeSpeechRecognizer(
    private val delayMs: Long = 400L,
    private val emitPartials: Boolean = true
) : SpeechRecognizer {

    private val _readiness = MutableStateFlow<EngineReadiness>(EngineReadiness.Ready)
    override val readiness: StateFlow<EngineReadiness> = _readiness

    override fun partials(): Flow<PartialTranscript> = if (emitPartials) flow {
        emit(PartialTranscript("…", isStable = false))
    } else emptyFlow()

    override suspend fun transcribe(audio: RecordedAudio, language: Language): Transcript {
        if (delayMs > 0) delay(delayMs.milliseconds)
        return Transcript(
            text = sampleUttterance(language),
            isFinal = true,
            confidence = 0.95f
        )
    }

    override suspend fun cancel() {
        _readiness.value = EngineReadiness.Ready
    }

    fun setReady(isReady: Boolean) {
        _readiness.value = if (isReady) EngineReadiness.Ready else EngineReadiness.MissingAsset(Language.ENGLISH)
    }

    private fun sampleUttterance(language: Language): String = when (language) {
        Language.ENGLISH -> "Where is the nearest hospital?"
        Language.ARABIC -> "أين أقرب مستشفى؟"
        Language.SPANISH -> "¿Dónde está el hospital más cercano?"
        Language.JAPANESE -> "最寄りの病院はどこですか？"
        Language.MANDARIN -> "最近的医院在哪里？"
        Language.KOREAN -> "가장 가까운 병원이 어디예요?"
        Language.VIETNAMESE -> "Bệnh viện gần nhất ở đâu?"
        Language.UKRAINIAN -> "Де найближча лікарня?"
        else -> "Where is the nearest hospital?"
    }
}
