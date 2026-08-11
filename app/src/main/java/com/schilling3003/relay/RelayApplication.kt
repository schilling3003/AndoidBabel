package com.schilling3003.relay

import android.app.Application
import com.schilling3003.relay.audio.AudioPlayer
import com.schilling3003.relay.audio.AudioRecorder
import com.schilling3003.relay.audio.AudioTrackAudioPlayer
import com.schilling3003.relay.audio.FakeAudioPlayer
import com.schilling3003.relay.audio.FakeAudioRecorder
import com.schilling3003.relay.audio.RealAudioRecorder
import com.schilling3003.relay.engines.ModelManager
import com.schilling3003.relay.engines.PerformanceRecorder
import com.schilling3003.relay.engines.SpeechRecognizer
import com.schilling3003.relay.engines.SpeechSynthesizer
import com.schilling3003.relay.engines.TranslationEngine
import com.schilling3003.relay.engines.fake.FakeModelManager
import com.schilling3003.relay.engines.fake.FakePerformanceRecorder
import com.schilling3003.relay.engines.fake.FakeSpeechRecognizer
import com.schilling3003.relay.engines.fake.FakeSpeechSynthesizer
import com.schilling3003.relay.engines.fake.FakeTranslationEngine
import com.schilling3003.relay.engines.FilePerformanceRecorder
import com.schilling3003.relay.engines.litert.GemmaTranslationEngine
import com.schilling3003.relay.engines.moonshine.MoonshineSpeechRecognizer
import com.schilling3003.relay.engines.moonshine.MoonshineSpeechSynthesizer
import com.schilling3003.relay.storage.LocalModelManager

/**
 * Lightweight application container. In v1 we use manual constructor wiring
 * because the dependency graph is small and deterministic; a framework is not
 * justified until the graph grows.
 */
class RelayApplication : Application() {

    lateinit var modelManager: ModelManager
        private set
    lateinit var speechRecognizer: SpeechRecognizer
        private set
    lateinit var translationEngine: TranslationEngine
        private set
    lateinit var speechSynthesizer: SpeechSynthesizer
        private set
    lateinit var audioRecorder: AudioRecorder
        private set
    lateinit var audioPlayer: AudioPlayer
        private set
    lateinit var performanceRecorder: PerformanceRecorder
        private set

    /**
     * Set to true for UI-only gauntlet rounds that do not require real model
     * assets. The release build uses real engines.
     */
    val useFakeEngines: Boolean = false

    override fun onCreate() {
        super.onCreate()
        instance = this
        initializeEngines()
    }

    private fun initializeEngines() {
        if (useFakeEngines) {
            modelManager = FakeModelManager()
            speechRecognizer = FakeSpeechRecognizer()
            translationEngine = FakeTranslationEngine()
            speechSynthesizer = FakeSpeechSynthesizer()
            audioRecorder = FakeAudioRecorder()
            audioPlayer = FakeAudioPlayer()
            performanceRecorder = FakePerformanceRecorder()
        } else {
            modelManager = LocalModelManager(this)
            audioPlayer = AudioTrackAudioPlayer()
            speechRecognizer = MoonshineSpeechRecognizer(this)
            translationEngine = GemmaTranslationEngine(this, modelManager)
            speechSynthesizer = MoonshineSpeechSynthesizer(this, audioPlayer)
            audioRecorder = RealAudioRecorder(this)
            performanceRecorder = FilePerformanceRecorder(this)
        }
    }

    companion object {
        lateinit var instance: RelayApplication
            private set
    }
}
