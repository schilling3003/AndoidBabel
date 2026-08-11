package com.schilling3003.relay.engines.moonshine

import ai.moonshine.voice.AssetDownloader
import ai.moonshine.voice.DownloadProgress
import ai.moonshine.voice.JNI
import ai.moonshine.voice.ModelSpec
import ai.moonshine.voice.MoonshineDownloadWorker
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.schilling3003.relay.domain.Language
import java.io.File
import java.util.UUID

/**
 * Coordinates downloading Moonshine STT/TTS model files through the same
 * [MoonshineDownloadWorker] the library provides. The download manager is
 * intentionally separate from the speech engines so the setup UI can show status
 * and progress before the first conversation turn.
 */
class ModelDownloadManager(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)
    private val assetDownloader = AssetDownloader()

    data class DownloadSpec(
        val id: String,
        val displayName: String,
        val type: ModelType,
        val language: Language,
        val modelDir: File,
        val modelSpec: ModelSpec
    ) {
        enum class ModelType { STT, TTS }
    }

    data class DownloadTask(
        val spec: DownloadSpec,
        val workId: UUID? = null,
        val workState: WorkInfo.State? = null,
        val progress: DownloadProgress? = null,
        val isPresent: Boolean = false,
        val error: String? = null
    )

    fun requiredSpecs(source: Language, target: Language): List<DownloadSpec> {
        val languages = listOf(source, target).distinct()
        return languages.flatMap { language ->
            listOf(
                sttSpec(language),
                ttsSpec(language)
            )
        }
    }

    fun isPresent(spec: DownloadSpec): Boolean =
        assetDownloader.isModelPresent(spec.modelDir, spec.modelSpec)

    fun startDownload(spec: DownloadSpec, requireUnmetered: Boolean = false): UUID {
        spec.modelDir.mkdirs()
        val request = MoonshineDownloadWorker.buildRequest(spec.modelDir, spec.modelSpec, requireUnmetered)
        workManager.enqueue(request)
        return request.id
    }

    fun workInfo(workId: UUID): LiveData<WorkInfo> =
        workManager.getWorkInfoByIdLiveData(workId)

    private fun sttSpec(language: Language): DownloadSpec {
        val code = language.code
        val arch = defaultSttModelArch(language)
        val modelDir = File(context.filesDir, "moonshine/stt/$code").apply { mkdirs() }
        return DownloadSpec(
            id = "stt-$code",
            displayName = "${language.displayName} speech-to-text",
            type = DownloadSpec.ModelType.STT,
            language = language,
            modelDir = modelDir,
            modelSpec = ModelSpec.stt(code, arch, false)
        )
    }

    private fun ttsSpec(language: Language): DownloadSpec {
        val code = language.code
        val voice = defaultVoice(language)
        val modelDir = File(context.filesDir, "moonshine/tts/$code").apply { mkdirs() }
        return DownloadSpec(
            id = "tts-$code",
            displayName = "${language.displayName} text-to-speech",
            type = DownloadSpec.ModelType.TTS,
            language = language,
            modelDir = modelDir,
            modelSpec = ModelSpec.tts(code, voice)
        )
    }

    companion object {
        /**
         * Language-specific default voice. Kokoro voices are used where available;
         * Arabic and Korean fall back to Piper voices because the moonshine-voice
         * 0.1.1 AAR does not ship Kokoro voices for those languages.
         */
        fun defaultVoice(language: Language): String = when (language) {
            Language.ENGLISH -> "kokoro_af_heart"
            Language.SPANISH -> "kokoro_ef_dora"
            Language.JAPANESE -> "kokoro_jf_alpha"
            Language.MANDARIN -> "kokoro_zf_xiaobei"
            Language.ARABIC -> "piper_ar_JO-kareem-medium"
            Language.KOREAN -> "piper_ko_KR-melotts-medium"
        }

        fun defaultSttModelArch(language: Language): Int = when (language) {
            // English has a tiny model that keeps the APK-adjacent footprint small.
            Language.ENGLISH, Language.KOREAN -> JNI.MOONSHINE_MODEL_ARCH_TINY
            else -> JNI.MOONSHINE_MODEL_ARCH_BASE
        }
    }
}
