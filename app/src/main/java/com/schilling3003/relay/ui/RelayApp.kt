package com.schilling3003.relay.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.schilling3003.relay.RelayApplication
import com.schilling3003.relay.ui.conversation.ConversationScreen
import com.schilling3003.relay.ui.setup.SetupScreen
import com.schilling3003.relay.ui.theme.RelayTheme
import com.schilling3003.relay.viewmodel.ConversationViewModel
import com.schilling3003.relay.viewmodel.SetupViewModel

/**
 * Root Compose entry point. Switches between setup and conversation surfaces.
 */
@Composable
fun RelayApp() {
    val app = RelayApplication.instance
    val setupViewModel: SetupViewModel = viewModel(
        factory = SetupViewModel.Factory(app.modelManager)
    )
    val conversationViewModel: ConversationViewModel = viewModel(
        factory = ConversationViewModel.Factory(
            app.modelManager,
            app.speechRecognizer,
            app.translationEngine,
            app.speechSynthesizer,
            app.audioRecorder,
            app.audioPlayer,
            app.performanceRecorder
        )
    )

    RelayTheme {
        if (setupViewModel.shouldShowSetup.value) {
            SetupScreen(
                viewModel = setupViewModel,
                onSetupComplete = { setupViewModel.dismissSetup() }
            )
        } else {
            ConversationScreen(viewModel = conversationViewModel)
        }
    }
}
