package com.schilling3003.relay.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.schilling3003.relay.audio.AudioPlayer
import com.schilling3003.relay.audio.AudioRecorder
import com.schilling3003.relay.domain.ConversationError
import com.schilling3003.relay.domain.ConversationState
import com.schilling3003.relay.domain.ConversationTurn
import com.schilling3003.relay.domain.EngineReadiness
import com.schilling3003.relay.domain.Language
import com.schilling3003.relay.domain.ModelState
import com.schilling3003.relay.domain.PipelineEvent
import com.schilling3003.relay.domain.Translation
import com.schilling3003.relay.engines.ModelManager
import com.schilling3003.relay.engines.PerformanceRecorder
import com.schilling3003.relay.engines.SpeechRecognizer
import com.schilling3003.relay.engines.SpeechSynthesizer
import com.schilling3003.relay.engines.TranslationEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Central conversation coordinator. Owns the finite state machine, drives the
 * audio/translation pipeline, and exposes stable Compose state.
 */
class ConversationViewModel(
    private val modelManager: ModelManager,
    private val speechRecognizer: SpeechRecognizer,
    private val translationEngine: TranslationEngine,
    private val speechSynthesizer: SpeechSynthesizer,
    private val audioRecorder: AudioRecorder,
    private val audioPlayer: AudioPlayer,
    private val performanceRecorder: PerformanceRecorder
) : ViewModel() {

    private val _state = MutableStateFlow<ConversationState>(ConversationState.Setup)
    val state: StateFlow<ConversationState> = _state.asStateFlow()

    private val _sourceLanguage = mutableStateOf(Language.ENGLISH)
    val sourceLanguage: State<Language> = _sourceLanguage

    private val _targetLanguage = mutableStateOf(Language.SPANISH)
    val targetLanguage: State<Language> = _targetLanguage

    private val _preferredSource = mutableStateOf(Language.ENGLISH)
    private val _preferredTarget = mutableStateOf(Language.SPANISH)

    private val _turns = mutableStateListOf<ConversationTurn>()
    val turns: List<ConversationTurn> = _turns

    private val _readiness = mutableStateOf<EngineReadiness>(EngineReadiness.Uninitialized)
    val readiness: State<EngineReadiness> = _readiness

    private val _tabletopMode = mutableStateOf(false)
    val tabletopMode: State<Boolean> = _tabletopMode

    private var activeJob: Job? = null
    private var turnId: Int = 0

    init {
        viewModelScope.launch {
            modelManager.state.collectLatest { model ->
                when (model) {
                    is ModelState.Ready -> _readiness.value = EngineReadiness.Ready
                    is ModelState.Error -> _readiness.value = EngineReadiness.Error(model.reason.userMessage)
                    else -> _readiness.value = EngineReadiness.Loading(message = "Waiting for model…")
                }
                if (model is ModelState.Ready) {
                    if (_state.value is ConversationState.Setup) {
                        _state.value = ConversationState.Ready(_sourceLanguage.value, _targetLanguage.value)
                    }
                } else {
                    _state.value = ConversationState.Setup
                }
            }
        }
        viewModelScope.launch {
            speechRecognizer.readiness.collectLatest { }
        }
    }

    fun setSource(language: Language) {
        if (_state.value.isProcessing) return
        _preferredSource.value = language
        if (language == _preferredTarget.value) {
            _preferredTarget.value = _sourceLanguage.value
        }
        _sourceLanguage.value = _preferredSource.value
        _targetLanguage.value = _preferredTarget.value
        updateReadyState()
    }

    fun setTarget(language: Language) {
        if (_state.value.isProcessing) return
        _preferredTarget.value = language
        if (language == _preferredSource.value) {
            _preferredSource.value = _targetLanguage.value
        }
        _sourceLanguage.value = _preferredSource.value
        _targetLanguage.value = _preferredTarget.value
        updateReadyState()
    }

    fun swapLanguages() {
        if (_state.value.isProcessing) return
        val oldSource = _preferredSource.value
        _preferredSource.value = _preferredTarget.value
        _preferredTarget.value = oldSource
        _sourceLanguage.value = _preferredSource.value
        _targetLanguage.value = _preferredTarget.value
        updateReadyState()
    }

    fun toggleTabletopMode() {
        _tabletopMode.value = !_tabletopMode.value
    }

    fun startRecordingIn(language: Language) {
        if (_state.value.isProcessing) return
        val other = if (language == _preferredSource.value) _preferredTarget.value else _preferredSource.value
        _sourceLanguage.value = language
        _targetLanguage.value = other
        startRecording()
    }

    fun startRecording() {
        val current = _state.value
        if (current is ConversationState.Speaking) {
            viewModelScope.launch { speechSynthesizer.stop() }
        }
        if (current is ConversationState.Ready || current is ConversationState.Speaking || current is ConversationState.Error) {
            performanceRecorder.mark(PipelineEvent.PressReceived())
            performanceRecorder.mark(PipelineEvent.RecorderStarted())
            _state.value = ConversationState.Recording(
                sourceLanguage = _sourceLanguage.value,
                targetLanguage = _targetLanguage.value
            )
            activeJob?.cancel()
            activeJob = viewModelScope.launch {
                try {
                    audioRecorder.start()
                } catch (e: CancellationException) {
                    // expected on cancellation
                }
            }
        }
    }

    fun stopRecording() {
        val recording = _state.value as? ConversationState.Recording ?: return
        performanceRecorder.mark(PipelineEvent.RecorderStopped())
        activeJob?.cancel()
        activeJob = null

        _state.value = ConversationState.Transcribing(
            sourceLanguage = recording.sourceLanguage,
            targetLanguage = recording.targetLanguage
        )

        activeJob = viewModelScope.launch {
            try {
                val audio = audioRecorder.stop()
                performanceRecorder.mark(PipelineEvent.AudioAvailable())
                performanceRecorder.mark(PipelineEvent.SttStarted())
                val transcript = speechRecognizer.transcribe(audio, recording.sourceLanguage)
                performanceRecorder.mark(PipelineEvent.SttFinal())
                translateAndSpeak(transcript.text, recording.sourceLanguage, recording.targetLanguage)
            } catch (e: CancellationException) {
                recoverToReady()
            } catch (e: Exception) {
                _state.value = ConversationState.Error(
                    previousState = recording,
                    error = ConversationError.Transcription(e.localizedMessage ?: "Transcription failed")
                )
            }
        }
    }

    fun cancel() {
        activeJob?.cancel()
        activeJob = null
        viewModelScope.launch { speechSynthesizer.stop() }
        performanceRecorder.mark(PipelineEvent.CancellationRequested())
        recoverToReady()
        performanceRecorder.mark(PipelineEvent.CancellationCompleted())
    }

    fun replayLast() {
        val last = _turns.lastOrNull() ?: return
        speakTurn(last)
    }

    fun retryLast() {
        val last = _turns.lastOrNull() ?: return
        viewModelScope.launch {
            translateAndSpeak(last.transcript.text, last.sourceLanguage, last.targetLanguage)
        }
    }

    fun editTranscript(turnId: String, newText: String) {
        val index = _turns.indexOfFirst { it.id == turnId }
        if (index == -1) return
        val turn = _turns[index]
        _turns[index] = turn.copy(transcript = turn.transcript.copy(text = newText, isFinal = true))
    }

    private fun updateReadyState() {
        if (_state.value is ConversationState.Ready || _state.value is ConversationState.Setup) {
            _state.value = if (_readiness.value.isReady) {
                ConversationState.Ready(_sourceLanguage.value, _targetLanguage.value)
            } else ConversationState.Setup
        }
    }

    private fun recoverToReady() {
        _sourceLanguage.value = _preferredSource.value
        _targetLanguage.value = _preferredTarget.value
        _state.value = ConversationState.Ready(_sourceLanguage.value, _targetLanguage.value)
    }

    private fun translateAndSpeak(text: String, source: Language, target: Language) {
        activeJob = viewModelScope.launch {
            try {
                _state.value = ConversationState.Translating(source, target, transcript = com.schilling3003.relay.domain.Transcript(text))
                performanceRecorder.mark(PipelineEvent.TranslationQueued())
                val translation = translationEngine.translate(text, source, target)
                performanceRecorder.mark(PipelineEvent.TranslationFinal())
                val turn = ConversationTurn(
                    sourceLanguage = source,
                    targetLanguage = target,
                    transcript = com.schilling3003.relay.domain.Transcript(text),
                    translation = translation
                )
                _turns.add(turn)
                _state.value = ConversationState.Speaking(source, target, turn)
                speakTurn(turn)
            } catch (e: CancellationException) {
                recoverToReady()
            } catch (e: Exception) {
                _state.value = ConversationState.Error(
                    previousState = _state.value,
                    error = ConversationError.Translation(e.localizedMessage ?: "Translation failed")
                )
            }
        }
    }

    private fun speakTurn(turn: ConversationTurn) {
        val translation = turn.translation ?: return
        activeJob = viewModelScope.launch {
            try {
                performanceRecorder.mark(PipelineEvent.TtsQueued())
                speechSynthesizer.speak(translation.translatedText, turn.targetLanguage)
                performanceRecorder.mark(PipelineEvent.TtsComplete())
                val index = _turns.indexOfFirst { it.id == turn.id }
                if (index != -1) {
                    _turns[index] = turn.copy(wasPlayed = true)
                }
                recoverToReady()
            } catch (e: CancellationException) {
                recoverToReady()
            } catch (e: Exception) {
                _state.value = ConversationState.Error(
                    previousState = _state.value,
                    error = ConversationError.Synthesis(e.localizedMessage ?: "Speech failed")
                )
            }
        }
    }

    class Factory(
        private val modelManager: ModelManager,
        private val speechRecognizer: SpeechRecognizer,
        private val translationEngine: TranslationEngine,
        private val speechSynthesizer: SpeechSynthesizer,
        private val audioRecorder: AudioRecorder,
        private val audioPlayer: AudioPlayer,
        private val performanceRecorder: PerformanceRecorder
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ConversationViewModel(
                modelManager,
                speechRecognizer,
                translationEngine,
                speechSynthesizer,
                audioRecorder,
                audioPlayer,
                performanceRecorder
            ) as T
    }
}
