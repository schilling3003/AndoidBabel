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
    private var source: Language = Language.ENGLISH,
    private var target: Language = Language.SPANISH
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<ModelDownloadManager.DownloadTask>>(emptyList())
    val tasks: StateFlow<List<ModelDownloadManager.DownloadTask>> = _tasks

    private val _allLanguageTasks = MutableStateFlow<List<ModelDownloadManager.DownloadTask>>(emptyList())
    val allLanguageTasks: StateFlow<List<ModelDownloadManager.DownloadTask>> = _allLanguageTasks

    private val workLiveData = mutableMapOf<UUID, LiveData<WorkInfo>>()
    private val workObservers = mutableMapOf<UUID, Observer<WorkInfo>>()

    init {
        refresh()
    }

    fun setLanguages(source: Language, target: Language) {
        this.source = source
        this.target = target
        refresh()
    }

    fun refresh() {
        val pairSpecs = downloadManager.requiredSpecs(source, target)
        val allSpecs = downloadManager.allSpecs()
        _tasks.value = pairSpecs.map { spec -> taskFor(spec) }
        _allLanguageTasks.value = allSpecs.map { spec -> taskFor(spec) }
    }

    fun startDownload(task: ModelDownloadManager.DownloadTask) {
        if (task.workState == WorkInfo.State.RUNNING) return
        val workId = downloadManager.startDownload(task.spec)
        update(task.spec.id) { it.copy(workId = workId, workState = WorkInfo.State.ENQUEUED, error = null) }
        observe(workId)
    }

    fun startAll() {
        _tasks.value.filter { !it.isPresent }.forEach { startDownload(it) }
    }

    fun startAllLanguages() {
        _allLanguageTasks.value.filter { !it.isPresent }.forEach { startDownload(it) }
    }

    override fun onCleared() {
        workLiveData.forEach { (id, liveData) ->
            workObservers[id]?.let { liveData.removeObserver(it) }
        }
        workLiveData.clear()
        workObservers.clear()
        super.onCleared()
    }

    private fun taskFor(spec: ModelDownloadManager.DownloadSpec): ModelDownloadManager.DownloadTask {
        val current = (_tasks.value + _allLanguageTasks.value).associateBy { it.spec.id }
        return current[spec.id]?.copy(spec = spec, isPresent = downloadManager.isPresent(spec))
            ?: ModelDownloadManager.DownloadTask(spec, isPresent = downloadManager.isPresent(spec))
    }

    private fun observe(workId: UUID) {
        if (workObservers.containsKey(workId)) return
        val liveData: LiveData<WorkInfo> = downloadManager.workInfo(workId)
        workLiveData[workId] = liveData
        val observer = Observer<WorkInfo> { info ->
            val task = findTask(workId) ?: return@Observer
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

    private fun findTask(workId: UUID): ModelDownloadManager.DownloadTask? =
        (_tasks.value + _allLanguageTasks.value).find { it.workId == workId }

    private fun update(id: String, transform: (ModelDownloadManager.DownloadTask) -> ModelDownloadManager.DownloadTask) {
        _tasks.value = _tasks.value.map { if (it.spec.id == id) transform(it) else it }
        _allLanguageTasks.value = _allLanguageTasks.value.map { if (it.spec.id == id) transform(it) else it }
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
