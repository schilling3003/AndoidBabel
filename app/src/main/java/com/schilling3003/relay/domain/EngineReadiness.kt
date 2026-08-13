package com.schilling3003.relay.domain

/**
 * Describes whether a voice/translation engine can be used right now.
 * UI text is resolved from [EngineStatus]; the sealed class stores the
 * machine-readable state and an optional human-readable detail.
 */
sealed class EngineReadiness {
    data object Uninitialized : EngineReadiness()
    data class Loading(val progress: Float = 0f, val message: String = "") : EngineReadiness()
    data class MissingAsset(val language: Language) : EngineReadiness()
    data class Error(val message: String) : EngineReadiness()
    data object Ready : EngineReadiness()

    val isReady: Boolean get() = this is Ready
}
