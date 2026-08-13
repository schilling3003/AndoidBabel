package com.schilling3003.relay.ui.setup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.schilling3003.relay.R
import com.schilling3003.relay.domain.EngineReadiness
import com.schilling3003.relay.domain.ModelState
import com.schilling3003.relay.ui.components.ErrorText
import com.schilling3003.relay.ui.components.ModelDownloadSection
import com.schilling3003.relay.ui.components.ModelStatusCard
import com.schilling3003.relay.viewmodel.ModelDownloadViewModel
import com.schilling3003.relay.viewmodel.SetupViewModel

@Composable
fun SetupScreen(
    viewModel: SetupViewModel,
    downloadViewModel: ModelDownloadViewModel,
    onSetupComplete: () -> Unit,
    onSettings: () -> Unit
) {
    val modelState by viewModel.modelState
    val engineReadiness by viewModel.engineReadiness
    val importError by viewModel.importError
    val downloadTasks by downloadViewModel.tasks.collectAsStateWithLifecycle()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.importModel(it) }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.setup_title),
                style = MaterialTheme.typography.headlineLarge
            )

            ModelStatusCard(state = modelState, engineReadiness = engineReadiness)

            Text(
                text = stringResource(R.string.setup_model_requirements),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

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
                ModelDownloadSection(
                    tasks = downloadTasks,
                    onDownload = { downloadViewModel.startDownload(it) },
                    onDownloadAll = { downloadViewModel.startAll() },
                    onRefresh = { downloadViewModel.refresh() },
                    title = stringResource(R.string.setup_voice_models_title),
                    subtitle = stringResource(R.string.setup_voice_models_subtitle)
                )

                TextButton(onClick = onSettings) {
                    Text(text = stringResource(R.string.setup_more_languages))
                }

                val engineReady = engineReadiness is EngineReadiness.Ready
                Button(
                    onClick = onSetupComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = engineReady
                ) {
                    Text(
                        text = if (engineReady) {
                            stringResource(R.string.setup_ready)
                        } else {
                            stringResource(R.string.setup_warming_engine)
                        }
                    )
                }

                TextButton(onClick = { viewModel.removeModel() }) {
                    Text(text = stringResource(R.string.settings_remove_model))
                }

                TextButton(onClick = onSetupComplete) {
                    Text(text = stringResource(R.string.setup_skip_downloads))
                }
            }

            importError?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                ErrorText(message = error.userMessage)
            }
        }
    }
}
