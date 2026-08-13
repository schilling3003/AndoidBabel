package com.schilling3003.relay.engines

import com.schilling3003.relay.domain.EngineReadiness
import com.schilling3003.relay.domain.Language
import com.schilling3003.relay.domain.Translation
import kotlinx.coroutines.flow.StateFlow

/**
 * Abstracts on-device text translation. v1 uses a Gemma `.litertlm` model via
 * LiteRT-LM. The engine is responsible for structured output parsing and recovery.
 */
interface TranslationEngine {
    val readiness: StateFlow<EngineReadiness>

    /** Translate [text] from [source] to [target]. */
    suspend fun translate(text: String, source: Language, target: Language): Translation

    /** Cancel any in-flight translation request. */
    suspend fun cancel()
}
