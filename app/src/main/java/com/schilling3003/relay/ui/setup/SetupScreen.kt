package com.schilling3003.relay.ui.setup

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import com.schilling3003.relay.R
import com.schilling3003.relay.domain.ModelError
import com.schilling3003.relay.domain.ModelState
import com.schilling3003.relay.engines.moonshine.ModelDownloadManager
import com.schilling3003.relay.viewmodel.ModelDownloadViewModel
import com.schilling3003.relay.viewmodel.SetupViewModel

@Composable
fun SetupScreen(
    viewModel: SetupViewModel,
    downloadViewModel: ModelDownloadViewModel,
    onSetupComplete: () -> Unit
) {
    val modelState by viewModel.modelState
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

            ModelStatusCard(modelState)

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
                    onRefresh = { downloadViewModel.refresh() }
                )
            }

            if (modelState is ModelState.Ready) {
                Button(
                    onClick = onSetupComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(text = stringResource(R.string.setup_ready))
                }
            }

            if (modelState is ModelState.Ready) {
                TextButton(onClick = { viewModel.removeModel() }) {
                    Text(text = stringResource(R.string.settings_remove_model))
                }
            }

            importError?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error.userMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics {
                        contentDescription = error.userMessage
                    }
                )
            }
        }
    }
}

@Composable
private fun ModelStatusCard(state: ModelState) {
    val title = when (state) {
        is ModelState.Missing -> stringResource(R.string.setup_model_missing)
        is ModelState.Ready -> stringResource(R.string.setup_model_ready)
        is ModelState.Importing -> state.stage
        is ModelState.Validating -> stringResource(R.string.setup_warm_engines)
        is ModelState.Error -> state.reason.userMessage
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            when (state) {
                is ModelState.Importing -> {
                    LinearProgressIndicator(
                        progress = { state.bytesCopied.toFloat() / state.totalBytes.toFloat() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                is ModelState.Validating -> CircularProgressIndicator()
                else -> {}
            }
        }
    }
}

@Composable
private fun ModelDownloadSection(
    tasks: List<ModelDownloadManager.DownloadTask>,
    onDownload: (ModelDownloadManager.DownloadTask) -> Unit,
    onDownloadAll: () -> Unit,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.setup_voice_models_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.setup_voice_models_subtitle),
                style = MaterialTheme.typography.bodyMedium
            )

            tasks.forEach { task ->
                DownloadTaskRow(task = task, onDownload = { onDownload(task) })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRefresh,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(R.string.setup_refresh))
                }
                Button(
                    onClick = onDownloadAll,
                    modifier = Modifier.weight(1f),
                    enabled = tasks.any { !it.isPresent && it.workState != WorkInfo.State.RUNNING }
                ) {
                    Text(text = stringResource(R.string.setup_download_all))
                }
            }
        }
    }
}

@Composable
private fun DownloadTaskRow(
    task: ModelDownloadManager.DownloadTask,
    onDownload: () -> Unit
) {
    val statusLabel = when {
        task.isPresent -> stringResource(R.string.setup_downloaded)
        task.workState == WorkInfo.State.RUNNING -> stringResource(R.string.setup_downloading)
        task.workState == WorkInfo.State.ENQUEUED || task.workState == WorkInfo.State.BLOCKED -> stringResource(R.string.setup_pending)
        task.error != null -> stringResource(R.string.setup_error)
        else -> stringResource(R.string.setup_pending)
    }

    val progressFraction = task.progress?.let { p ->
        if (p.bytesTotal > 0) p.bytesDownloaded.toFloat() / p.bytesTotal.toFloat() else 0f
    } ?: 0f

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = task.spec.displayName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            when {
                task.isPresent -> {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                task.workState == WorkInfo.State.RUNNING -> {
                    Text(
                        text = "${(progressFraction * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                else -> {
                    OutlinedButton(onClick = onDownload) {
                        Text(text = stringResource(R.string.setup_download))
                    }
                }
            }
        }

        if (task.workState == WorkInfo.State.RUNNING) {
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier.fillMaxWidth()
            )
        }

        task.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
