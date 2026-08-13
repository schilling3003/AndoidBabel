package com.schilling3003.relay.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageTest {

    @Test
    fun `all supported languages are reachable`() {
        val codes = listOf(
            "en", "ar", "es", "ja", "zh", "ko",
            "vi", "uk", "de", "fr", "hi", "it", "nl", "pt", "ru", "tr"
        )
        codes.forEach { code ->
            assertFalse(Language.fromCode(code).displayName.isBlank())
        }
    }

    @Test
    fun `stt and tts flags match moonshine capabilities`() {
        // These are the languages moonshine-voice 0.1.1 supports for both STT and TTS.
        val sttOnly = setOf(Language.VIETNAMESE, Language.UKRAINIAN)
        val ttsOnly = setOf(
            Language.GERMAN, Language.FRENCH, Language.HINDI, Language.ITALIAN,
            Language.DUTCH, Language.PORTUGUESE, Language.RUSSIAN, Language.TURKISH
        )
        sttOnly.forEach {
            assertTrue(it.supportsStt)
            assertTrue(it.supportsTts)
        }
        ttsOnly.forEach {
            assertFalse(it.supportsStt)
            assertTrue(it.supportsTts)
        }
    }

    @Test
    fun `arabic is RTL`() {
        assertEquals(LayoutDirection.RTL, Language.ARABIC.direction)
    }

    @Test
    fun `all non-Arabic languages are LTR`() {
        Language.entries.filter { it != Language.ARABIC }.forEach {
            assertEquals(LayoutDirection.LTR, it.direction)
        }
    }

    @Test
    fun `default pair is English Spanish`() {
        assertEquals(Language.ENGLISH to Language.SPANISH, Language.defaults)
    }

    @Test
    fun `fromCode is case insensitive`() {
        assertEquals(Language.ENGLISH, Language.fromCode("EN"))
        assertEquals(Language.ARABIC, Language.fromCode("Ar"))
    }

    @Test
    fun `unknown code falls back to English`() {
        assertEquals(Language.ENGLISH, Language.fromCode("xx"))
    }

    @Test
    fun `display label contains native name`() {
        assert(Language.JAPANESE.displayLabel().contains("日本語"))
        assert(Language.KOREAN.displayLabel().contains("한국어"))
    }
}
