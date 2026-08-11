package com.schilling3003.relay.viewmodel

import ai.moonshine.voice.MoonshineDownloadWorker
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkInfo
import com.schilling3003.relay.domain.Language
import com.schilling3003.relay.engines.moonshine.ModelDownloadManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class ModelDownloadViewModel(
    private val downloadManager: ModelDownloadManager,
    private val source: Language = Language.ENGLISH,
    private val target: Language = Language.SPANISH
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<ModelDownloadManager.DownloadTask>>(emptyList())
    val tasks: StateFlow<List<ModelDownloadManager.DownloadTask>> = _tasks

    private val workLiveData = mutableMapOf<UUID, LiveData<WorkInfo>>()
    private val workObservers = mutableMapOf<UUID, Observer<WorkInfo>>()

    init {
        refresh()
    }

    fun refresh() {
        val specs = downloadManager.requiredSpecs(source, target)
        val current = _tasks.value.associateBy { it.spec.id }
        _tasks.value = specs.map { spec ->
            current[spec.id]?.copy(isPresent = downloadManager.isPresent(spec))
                ?: ModelDownloadManager.DownloadTask(spec, isPresent = downloadManager.isPresent(spec))
        }
    }

    fun startDownload(task: ModelDownloadManager.DownloadTask) {
        val workId = downloadManager.startDownload(task.spec)
        update(task.spec.id) { it.copy(workId = workId, workState = WorkInfo.State.ENQUEUED, error = null) }
        observe(workId)
    }

    fun startAll() {
        _tasks.value.filter { !it.isPresent }.forEach { startDownload(it) }
    }

    override fun onCleared() {
        workLiveData.forEach { (id, liveData) ->
            workObservers[id]?.let { liveData.removeObserver(it) }
        }
        workLiveData.clear()
        workObservers.clear()
        super.onCleared()
    }

    private fun observe(workId: UUID) {
        if (workObservers.containsKey(workId)) return
        val liveData: LiveData<WorkInfo> = downloadManager.workInfo(workId)
        workLiveData[workId] = liveData
        val observer = Observer<WorkInfo> { info ->
            val task = _tasks.value.find { it.workId == workId } ?: return@Observer
            val progress = if (info.progress.keyValueMap.isNotEmpty()) {
                ai.moonshine.voice.DownloadProgress(
                    info.progress.getString(MoonshineDownloadWorker.PROGRESS_RELATIVE_PATH) ?: "",
                    info.progress.getInt(MoonshineDownloadWorker.PROGRESS_FILE_INDEX, 0),
                    info.progress.getInt(MoonshineDownloadWorker.PROGRESS_TOTAL_FILES, 0),
                    info.progress.getLong(MoonshineDownloadWorker.PROGRESS_BYTES_DOWNLOADED, 0L),
                    info.progress.getLong(MoonshineDownloadWorker.PROGRESS_BYTES_TOTAL, 0L)
                )
            } else null
            val isPresent = if (info.state == WorkInfo.State.SUCCEEDED) {
                downloadManager.isPresent(task.spec)
            } else task.isPresent
            val error = if (info.state == WorkInfo.State.FAILED) {
                info.outputData.getString(MoonshineDownloadWorker.OUTPUT_ERROR)
            } else task.error
            update(task.spec.id) {
                it.copy(workState = info.state, progress = progress, isPresent = isPresent, error = error)
            }
        }
        workObservers[workId] = observer
        liveData.observeForever(observer)
    }

    private fun update(id: String, transform: (ModelDownloadManager.DownloadTask) -> ModelDownloadManager.DownloadTask) {
        _tasks.value = _tasks.value.map { if (it.spec.id == id) transform(it) else it }
    }

    class Factory(
        private val downloadManager: ModelDownloadManager,
        private val source: Language = Language.ENGLISH,
        private val target: Language = Language.SPANISH
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ModelDownloadViewModel(downloadManager, source, target) as T
    }
}
