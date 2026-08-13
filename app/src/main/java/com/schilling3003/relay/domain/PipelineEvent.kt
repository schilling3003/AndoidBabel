package com.schilling3003.relay.domain

sealed class PipelineEvent {
    abstract val stage: String

    data class PressReceived(override val stage: String = "press_received") : PipelineEvent()
    data class RecordingFeedbackRendered(override val stage: String = "recording_feedback_rendered") : PipelineEvent()
    data class RecorderStarted(override val stage: String = "recorder_started") : PipelineEvent()
    data class RecorderStopped(override val stage: String = "recorder_stopped") : PipelineEvent()
    data class AudioAvailable(override val stage: String = "audio_available") : PipelineEvent()
    data class SttStarted(override val stage: String = "stt_started") : PipelineEvent()
    data class SttPartial(override val stage: String = "stt_partial") : PipelineEvent()
    data class SttFinal(override val stage: String = "stt_final") : PipelineEvent()
    data class TranslationQueued(override val stage: String = "translation_queued") : PipelineEvent()
    data class TranslationFirstToken(override val stage: String = "translation_first_token") : PipelineEvent()
    data class TranslationFinal(override val stage: String = "translation_final") : PipelineEvent()
    data class TtsQueued(override val stage: String = "tts_queued") : PipelineEvent()
    data class TtsFirstAudio(override val stage: String = "tts_first_audio") : PipelineEvent()
    data class TtsComplete(override val stage: String = "tts_complete") : PipelineEvent()
    data class CancellationRequested(override val stage: String = "cancellation_requested") : PipelineEvent()
    data class CancellationCompleted(override val stage: String = "cancellation_completed") : PipelineEvent()
    data class EngineLoadStart(override val stage: String = "engine_load_start") : PipelineEvent()
    data class EngineLoadReady(override val stage: String = "engine_load_ready") : PipelineEvent()
    data class MemorySnapshot(override val stage: String = "memory_snapshot") : PipelineEvent()
}
