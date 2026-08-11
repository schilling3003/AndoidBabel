package com.schilling3003.relay.engines.litert

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Contents
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.LogSeverity
import com.google.ai.edge.litertlm.Message
import com.schilling3003.relay.domain.EngineReadiness
import com.schilling3003.relay.domain.Language
import com.schilling3003.relay.domain.ModelState
import com.schilling3003.relay.domain.Translation
import com.schilling3003.relay.engines.ModelManager
import com.schilling3003.relay.engines.TranslationEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Gemma 4 E2B translation through LiteRT-LM.
 *
 * Keeps one [Engine] warm while the model file is available, closes it when the
 * model is removed, and runs all inference on [Dispatchers.IO].
 */
class GemmaTranslationEngine(
    private val context: Context,
    private val modelManager: ModelManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TranslationEngine {

    private val _readiness = MutableStateFlow<EngineReadiness>(EngineReadiness.Uninitialized)
    override val readiness: StateFlow<EngineReadiness> = _readiness.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val engineLock = Mutex()
    private var engine: Engine? = null
    private var observeJob: Job? = null

    init {
        Engine.setNativeMinLogSeverity(LogSeverity.ERROR)
        observeJob = scope.launch {
            modelManager.state.collectLatest { state ->
                when (state) {
                    is ModelState.Ready -> loadEngine(state.path)
                    else -> closeEngine("Model not available")
                }
            }
        }
    }

    override suspend fun translate(text: String, source: Language, target: Language): Translation =
        withContext(dispatcher) {
            val modelPath = engineLock.withLock {
                (modelManager.state.value as? ModelState.Ready)?.path
                    ?: throw ModelUnavailableException("Gemma model is not ready")
            }

            val conversation = createConversation(modelPath)
            try {
                val prompt = buildPrompt(text, source, target)
                val result = conversation.sendMessage(prompt)
                val translated = extractTranslation(result)
                Translation(
                    sourceText = text,
                    translatedText = translated,
                    sourceLanguage = source,
                    targetLanguage = target,
                    raw = result.toString()
                )
            } finally {
                conversation.close()
            }
        }

    override suspend fun cancel() {
        // Cancelling the coroutine scope in which inference runs is the cleanest
        // LiteRT-LM provides at this level. The engine itself is preserved for reuse.
        observeJob?.cancel()
    }

    private suspend fun loadEngine(modelPath: String) {
        engineLock.withLock {
            if (engine?.let { it.isInitialized() } == true) {
                _readiness.value = EngineReadiness.Ready
                return
            }
            _readiness.value = EngineReadiness.Loading(message = "Loading Gemma…")
            try {
                val previous = engine
                val config = EngineConfig(
                    modelPath = modelPath,
                    backend = Backend.CPU(),
                    cacheDir = context.cacheDir.absolutePath
                )
                val newEngine = Engine(config)
                newEngine.initialize()
                previous?.close()
                engine = newEngine
                _readiness.value = EngineReadiness.Ready
            } catch (e: Exception) {
                closeEngine("Failed to load Gemma: ${e.localizedMessage}")
                throw e
            }
        }
    }

    private suspend fun closeEngine(reason: String) {
        engineLock.withLock {
            engine?.close()
            engine = null
            _readiness.value = if (reason.isBlank()) EngineReadiness.Uninitialized else EngineReadiness.Error(reason)
        }
    }

    private suspend fun createConversation(modelPath: String): Conversation {
        val current = engineLock.withLock {
            val e = engine
                ?: throw ModelUnavailableException("Gemma engine is not loaded")
            if (!e.isInitialized()) {
                loadEngine(modelPath)
                engine ?: throw ModelUnavailableException("Gemma engine failed to load")
            } else {
                e
            }
        }
        return current.createConversation(
            ConversationConfig(
                systemInstruction = Contents.of(SYSTEM_PROMPT)
            )
        )
    }

    private fun buildPrompt(text: String, source: Language, target: Language): String =
        """Translate the following from ${source.displayName} to ${target.displayName}.
            |Respond with only the translation, no explanation, no quotation marks, no prefixes.
            |
            |$text""".trimMargin()

    private fun extractTranslation(message: Message): String {
        return message.toString()
            .trim()
            .removeSurrounding("\"")
            .removeSurrounding("'")
    }

    private class ModelUnavailableException(message: String) : Exception(message)

    companion object {
        private const val SYSTEM_PROMPT =
            "You are a precise offline translator. Reply with only the translated text."
    }
}
