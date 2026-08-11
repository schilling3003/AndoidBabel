package com.schilling3003.relay.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.schilling3003.relay.RelayApplication
import com.schilling3003.relay.ui.conversation.ConversationScreen
import com.schilling3003.relay.ui.settings.SettingsScreen
import com.schilling3003.relay.ui.setup.SetupScreen
import com.schilling3003.relay.ui.theme.RelayTheme
import com.schilling3003.relay.viewmodel.ConversationViewModel
import com.schilling3003.relay.viewmodel.ModelDownloadViewModel
import com.schilling3003.relay.viewmodel.SetupViewModel

/**
 * Root Compose entry point. Switches between setup, conversation, and settings surfaces.
 */
@Composable
fun RelayApp(recordPermissionGranted: Boolean = false) {
    val app = RelayApplication.instance
    val setupViewModel: SetupViewModel = viewModel(
        factory = SetupViewModel.Factory(app.modelManager, app.translationEngine, app.modelDownloader)
    )
    val downloadViewModel: ModelDownloadViewModel = viewModel(
        factory = ModelDownloadViewModel.Factory(app.modelDownloader)
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

    var settingsOpen by remember { mutableStateOf(false) }
    val openSettings = { settingsOpen = true }
    val closeSettings = { settingsOpen = false }

    RelayTheme {
        when {
            settingsOpen -> SettingsScreen(
                setupViewModel = setupViewModel,
                downloadViewModel = downloadViewModel,
                conversationViewModel = conversationViewModel,
                onBack = closeSettings
            )
            setupViewModel.shouldShowSetup.value -> SetupScreen(
                viewModel = setupViewModel,
                downloadViewModel = downloadViewModel,
                onSetupComplete = { setupViewModel.dismissSetup() },
                onSettings = openSettings
            )
            else -> ConversationScreen(
                viewModel = conversationViewModel,
                recordPermissionGranted = recordPermissionGranted,
                onSettings = openSettings
            )
        }
    }
}
