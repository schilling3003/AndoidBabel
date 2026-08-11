package com.schilling3003.relay.engines.moonshine

import ai.moonshine.voice.AssetDownloader
import ai.moonshine.voice.ModelSpec
import ai.moonshine.voice.TextToSpeech
import com.schilling3003.relay.audio.AudioPlayer
import com.schilling3003.relay.domain.EngineReadiness
import com.schilling3003.relay.domain.Language
import com.schilling3003.relay.engines.SpeechSynthesizer
import com.schilling3003.relay.engines.SynthesisProgress
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

/**
 * On-device TTS using Moonshine Voice. Downloads the target-language voice model
 * on first use, then synthesizes to PCM and hands it to [audioPlayer].
 */
class MoonshineSpeechSynthesizer(
    private val context: android.content.Context,
    private val audioPlayer: AudioPlayer,
    private val voice: String = "",
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SpeechSynthesizer {

    private val _readiness = MutableStateFlow<EngineReadiness>(EngineReadiness.Uninitialized)
    override val readiness: StateFlow<EngineReadiness> = _readiness.asStateFlow()

    private val _progress = MutableSharedFlow<SynthesisProgress>(extraBufferCapacity = 1)
    override fun progress(): SharedFlow<SynthesisProgress> = _progress.asSharedFlow()

    private val lock = Mutex()
    private val textToSpeech = TextToSpeech(context)
    private var loadedLanguage: String? = null
    private var loadedVoice: String? = null

    override suspend fun speak(text: String, language: Language) {
        withContext(dispatcher) {
            ensureLoaded(language)
            _progress.tryEmit(SynthesisProgress.Started)
            try {
                textToSpeech.language(language.code)
                val result = textToSpeech.synthesize(text)
                _progress.tryEmit(SynthesisProgress.ChunkReady(0))
                val pcm = floatToPcm16(result.samples)
                _progress.tryEmit(SynthesisProgress.Playing(0.5f))
                audioPlayer.play(pcm, result.sampleRateHz)
                _progress.tryEmit(SynthesisProgress.Completed)
            } catch (e: CancellationException) {
                audioPlayer.stop()
                throw e
            } catch (e: Exception) {
                _progress.tryEmit(SynthesisProgress.Error(e.localizedMessage ?: "TTS failed"))
                throw e
            }
        }
    }

    override suspend fun stop() {
        withContext(dispatcher) {
            textToSpeech.stop()
            audioPlayer.stop()
        }
    }

    private suspend fun ensureLoaded(language: Language) {
        val code = language.code
        val selectedVoice = voice.takeIf { it.isNotBlank() } ?: ModelDownloadManager.defaultVoice(language)
        lock.withLock {
            if (loadedLanguage == code && loadedVoice == selectedVoice && textToSpeech.isLoaded) {
                _readiness.value = EngineReadiness.Ready
                return
            }

            _readiness.value = EngineReadiness.Loading(message = "Loading TTS model for ${language.displayName}…")
            try {
                val modelDir = File(context.filesDir, "moonshine/tts/$code").apply { mkdirs() }
                val root = AssetDownloader().ensureModelPresent(
                    modelDir,
                    ModelSpec.tts(code, selectedVoice),
                    null
                )
                textToSpeech.close()
                textToSpeech.modelsFrom(root).language(code).voice(selectedVoice).load()
                loadedLanguage = code
                loadedVoice = selectedVoice
                _readiness.value = EngineReadiness.Ready
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _readiness.value = EngineReadiness.Error("TTS model load failed: ${e.localizedMessage}")
                throw e
            }
        }
    }

    private fun floatToPcm16(samples: FloatArray): ByteArray {
        val bytes = ByteArray(samples.size * 2)
        for (i in samples.indices) {
            val sample = (samples[i].coerceIn(-1.0f, 1.0f) * 32_767).toInt().toShort()
            bytes[i * 2] = (sample.toInt() and 0xFF).toByte()
            bytes[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
        }
        return bytes
    }
}
