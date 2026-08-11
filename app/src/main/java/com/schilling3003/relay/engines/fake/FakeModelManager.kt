package com.schilling3003.relay.engines.fake

import android.net.Uri
import com.schilling3003.relay.domain.ImportResult
import com.schilling3003.relay.domain.ModelError
import com.schilling3003.relay.domain.ModelState
import com.schilling3003.relay.domain.ValidationResult
import com.schilling3003.relay.engines.ModelManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration.Companion.milliseconds

/**
 * Deterministic fake model manager used for screenshot tests and early UI
 * iterations. It simulates import/validation progress without touching real files.
 */
class FakeModelManager(
    private val modelSizeBytes: Long = 4_500_000_000L,
    private val importDurationMs: Long = 800L,
    initialState: ModelState = ModelState.Ready(
        path = "fake:///gemma-4-e2b-it.litertlm",
        modelId = "gemma-4-E2B-it",
        sizeBytes = 4_500_000_000L
    )
) : ModelManager {

    private val _state = MutableStateFlow<ModelState>(initialState)
    override val state: StateFlow<ModelState> = _state

    override suspend fun import(uri: Uri): ImportResult {
        _state.value = ModelState.Importing(
            bytesCopied = 0,
            totalBytes = modelSizeBytes,
            stage = "Copying model…"
        )
        val steps = 5
        repeat(steps) { i ->
            delay((importDurationMs / steps).milliseconds)
            _state.value = ModelState.Importing(
                bytesCopied = modelSizeBytes * (i + 1) / steps,
                totalBytes = modelSizeBytes,
                stage = if (i < steps - 2) "Copying model…" else "Validating model…"
            )
        }
        _state.value = ModelState.Ready(
            path = "fake:///gemma-4-e2b-it.litertlm",
            modelId = "gemma-4-E2B-it",
            sizeBytes = modelSizeBytes
        )
        return ImportResult.Success(_state.value as ModelState.Ready)
    }

    override suspend fun validate(): ValidationResult {
        return when (val s = _state.value) {
            is ModelState.Ready -> ValidationResult.Valid
            is ModelState.Error -> ValidationResult.Invalid(s.reason)
            else -> ValidationResult.Invalid(ModelError.Missing())
        }
    }

    override suspend fun remove() {
        _state.value = ModelState.Missing
    }

    fun setState(state: ModelState) {
        _state.value = state
    }
}
