package com.schilling3003.relay.viewmodel

import com.schilling3003.relay.audio.FakeAudioPlayer
import com.schilling3003.relay.audio.FakeAudioRecorder
import com.schilling3003.relay.domain.ConversationState
import com.schilling3003.relay.domain.Language
import com.schilling3003.relay.domain.ModelState
import com.schilling3003.relay.engines.fake.FakeModelManager
import com.schilling3003.relay.engines.fake.FakePerformanceRecorder
import com.schilling3003.relay.engines.fake.FakeSpeechRecognizer
import com.schilling3003.relay.engines.fake.FakeSpeechSynthesizer
import com.schilling3003.relay.engines.fake.FakeTranslationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: ConversationViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        modelState: ModelState = ModelState.Ready("", null, 0)
    ): ConversationViewModel {
        return ConversationViewModel(
            FakeModelManager(initialState = modelState),
            FakeSpeechRecognizer(delayMs = 0),
            FakeTranslationEngine(delayMs = 0),
            FakeSpeechSynthesizer(speakDurationMs = 0),
            FakeAudioRecorder(),
            FakeAudioPlayer(),
            FakePerformanceRecorder()
        )
    }

    @Test
    fun `initial state is Ready when model is ready`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()
        assertTrue(viewModel.state.value is ConversationState.Ready)
    }

    @Test
    fun `initial state is Setup when model is missing`() = runTest(testDispatcher) {
        viewModel = createViewModel(ModelState.Missing)
        advanceUntilIdle()
        assertTrue(viewModel.state.value is ConversationState.Setup)
    }

    @Test
    fun `complete turn produces a turn and returns to Ready`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.startRecording()
        assertTrue(viewModel.state.value is ConversationState.Recording)

        viewModel.stopRecording()
        advanceUntilIdle()

        assertTrue(viewModel.state.value is ConversationState.Ready)
        assertEquals(1, viewModel.turns.size)

        val turn = viewModel.turns.first()
        assertEquals(Language.ENGLISH, turn.sourceLanguage)
        assertEquals(Language.SPANISH, turn.targetLanguage)
        assertTrue(turn.transcript.text.isNotBlank())
        assertTrue(turn.translation?.translatedText?.isNotBlank() == true)
    }

    @Test
    fun `swap languages exchanges source and target`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(Language.ENGLISH, viewModel.sourceLanguage.value)
        assertEquals(Language.SPANISH, viewModel.targetLanguage.value)
        viewModel.swapLanguages()
        assertEquals(Language.SPANISH, viewModel.sourceLanguage.value)
        assertEquals(Language.ENGLISH, viewModel.targetLanguage.value)
    }

    @Test
    fun `swap is ignored while processing`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startRecording()
        viewModel.swapLanguages()
        assertEquals(Language.ENGLISH, viewModel.sourceLanguage.value)
        assertEquals(Language.SPANISH, viewModel.targetLanguage.value)
    }

    @Test
    fun `cancel returns to Ready`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.startRecording()
        viewModel.cancel()
        advanceUntilIdle()
        assertTrue(viewModel.state.value is ConversationState.Ready)
    }

    @Test
    fun `turn accumulation keeps history order`() = runTest(testDispatcher) {
        viewModel = createViewModel()
        advanceUntilIdle()
        repeat(2) {
            viewModel.startRecording()
            viewModel.stopRecording()
            advanceUntilIdle()
        }
        assertEquals(2, viewModel.turns.size)
    }
}
