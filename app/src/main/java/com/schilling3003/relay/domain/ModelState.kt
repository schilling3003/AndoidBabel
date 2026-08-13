package com.schilling3003.relay.domain

/**
 * Immutable model asset state surfaced by [ModelManager].
 */
sealed class ModelState {
    data object Missing : ModelState()
    data class Importing(
        val bytesCopied: Long,
        val totalBytes: Long,
        val stage: String
    ) : ModelState()
    data class Validating(val message: String = "Validating model…") : ModelState()
    data class Ready(
        val path: String,
        val modelId: String?,
        val sizeBytes: Long
    ) : ModelState()
    data class Error(val reason: ModelError) : ModelState()
}

sealed class ModelError {
    abstract val userMessage: String

    data class Incompatible(override val userMessage: String = "Incompatible model") : ModelError()
    data class Corrupt(override val userMessage: String = "Model file is damaged") : ModelError()
    data class Storage(override val userMessage: String = "Not enough storage") : ModelError()
    data class CopyFailed(override val userMessage: String = "Could not save model") : ModelError()
    data class Missing(override val userMessage: String = "No model imported") : ModelError()
}

sealed class ImportResult {
    data class Success(val state: ModelState.Ready) : ImportResult()
    data class Failure(val error: ModelError) : ImportResult()
    data class Cancelled(val reason: String = "Import cancelled") : ImportResult()
}

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val error: ModelError) : ValidationResult()
}
