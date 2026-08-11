package com.schilling3003.relay.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.schilling3003.relay.R
import com.schilling3003.relay.domain.EngineReadiness
import com.schilling3003.relay.domain.ModelState
import com.schilling3003.relay.ui.components.AllLanguagesDownloadSection
import com.schilling3003.relay.ui.components.ErrorText
import com.schilling3003.relay.ui.components.LanguageDropdown
import com.schilling3003.relay.ui.components.ModelStatusCard
import com.schilling3003.relay.viewmodel.ConversationViewModel
import com.schilling3003.relay.viewmodel.ModelDownloadViewModel
import com.schilling3003.relay.viewmodel.SetupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    setupViewModel: SetupViewModel,
    downloadViewModel: ModelDownloadViewModel,
    conversationViewModel: ConversationViewModel,
    onBack: () -> Unit
) {
    val modelState by setupViewModel.modelState
    val engineReadiness by setupViewModel.engineReadiness
    val importError by setupViewModel.importError
    val source by conversationViewModel.sourceLanguage
    val target by conversationViewModel.targetLanguage
    val tabletop by conversationViewModel.tabletopMode
    val allTasks by downloadViewModel.allLanguageTasks.collectAsStateWithLifecycle()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { setupViewModel.importModel(it) }
    }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            ModelStatusCard(state = modelState, engineReadiness = engineReadiness)

            Button(
                onClick = { launcher.launch("*/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = modelState !is ModelState.Importing && modelState !is ModelState.Validating
            ) {
                Text(text = stringResource(R.string.setup_import_model))
            }

            if (modelState is ModelState.Ready) {
                TextButton(
                    onClick = { setupViewModel.removeModel() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.settings_remove_model))
                }
            }

            importError?.let { error ->
                ErrorText(message = error.userMessage)
            }

            LanguageSection(
                source = source,
                target = target,
                onSourceSelected = { language ->
                    conversationViewModel.setSource(language)
                    downloadViewModel.setLanguages(
                        conversationViewModel.sourceLanguage.value,
                        conversationViewModel.targetLanguage.value
                    )
                },
                onTargetSelected = { language ->
                    conversationViewModel.setTarget(language)
                    downloadViewModel.setLanguages(
                        conversationViewModel.sourceLanguage.value,
                        conversationViewModel.targetLanguage.value
                    )
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_tabletop_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.settings_tabletop_subtitle),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Switch(
                    checked = tabletop,
                    onCheckedChange = { conversationViewModel.toggleTabletopMode() }
                )
            }

            AllLanguagesDownloadSection(
                tasks = allTasks,
                onDownload = { downloadViewModel.startDownload(it) },
                onDownloadAll = { downloadViewModel.startAllLanguages() },
                onRefresh = { downloadViewModel.refresh() }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LanguageSection(
    source: com.schilling3003.relay.domain.Language,
    target: com.schilling3003.relay.domain.Language,
    onSourceSelected: (com.schilling3003.relay.domain.Language) -> Unit,
    onTargetSelected: (com.schilling3003.relay.domain.Language) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_languages),
            style = MaterialTheme.typography.titleMedium
        )
        LanguageDropdown(
            label = stringResource(R.string.settings_source_language),
            selected = source,
            onSelected = onSourceSelected,
            exclude = target,
            modifier = Modifier.fillMaxWidth()
        )
        LanguageDropdown(
            label = stringResource(R.string.settings_target_language),
            selected = target,
            onSelected = onTargetSelected,
            exclude = source,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
