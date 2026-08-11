package com.schilling3003.relay

import android.app.Application
import com.schilling3003.relay.audio.AudioPlayer
import com.schilling3003.relay.audio.AudioRecorder
import com.schilling3003.relay.audio.FakeAudioPlayer
import com.schilling3003.relay.audio.FakeAudioRecorder
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

    /** Switch to true to exercise fake engines during UI-only gauntlet rounds. */
    val useFakeEngines: Boolean = true

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
            // TODO: wire real Moonshine/LiteRT-LM implementations once the vertical slice
            // has passed UI and state-machine gates.
            modelManager = LocalModelManager(this)
            speechRecognizer = FakeSpeechRecognizer()
            translationEngine = FakeTranslationEngine()
            speechSynthesizer = FakeSpeechSynthesizer()
            audioRecorder = FakeAudioRecorder()
            audioPlayer = FakeAudioPlayer()
            performanceRecorder = FakePerformanceRecorder()
        }
    }

    companion object {
        lateinit var instance: RelayApplication
            private set
    }
}
