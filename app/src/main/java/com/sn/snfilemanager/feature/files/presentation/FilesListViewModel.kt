package com.sn.snfilemanager.feature.files.presentation

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sn.mediastorepv.data.ConflictStrategy
import com.sn.mediastorepv.data.MediaType
import com.sn.snfilemanager.core.base.BaseResult
import com.sn.snfilemanager.core.util.Config
import com.sn.snfilemanager.core.util.Event
import com.sn.snfilemanager.core.util.RootPath
import com.sn.snfilemanager.feature.files.data.FileModel
import com.sn.snfilemanager.feature.files.data.toFileModel
import com.sn.snfilemanager.providers.filepath.FilePathProvider
import com.sn.snfilemanager.providers.mediastore.MediaStoreProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Long.min
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject

@HiltViewModel
class FilesListViewModel
    @Inject
    constructor(
        private val filePathProvider: FilePathProvider,
        private val mediaStoreProvider: MediaStoreProvider,
    ) : ViewModel() {
        private var fileListJob: Job? = null
        private var selectedItemList: MutableList<FileModel> = mutableListOf()
        private val directoryList: MutableList<String> = mutableListOf()
        private val handler = Handler(Looper.getMainLooper())

        private var searchRunnable: Runnable? = null
        var currentPath: String? = null
        var isCopy: Boolean = false
        var firstInit = false

        private val _conflictQuestionLiveData: MutableLiveData<Event<String>> =
            MutableLiveData()
        val conflictQuestionLiveData: LiveData<Event<String>> = _conflictQuestionLiveData

        private val _pathConflictLiveData: MutableLiveData<Event<String>> = MutableLiveData()
        val pathConflictLiveData: LiveData<Event<String>> = _pathConflictLiveData

        private val _startMoveJobLiveData: MutableLiveData<Event<Pair<List<FileModel>, Path>>> =
            MutableLiveData()
        val startMoveJobLiveData: LiveData<Event<Pair<List<FileModel>, Path>>> = _startMoveJobLiveData

        private val _startDeleteJobLiveData: MutableLiveData<Event<List<FileModel>>> =
            MutableLiveData()
        val startDeleteJobLiveData: LiveData<Event<List<FileModel>>> = _startDeleteJobLiveData

        private val _updateListLiveData: MutableLiveData<Event<MutableList<FileModel>>> =
            MutableLiveData()
        val updateListLiveData: LiveData<Event<MutableList<FileModel>>> = _updateListLiveData

        private val _searchStateLiveData: MutableLiveData<Event<Pair<Boolean, Boolean>>> =
            MutableLiveData()
        val searchStateLiveData: LiveData<Event<Pair<Boolean, Boolean>>> = _searchStateLiveData

        private val _startCreateFolderJob: MutableLiveData<Event<Path>> = MutableLiveData()
        val startCreateFolderJob: LiveData<Event<Path>> = _startCreateFolderJob

        var conflictDialogDeferred = CompletableDeferred<Pair<ConflictStrategy, Boolean>>()

        companion object {
            private const val SEARCH_DELAY = 500L
            private const val BATCH_SIZE = 100L
        }

        fun getStoragePath(rootPath: RootPath): String =
            when (rootPath) {
                RootPath.INTERNAL -> filePathProvider.internalStorageRootPath
                RootPath.EXTERNAL -> filePathProvider.externalStorageRootPath
            }

        fun updateDirectoryList(path: String) {
            currentPath = path
            if (!directoryList.contains(path)) {
                directoryList.add(path)
            }
        }

        fun updateDirectoryListWithPos(position: Int) {
            if (position >= 0 && position < directoryList.size) {
                directoryList.subList(position + 1, directoryList.size).clear()
            }
        }

        fun getDirectoryList() = directoryList

        fun getSelectedItem() = selectedItemList

        fun getSelectedItemToFiles(): List<File> = selectedItemList.map { File(it.absolutePath) }

        fun selectedItemsContainsFolder(): Boolean = selectedItemList.any { it.isDirectory }

        fun isSingleItemSelected(): Boolean = selectedItemList.size == 1

        fun addSelectedItem(
            file: FileModel,
            selected: Boolean,
        ) {
            if (selected) {
                if (file !in selectedItemList) {
                    selectedItemList.add(file)
                }
            } else {
                selectedItemList.remove(file)
            }
        }

        fun clearSelectionList() {
            if (selectedItemList.isNotEmpty()) {
                selectedItemList.clear()
            }
        }

        fun getFilesList(path: String) {
            fileListJob =
                viewModelScope.launch {
                    val directory = Paths.get(path)
                    val totalFiles = Files.list(directory).count()
                    var processedFiles: Long = 0
                    val fileList: MutableList<FileModel> = mutableListOf()

                    if (totalFiles == 0L) {
                        _updateListLiveData.postValue(Event(mutableListOf()))
                        return@launch
                    }

                    while (processedFiles < totalFiles) {
                        val remainingFiles = totalFiles - processedFiles
                        val currentBatchSize = min(remainingFiles, BATCH_SIZE)
                        withContext(Dispatchers.IO) {
                            Files.list(directory)
                                .skip(processedFiles)
                                .limit(currentBatchSize)
                                .forEach { file ->
                                    if (Files.isReadable(file) && (
                                            Config.hiddenFile ||
                                                !Files.isHidden(
                                                    file,
                                                )
                                        )
                                    ) {
                                        fileList.add(file.toFileModel())
                                    }
                                }
                        }
                        withContext(Dispatchers.Main) {
                            _updateListLiveData.postValue(Event(fileList))
                        }
                        processedFiles += currentBatchSize
                    }
                }
        }

        fun setUpdateList(files: MutableList<FileModel>) {
            _updateListLiveData.postValue(Event(files))
        }

        fun cancelFileListJob() {
            if (fileListJob != null && fileListJob?.isActive == true) {
                fileListJob?.cancel()
            }
        }

        // Todo check free space
        fun moveFilesAndDirectories(destinationPath: Path) {
            val operationItemList: MutableList<FileModel> = mutableListOf()
            viewModelScope.launch {
                val job =
                    async {
                        for (i in selectedItemList.indices) {
                            val file = selectedItemList[i]
                            val targetPath = destinationPath.resolve(file.name)

                            if (targetPath.parent.startsWith(Paths.get(file.absolutePath))) {
                                _pathConflictLiveData.postValue(Event(file.name))
                                continue
                            }

                            if (Files.exists(targetPath)) {
                                _conflictQuestionLiveData.postValue(Event(file.name))
                                val result = conflictDialogDeferred.await()
                                conflictDialogDeferred = CompletableDeferred()

                                if (!result.second) {
                                    file.conflictStrategy = result.first
                                    operationItemList.add(file)
                                } else {
                                    if (i < selectedItemList.size - 1) {
                                        for (remainingFile in selectedItemList.subList(
                                            i + 1,
                                            selectedItemList.size,
                                        )) {
                                            remainingFile.conflictStrategy = result.first
                                            operationItemList.add(remainingFile)
                                        }
                                    }
                                    break
                                }
                            } else {
                                operationItemList.add(file)
                            }
                        }
                    }
                job.await()
                _startMoveJobLiveData.postValue(Event(Pair(operationItemList, destinationPath)))
            }
        }

        // Todo check free space
        fun createFolder(targetPath: Path) {
            _startCreateFolderJob.value = Event(targetPath)
        }

        fun deleteFiles() {
            val operationItemList: List<FileModel> = selectedItemList.toList()
            _startDeleteJobLiveData.postValue(Event(operationItemList))
        }

        private fun removeSearchCallback() {
            searchRunnable?.let { handler.removeCallbacks(it) }
        }

        fun searchFiles(query: String?) {
            removeSearchCallback()
            _searchStateLiveData.postValue(Event(Pair(true, false)))

            if (query.isNullOrEmpty()) {
                currentPath?.let { getFilesList(it) }
                _searchStateLiveData.postValue(Event(Pair(false, false)))
                return
            }

            if (query.length < 3) {
                _searchStateLiveData.postValue(Event(Pair(true, false)))
                return
            }

            _searchStateLiveData.postValue(Event(Pair(true, true)))

            searchRunnable =
                Runnable {
                    currentPath?.let { path ->
                        viewModelScope.launch {
                            when (
                                val result =
                                    mediaStoreProvider.searchInPath(
                                        query,
                                        path,
                                        MediaType.FILES,
                                        MediaType.VIDEOS,
                                        MediaType.IMAGES,
                                        MediaType.AUDIOS,
                                    )
                            ) {
                                is BaseResult.Success -> {
                                    val list = result.data.map { Paths.get(it).toFileModel() }
                                    _searchStateLiveData.postValue(Event(Pair(false, false)))
                                    _updateListLiveData.postValue(Event(list.toMutableList()))
                                }

                                is BaseResult.Failure -> {
                                    // Handle failure
                                }
                            }
                        }
                    }
                }
            searchRunnable?.let { handler.postDelayed(it, SEARCH_DELAY) }
        }
    }
