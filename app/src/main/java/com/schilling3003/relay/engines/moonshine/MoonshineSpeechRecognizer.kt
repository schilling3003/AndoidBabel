package com.schilling3003.relay.engines.moonshine

import ai.moonshine.voice.AssetDownloader
import ai.moonshine.voice.JNI
import ai.moonshine.voice.ModelSpec
import ai.moonshine.voice.Transcriber
import com.schilling3003.relay.domain.EngineReadiness
import com.schilling3003.relay.domain.Language
import com.schilling3003.relay.domain.PartialTranscript
import com.schilling3003.relay.domain.RecordedAudio
import com.schilling3003.relay.domain.Transcript
import com.schilling3003.relay.engines.SpeechRecognizer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

/**
 * On-device STT using Moonshine Voice. Models are downloaded on first use to
 * [context.filesDir/moonshine/stt/&lt;language&gt;] when absent; the loaded
 * [Transcriber] is reused across utterances.
 */
class MoonshineSpeechRecognizer(
    private val context: android.content.Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SpeechRecognizer {

    private val _readiness = MutableStateFlow<EngineReadiness>(EngineReadiness.Uninitialized)
    override val readiness: StateFlow<EngineReadiness> = _readiness.asStateFlow()

    private val lock = Mutex()
    private val transcriber = Transcriber()
    private var loadedLanguage: String? = null
    private var loadedModelArch: Int = JNI.MOONSHINE_MODEL_ARCH_TINY

    override fun partials(): Flow<PartialTranscript> = emptyFlow()

    override suspend fun transcribe(audio: RecordedAudio, language: Language): Transcript =
        withContext(dispatcher) {
            ensureLoaded(language)
            val samples = pcm16ToFloat(audio.pcmBytes)
            val result = transcriber.transcribeWithoutStreaming(samples, audio.sampleRateHz)
            Transcript(
                text = result.text().orEmpty(),
                isFinal = true,
                confidence = null
            )
        }

    override suspend fun cancel() {
        withContext(dispatcher) {
            try {
                transcriber.stop()
            } catch (_: Exception) {
                // stop() is safe to call even when not streaming.
            }
        }
    }

    private suspend fun ensureLoaded(language: Language) {
        val code = language.code
        lock.withLock {
            val arch = ModelDownloadManager.defaultSttModelArch(language)
            if (loadedLanguage == code && loadedModelArch == arch && transcriber.isLoaded) {
                _readiness.value = EngineReadiness.Ready
                return
            }

            _readiness.value = EngineReadiness.Loading(message = "Loading STT model for ${language.displayName}…")
            try {
                val modelDir = File(context.filesDir, "moonshine/stt/$code").apply { mkdirs() }
                val root = AssetDownloader().ensureModelPresent(
                    modelDir,
                    ModelSpec.stt(code, arch, false),
                    null
                )
                transcriber.close()
                transcriber.loadFromFiles(root.absolutePath, arch)
                loadedLanguage = code
                loadedModelArch = arch
                _readiness.value = EngineReadiness.Ready
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _readiness.value = EngineReadiness.Error("STT model load failed: ${e.localizedMessage}")
                throw e
            }
        }
    }

    private fun pcm16ToFloat(pcmBytes: ByteArray): FloatArray {
        val shorts = ShortArray(pcmBytes.size / 2)
        for (i in shorts.indices) {
            val lo = pcmBytes[i * 2].toInt() and 0xFF
            val hi = pcmBytes[i * 2 + 1].toInt()
            shorts[i] = ((hi shl 8) or lo).toShort()
        }
        return FloatArray(shorts.size) { i ->
            shorts[i] / 32_768.0f
        }
    }
}
