package com.schilling3003.relay.viewmodel

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.schilling3003.relay.domain.EngineReadiness
import com.schilling3003.relay.domain.ImportResult
import com.schilling3003.relay.domain.ModelError
import com.schilling3003.relay.domain.ModelState
import com.schilling3003.relay.engines.ModelManager
import com.schilling3003.relay.engines.TranslationEngine
import com.schilling3003.relay.engines.moonshine.ModelDownloadManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SetupViewModel(
    private val modelManager: ModelManager,
    private val translationEngine: TranslationEngine,
    private val downloadManager: ModelDownloadManager
) : ViewModel() {

    private val _modelState = mutableStateOf<ModelState>(modelManager.state.value)
    val modelState: State<ModelState> = _modelState

    private val _engineReadiness = mutableStateOf<EngineReadiness>(translationEngine.readiness.value)
    val engineReadiness: State<EngineReadiness> = _engineReadiness

    private val _importError = mutableStateOf<ModelError?>(null)
    val importError: State<ModelError?> = _importError

    private val _shouldShowSetup = mutableStateOf(computeShouldShow())
    val shouldShowSetup: State<Boolean> = _shouldShowSetup

    init {
        viewModelScope.launch {
            modelManager.state.collectLatest {
                _modelState.value = it
                when (it) {
                    is ModelState.Missing -> _shouldShowSetup.value = true
                    is ModelState.Error -> _shouldShowSetup.value = true
                    is ModelState.Ready -> {
                        // Keep setup visible after Gemma is imported until the user taps
                        // "Ready to translate", so optional voice-model downloads can run.
                        if (_shouldShowSetup.value) {
                            _shouldShowSetup.value = true
                        }
                    }
                    else -> { /* preserve current visibility during import/validation */ }
                }
            }
        }
        viewModelScope.launch {
            translationEngine.readiness.collectLatest {
                _engineReadiness.value = it
            }
        }
    }

    fun importModel(uri: Uri) {
        viewModelScope.launch {
            _importError.value = null
            when (val result = modelManager.import(uri)) {
                is ImportResult.Success -> _modelState.value = result.state
                is ImportResult.Failure -> _importError.value = result.error
                is ImportResult.Cancelled -> _importError.value = ModelError.CopyFailed(result.reason)
            }
        }
    }

    fun removeModel() {
        viewModelScope.launch {
            modelManager.remove()
        }
    }

    fun dismissImportError() {
        _importError.value = null
    }

    fun dismissSetup() {
        if (_modelState.value is ModelState.Ready) {
            _shouldShowSetup.value = false
        }
    }

    private fun computeShouldShow(): Boolean {
        return modelManager.state.value !is ModelState.Ready
    }

    class Factory(
        private val modelManager: ModelManager,
        private val translationEngine: TranslationEngine,
        private val downloadManager: ModelDownloadManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SetupViewModel(modelManager, translationEngine, downloadManager) as T
    }
}
