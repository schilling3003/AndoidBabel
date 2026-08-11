package com.schilling3003.relay.engines.fake

import com.schilling3003.relay.domain.EngineReadiness
import com.schilling3003.relay.domain.Language
import com.schilling3003.relay.domain.Translation
import com.schilling3003.relay.engines.TranslationEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration.Companion.milliseconds

/**
 * Deterministic fake translation engine. Returns a predictable translation for
 * the sample utterance in each supported language pair, allowing screenshot and
 * state-machine tests without a Gemma model.
 */
class FakeTranslationEngine(
    private val delayMs: Long = 600L
) : TranslationEngine {

    private val _readiness = MutableStateFlow<EngineReadiness>(EngineReadiness.Ready)
    override val readiness: StateFlow<EngineReadiness> = _readiness

    override suspend fun translate(text: String, source: Language, target: Language): Translation {
        if (delayMs > 0) delay(delayMs.milliseconds)
        return Translation(
            sourceText = text,
            translatedText = translateSample(text, source, target),
            sourceLanguage = source,
            targetLanguage = target
        )
    }

    override suspend fun cancel() {
        _readiness.value = EngineReadiness.Ready
    }

    fun setReady(isReady: Boolean) {
        _readiness.value = if (isReady) EngineReadiness.Ready else EngineReadiness.Loading(message = "Warming Gemma…")
    }

    private fun translateSample(text: String, source: Language, target: Language): String {
        if (source == target) return text
        return when (source to target) {
            Language.ENGLISH to Language.SPANISH -> "¿Dónde está el hospital más cercano?"
            Language.SPANISH to Language.ENGLISH -> "Where is the nearest hospital?"
            Language.ENGLISH to Language.ARABIC -> "أين أقرب مستشفى؟"
            Language.ARABIC to Language.ENGLISH -> "Where is the nearest hospital?"
            Language.ENGLISH to Language.JAPANESE -> "最寄りの病院はどこですか？"
            Language.JAPANESE to Language.ENGLISH -> "Where is the nearest hospital?"
            Language.ENGLISH to Language.MANDARIN -> "最近的医院在哪里？"
            Language.MANDARIN to Language.ENGLISH -> "Where is the nearest hospital?"
            Language.ENGLISH to Language.KOREAN -> "가장 가까운 병원이 어디예요?"
            Language.KOREAN to Language.ENGLISH -> "Where is the nearest hospital?"
            else -> "[${target.localName}] ${text.take(60)}"
        }
    }
}
