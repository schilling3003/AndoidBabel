package com.schilling3003.relay.domain

import java.util.Locale

/**
 * A supported conversation language. v1 supports six languages chosen for travel
 * coverage and script diversity. Direction, script, and display name are intrinsic
 * to the enum so the UI never special-cases by string.
 */
enum class Language(
    val code: String,
    val displayName: String,
    val localName: String,
    val direction: LayoutDirection,
    val scriptFamily: ScriptFamily
) {
    ENGLISH("en", "English", "English", LayoutDirection.LTR, ScriptFamily.LATIN),
    ARABIC("ar", "Arabic", "العربية", LayoutDirection.RTL, ScriptFamily.ARABIC),
    SPANISH("es", "Spanish", "Español", LayoutDirection.LTR, ScriptFamily.LATIN),
    JAPANESE("ja", "Japanese", "日本語", LayoutDirection.LTR, ScriptFamily.CJK),
    MANDARIN("zh", "Mandarin", "中文", LayoutDirection.LTR, ScriptFamily.CJK),
    KOREAN("ko", "Korean", "한국어", LayoutDirection.LTR, ScriptFamily.HANGUL);

    val locale: Locale get() = Locale.forLanguageTag(code)

    fun displayLabel(withLocal: Boolean = true): String =
        if (withLocal) "$displayName · $localName" else displayName

    companion object {
        fun fromCode(code: String): Language =
            entries.find { it.code == code.lowercase() } ?: ENGLISH

        val defaults: Pair<Language, Language> = ENGLISH to SPANISH
    }
}

enum class LayoutDirection { LTR, RTL }
enum class ScriptFamily { LATIN, ARABIC, CJK, HANGUL }
