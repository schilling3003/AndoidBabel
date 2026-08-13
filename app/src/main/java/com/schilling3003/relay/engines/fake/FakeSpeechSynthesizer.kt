package com.schilling3003.relay.engines.fake

import com.schilling3003.relay.domain.EngineReadiness
import com.schilling3003.relay.domain.Language
import com.schilling3003.relay.engines.SpeechSynthesizer
import com.schilling3003.relay.engines.SynthesisProgress
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration.Companion.milliseconds

/**
 * Deterministic fake TTS that advances through the synthesis progress stages.
 * It does not play real audio; it is sufficient for UI state and tests.
 */
class FakeSpeechSynthesizer(
    private val speakDurationMs: Long = 700L
) : SpeechSynthesizer {

    private val _readiness = MutableStateFlow<EngineReadiness>(EngineReadiness.Ready)
    override val readiness: StateFlow<EngineReadiness> = _readiness

    private val _progress = MutableStateFlow<SynthesisProgress>(SynthesisProgress.Completed)
    override fun progress(): Flow<SynthesisProgress> = _progress.asStateFlow()

    @Volatile
    private var cancelled = false

    override suspend fun speak(text: String, language: Language) {
        cancelled = false
        _progress.value = SynthesisProgress.Started
        val chunkCount = 4
        repeat(chunkCount) { i ->
            if (cancelled) return
            delay((speakDurationMs / chunkCount).milliseconds)
            _progress.value = SynthesisProgress.Playing((i + 1) / chunkCount.toFloat())
        }
        if (!cancelled) _progress.value = SynthesisProgress.Completed
    }

    override suspend fun stop() {
        cancelled = true
        _progress.value = SynthesisProgress.Completed
    }

    fun setReady(isReady: Boolean) {
        _readiness.value = if (isReady) EngineReadiness.Ready else EngineReadiness.MissingAsset(Language.ENGLISH)
    }
}
