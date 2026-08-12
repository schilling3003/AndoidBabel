package com.schilling3003.relay.domain

import java.util.Locale

/**
 * A supported conversation language. Moonshine STT only supports a subset, so
 * each entry declares which capabilities it has. Direction, script, and display
 * name are intrinsic to the enum so the UI never special-cases by string.
 */
enum class Language(
    val code: String,
    val displayName: String,
    val localName: String,
    val direction: LayoutDirection,
    val scriptFamily: ScriptFamily,
    val supportsStt: Boolean,
    val supportsTts: Boolean
) {
    ENGLISH("en", "English", "English", LayoutDirection.LTR, ScriptFamily.LATIN, supportsStt = true, supportsTts = true),
    ARABIC("ar", "Arabic", "العربية", LayoutDirection.RTL, ScriptFamily.ARABIC, supportsStt = true, supportsTts = true),
    SPANISH("es", "Spanish", "Español", LayoutDirection.LTR, ScriptFamily.LATIN, supportsStt = true, supportsTts = true),
    JAPANESE("ja", "Japanese", "日本語", LayoutDirection.LTR, ScriptFamily.CJK, supportsStt = true, supportsTts = true),
    MANDARIN("zh", "Mandarin", "中文", LayoutDirection.LTR, ScriptFamily.CJK, supportsStt = true, supportsTts = true),
    KOREAN("ko", "Korean", "한국어", LayoutDirection.LTR, ScriptFamily.HANGUL, supportsStt = true, supportsTts = true),

    VIETNAMESE("vi", "Vietnamese", "Tiếng Việt", LayoutDirection.LTR, ScriptFamily.LATIN, supportsStt = true, supportsTts = true),
    UKRAINIAN("uk", "Ukrainian", "Українська", LayoutDirection.LTR, ScriptFamily.CYRILLIC, supportsStt = true, supportsTts = true),

    GERMAN("de", "German", "Deutsch", LayoutDirection.LTR, ScriptFamily.LATIN, supportsStt = false, supportsTts = true),
    FRENCH("fr", "French", "Français", LayoutDirection.LTR, ScriptFamily.LATIN, supportsStt = false, supportsTts = true),
    HINDI("hi", "Hindi", "हिन्दी", LayoutDirection.LTR, ScriptFamily.DEVANAGARI, supportsStt = false, supportsTts = true),
    ITALIAN("it", "Italian", "Italiano", LayoutDirection.LTR, ScriptFamily.LATIN, supportsStt = false, supportsTts = true),
    DUTCH("nl", "Dutch", "Nederlands", LayoutDirection.LTR, ScriptFamily.LATIN, supportsStt = false, supportsTts = true),
    PORTUGUESE("pt", "Portuguese", "Português", LayoutDirection.LTR, ScriptFamily.LATIN, supportsStt = false, supportsTts = true),
    RUSSIAN("ru", "Russian", "Русский", LayoutDirection.LTR, ScriptFamily.CYRILLIC, supportsStt = false, supportsTts = true),
    TURKISH("tr", "Turkish", "Türkçe", LayoutDirection.LTR, ScriptFamily.LATIN, supportsStt = false, supportsTts = true);

    val locale: Locale get() = Locale.forLanguageTag(code)

    fun displayLabel(withLocal: Boolean = true): String =
        if (withLocal) "$displayName · $localName" else displayName

    companion object {
        fun fromCode(code: String): Language =
            entries.find { it.code == code.lowercase() } ?: ENGLISH

        val defaults: Pair<Language, Language> = ENGLISH to SPANISH

        val sttLanguages: List<Language> = entries.filter { it.supportsStt }
        val ttsLanguages: List<Language> = entries.filter { it.supportsTts }
    }
}

enum class LayoutDirection { LTR, RTL }
enum class ScriptFamily { LATIN, ARABIC, CJK, HANGUL, CYRILLIC, DEVANAGARI }
