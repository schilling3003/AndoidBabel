package com.schilling3003.relay.viewmodel

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.schilling3003.relay.domain.ImportResult
import com.schilling3003.relay.domain.Language
import com.schilling3003.relay.domain.ModelError
import com.schilling3003.relay.domain.ModelState
import com.schilling3003.relay.engines.ModelManager
import com.schilling3003.relay.engines.moonshine.ModelDownloadManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SetupViewModel(
    private val modelManager: ModelManager,
    private val downloadManager: ModelDownloadManager
) : ViewModel() {

    private val _modelState = mutableStateOf<ModelState>(modelManager.state.value)
    val modelState: State<ModelState> = _modelState

    private val _importError = mutableStateOf<ModelError?>(null)
    val importError: State<ModelError?> = _importError

    private val _shouldShowSetup = mutableStateOf(computeShouldShow())
    val shouldShowSetup: State<Boolean> = _shouldShowSetup

    private val _voiceModelsPresent = mutableStateOf(computeVoiceModelsPresent())
    val voiceModelsPresent: State<Boolean> = _voiceModelsPresent

    init {
        viewModelScope.launch {
            modelManager.state.collectLatest {
                _modelState.value = it
                _voiceModelsPresent.value = computeVoiceModelsPresent()
                when (it) {
                    // Stay on the setup screen after the Gemma model is ready so the
                    // user can see the voice-model download UI and tap "Ready to translate".
                    is ModelState.Missing -> _shouldShowSetup.value = true
                    is ModelState.Error -> _shouldShowSetup.value = true
                    is ModelState.Ready -> {
                        // Keep setup visible after Gemma is imported until the user taps
                        // "Ready to translate", so the voice-model card can be used.
                        if (_shouldShowSetup.value) {
                            _shouldShowSetup.value = true
                        }
                    }
                    else -> { /* preserve current visibility during import/validation */ }
                }
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
            _voiceModelsPresent.value = computeVoiceModelsPresent()
        }
    }

    fun removeModel() {
        viewModelScope.launch {
            modelManager.remove()
            _voiceModelsPresent.value = computeVoiceModelsPresent()
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

    fun refreshVoiceModels() {
        _voiceModelsPresent.value = computeVoiceModelsPresent()
    }

    private fun computeShouldShow(): Boolean {
        if (modelManager.state.value !is ModelState.Ready) return true
        return !computeVoiceModelsPresent()
    }

    private fun computeVoiceModelsPresent(): Boolean =
        downloadManager.requiredSpecs(Language.ENGLISH, Language.SPANISH)
            .all { downloadManager.isPresent(it) }

    class Factory(
        private val modelManager: ModelManager,
        private val downloadManager: ModelDownloadManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SetupViewModel(modelManager, downloadManager) as T
    }
}
