package com.schilling3003.relay.domain

/**
 * A completed translation with structured fields so the UI can distinguish the
 * source, the target rendering, and any parsing/metadata.
 */
data class Translation(
    val sourceText: String,
    val translatedText: String,
    val sourceLanguage: Language,
    val targetLanguage: Language,
    val raw: String? = null
)
