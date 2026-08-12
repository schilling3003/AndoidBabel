package com.schilling3003.relay.ui.conversation

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.schilling3003.relay.R
import com.schilling3003.relay.domain.ConversationState
import com.schilling3003.relay.domain.ConversationTurn
import com.schilling3003.relay.domain.EngineReadiness
import com.schilling3003.relay.domain.Language
import com.schilling3003.relay.domain.LayoutDirection as RelayLayoutDirection
import com.schilling3003.relay.viewmodel.ConversationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    viewModel: ConversationViewModel,
    recordPermissionGranted: Boolean = true,
    onSettings: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val source by viewModel.sourceLanguage
    val target by viewModel.targetLanguage
    val tabletop by viewModel.tabletopMode
    val turns = viewModel.turns

    val layoutDirection = if (source.direction == RelayLayoutDirection.RTL) {
        LayoutDirection.Rtl
    } else {
        LayoutDirection.Ltr
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                RelayTopBar(
                    source = source,
                    target = target,
                    state = state,
                    turns = turns,
                    onSwap = { viewModel.swapLanguages() },
                    onCancel = { viewModel.cancel() },
                    onReplay = { viewModel.replayLast() },
                    onToggleTabletop = { viewModel.toggleTabletopMode() },
                    onSettings = onSettings,
                    scrollBehavior = scrollBehavior
                )
            }
        ) { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (tabletop) {
                    TabletopLayout(
                        source = source,
                        target = target,
                        state = state,
                        turns = turns,
                        recordPermissionGranted = recordPermissionGranted,
                        onSourceSpeak = { viewModel.startRecordingIn(source) },
                        onSourceRelease = { viewModel.stopRecording() },
                        onTargetSpeak = { viewModel.startRecordingIn(target) },
                        onTargetRelease = { viewModel.stopRecording() }
                    )
                } else {
                    PortraitLayout(
                        source = source,
                        state = state,
                        turns = turns,
                        recordPermissionGranted = recordPermissionGranted,
                        onSpeakPress = { viewModel.startRecording() },
                        onSpeakRelease = { viewModel.stopRecording() },
                        onCancel = { viewModel.cancel() },
                        onReplay = { viewModel.replayLast() },
                        onRetry = { viewModel.retryLast() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RelayTopBar(
    source: Language,
    target: Language,
    state: ConversationState,
    turns: List<ConversationTurn>,
    onSwap: () -> Unit,
    onCancel: () -> Unit,
    onReplay: () -> Unit,
    onToggleTabletop: () -> Unit,
    onSettings: () -> Unit,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior
) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(0.75f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = source.displayLabel(),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )
                IconButton(onClick = onSwap) {
                    Icon(
                        imageVector = Icons.Filled.SwapHoriz,
                        contentDescription = stringResource(R.string.conversation_swap_languages)
                    )
                }
                Text(
                    text = target.displayLabel(),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }
        },
        actions = {
            if (turns.isNotEmpty() && state !is ConversationState.Speaking) {
                IconButton(onClick = onReplay) {
                    Icon(
                        imageVector = Icons.Filled.Replay,
                        contentDescription = stringResource(R.string.conversation_replay)
                    )
                }
            }
            if (state.isProcessing) {
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.conversation_cancel)
                    )
                }
            }
            IconButton(onClick = onToggleTabletop) {
                Icon(
                    imageVector = Icons.Filled.Splitscreen,
                    contentDescription = stringResource(R.string.conversation_tabletop_mode)
                )
            }
            IconButton(onClick = onSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = stringResource(R.string.settings_title)
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun PortraitLayout(
    source: Language,
    state: ConversationState,
    turns: List<ConversationTurn>,
    recordPermissionGranted: Boolean = true,
    modifier: Modifier = Modifier,
    onSpeakPress: () -> Unit,
    onSpeakRelease: () -> Unit,
    onCancel: () -> Unit,
    onReplay: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!recordPermissionGranted) {
            RecordAudioPermissionBanner()
        }

        CurrentResultCard(state, onCancel, onReplay, onRetry)

        if (turns.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Recent",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth()
            )
            turns.takeLast(3).reversed().forEach { turn ->
                TurnCard(turn)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        BigSpeakButton(
            source = source,
            state = state,
            permissionGranted = recordPermissionGranted,
            onPress = onSpeakPress,
            onRelease = onSpeakRelease
        )
    }
}

@Composable
private fun TabletopLayout(
    source: Language,
    target: Language,
    state: ConversationState,
    turns: List<ConversationTurn>,
    recordPermissionGranted: Boolean = true,
    modifier: Modifier = Modifier,
    onSourceSpeak: () -> Unit,
    onSourceRelease: () -> Unit,
    onTargetSpeak: () -> Unit,
    onTargetRelease: () -> Unit
) {
    Row(modifier = modifier.fillMaxSize()) {
        SpeakerZone(
            language = source,
            state = state,
            turn = turns.lastOrNull { it.sourceLanguage == source || it.targetLanguage == source },
            recordPermissionGranted = recordPermissionGranted,
            onPress = onSourceSpeak,
            onRelease = onSourceRelease,
            modifier = Modifier.weight(1f)
        )

        VerticalDivider()

        SpeakerZone(
            language = target,
            state = state,
            turn = turns.lastOrNull { it.sourceLanguage == target || it.targetLanguage == target },
            recordPermissionGranted = recordPermissionGranted,
            onPress = onTargetSpeak,
            onRelease = onTargetRelease,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun VerticalDivider(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 16.dp)
            .width(1.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    ) {}
}

@Composable
private fun CurrentResultCard(
    state: ConversationState,
    onCancel: () -> Unit,
    onReplay: () -> Unit,
    onRetry: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StatusText(state)

            when (state) {
                is ConversationState.Setup -> Text(
                    text = stringResource(R.string.setup_title),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                is ConversationState.Ready -> Text(
                    text = stringResource(R.string.conversation_hold_to_speak),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                is ConversationState.Recording -> {
                    Text(
                        text = stringResource(R.string.conversation_recording),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                is ConversationState.Transcribing -> {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(R.string.conversation_transcribing),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    state.partial?.let { Text(it.text) }
                }
                is ConversationState.Translating -> {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(R.string.conversation_translating),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(state.transcript.text, style = MaterialTheme.typography.bodyMedium)
                }
                is ConversationState.Speaking -> {
                    val turn = state.turn
                    TurnText(turn)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onCancel) { Text(stringResource(R.string.conversation_stop)) }
                        TextButton(onClick = onReplay) { Text(stringResource(R.string.conversation_replay)) }
                    }
                }
                is ConversationState.Error -> {
                    Text(
                        text = state.error.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onCancel) { Text(stringResource(R.string.conversation_try_again)) }
                        TextButton(onClick = onRetry) { Text(stringResource(R.string.conversation_retry)) }
                    }
                }
                is ConversationState.Warming -> {
                    CircularProgressIndicator()
                    Text(
                        text = state.overall.let {
                            when (it) {
                                is EngineReadiness.Loading -> it.message
                                else -> stringResource(R.string.setup_warm_engines)
                            }
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun TurnText(turn: ConversationTurn) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = turn.transcript.text,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        turn.translation?.let { translation ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = translation.translatedText,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun TurnCard(turn: ConversationTurn) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${turn.sourceLanguage.displayName()} → ${turn.targetLanguage.displayName()}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(text = turn.transcript.text, style = MaterialTheme.typography.bodyMedium)
            turn.translation?.let {
                Text(
                    text = it.translatedText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun BigSpeakButton(
    source: Language,
    state: ConversationState,
    permissionGranted: Boolean = true,
    onPress: () -> Unit,
    onRelease: () -> Unit
) {
    val isRecording = state is ConversationState.Recording && state.sourceLanguage == source
    val label = when {
        !source.supportsStt -> stringResource(R.string.conversation_listening_not_supported)
        isRecording -> stringResource(R.string.conversation_release_to_send)
        else -> stringResource(R.string.conversation_hold_to_speak)
    }
    val a11y = stringResource(R.string.a11y_speak_button, source.displayName)
    val enabled = source.supportsStt && (state is ConversationState.Ready || state is ConversationState.Recording) && permissionGranted

    val currentEnabled by rememberUpdatedState(enabled)
    val currentOnPress by rememberUpdatedState(onPress)
    val currentOnRelease by rememberUpdatedState(onRelease)

    val containerColor = if (isRecording) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = if (isRecording) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .alpha(if (enabled) 1f else 0.38f)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        if (!currentEnabled) return@detectTapGestures
                        currentOnPress()
                        if (tryAwaitRelease()) {
                            currentOnRelease()
                        }
                    }
                )
            }
            .semantics {
                contentDescription = a11y
                role = Role.Button
            },
        shape = MaterialTheme.shapes.extraLarge,
        color = containerColor,
        contentColor = contentColor
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun SpeakerZone(
    language: Language,
    state: ConversationState,
    turn: ConversationTurn?,
    recordPermissionGranted: Boolean = true,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = language.displayLabel(),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (state.isProcessing) {
                StatusText(state)
            } else if (turn != null) {
                TurnText(turn)
            } else {
                Text(
                    text = stringResource(R.string.conversation_hold_to_speak),
                    textAlign = TextAlign.Center
                )
            }
        }

        BigSpeakButton(
            source = language,
            state = state,
            permissionGranted = recordPermissionGranted,
            onPress = onPress,
            onRelease = onRelease
        )
    }
}

@Composable
private fun StatusText(state: ConversationState) {
    val text = when (state) {
        is ConversationState.Setup -> stringResource(R.string.setup_warm_engines)
        is ConversationState.Warming -> stringResource(R.string.setup_warm_engines)
        is ConversationState.Ready -> stringResource(R.string.conversation_hold_to_speak)
        is ConversationState.Recording -> stringResource(R.string.conversation_recording)
        is ConversationState.Transcribing -> stringResource(R.string.conversation_transcribing)
        is ConversationState.Translating -> stringResource(R.string.conversation_translating)
        is ConversationState.Speaking -> stringResource(R.string.conversation_speaking)
        is ConversationState.Error -> stringResource(R.string.conversation_try_again)
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.secondary
    )
}

private fun Language.displayName(): String = this.displayName

@Composable
private fun RecordAudioPermissionBanner() {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Microphone permission is required to translate by voice.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}
