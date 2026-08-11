package com.schilling3003.relay.viewmodel

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.schilling3003.relay.domain.ImportResult
import com.schilling3003.relay.domain.ModelError
import com.schilling3003.relay.domain.ModelState
import com.schilling3003.relay.engines.ModelManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SetupViewModel(private val modelManager: ModelManager) : ViewModel() {

    private val _modelState = mutableStateOf<ModelState>(ModelState.Missing)
    val modelState: State<ModelState> = _modelState

    private val _importError = mutableStateOf<ModelError?>(null)
    val importError: State<ModelError?> = _importError

    private val _shouldShowSetup = mutableStateOf(true)
    val shouldShowSetup: State<Boolean> = _shouldShowSetup

    init {
        viewModelScope.launch {
            modelManager.state.collectLatest { _modelState.value = it }
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
        viewModelScope.launch { modelManager.remove() }
    }

    fun dismissImportError() {
        _importError.value = null
    }

    fun dismissSetup() {
        if (_modelState.value is ModelState.Ready) {
            _shouldShowSetup.value = false
        }
    }

    class Factory(private val modelManager: ModelManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SetupViewModel(modelManager) as T
    }
}
