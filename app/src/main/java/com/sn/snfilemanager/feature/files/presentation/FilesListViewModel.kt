package com.sn.snfilemanager.feature.files.presentation

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sn.mediastorepv.data.ConflictStrategy
import com.sn.snfilemanager.core.util.Event
import com.sn.snfilemanager.core.util.RootPath
import com.sn.snfilemanager.feature.files.FileSearchTask
import com.sn.snfilemanager.feature.files.data.FileModel
import com.sn.snfilemanager.feature.files.data.toFileModel
import com.sn.snfilemanager.providers.filepath.FilePathProvider
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
class FilesListViewModel @Inject constructor(
    private val filePathProvider: FilePathProvider
) : ViewModel() {

    private var fileListJob: Job? = null
    private val searchTask = FileSearchTask()
    private var selectedItemList: MutableList<FileModel> = mutableListOf()
    private val directoryList: MutableList<String> = mutableListOf()
    private val handler = Handler(Looper.getMainLooper())

    private var searchRunnable: Runnable? = null
    var currentPath: String? = null
    var isCopy: Boolean = false
    var firstInit = false

    private val _conflictQuestionLiveData: MutableLiveData<Event<File>> =
        MutableLiveData()
    val conflictQuestionLiveData: LiveData<Event<File>> = _conflictQuestionLiveData

    private val _startMoveJobLiveData: MutableLiveData<Event<Pair<List<FileModel>, Path>>> =
        MutableLiveData()
    val startMoveJobLiveData: LiveData<Event<Pair<List<FileModel>, Path>>> = _startMoveJobLiveData

    private val _updateListLiveData: MutableLiveData<Event<List<FileModel>>> = MutableLiveData()
    val updateListLiveData: LiveData<Event<List<FileModel>>> = _updateListLiveData

    var conflictDialogDeferred = CompletableDeferred<Pair<ConflictStrategy, Boolean>>()

    companion object {
        private const val SEARCH_DELAY = 500L
        private const val BATCH_SIZE = 100L
    }

    fun getStoragePath(rootPath: RootPath): String = when (rootPath) {
        RootPath.INTERNAL -> filePathProvider.internalStorageRootPath
        RootPath.EXTERNAL -> filePathProvider.externalStorageRootPath
        else -> filePathProvider.downloadDirectoryPath
    }

    fun updateDirectoryList(path: String) {
        currentPath = path
        if (!directoryList.contains(path))
            directoryList.add(path)
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

    fun addSelectedItem(file: FileModel, selected: Boolean) {
        if (selected) {
            if (file !in selectedItemList) {
                selectedItemList.add(file)
            }
        } else {
            selectedItemList.remove(file)
        }
    }

    fun clearSelectionList() {
        if (selectedItemList.isNotEmpty())
            selectedItemList.clear()
    }

    fun getFilesList(path: String) {
        fileListJob = viewModelScope.launch {
            val directory = Paths.get(path)
            val totalFiles = Files.list(directory).count()
            var processedFiles: Long = 0
            val fileList: MutableList<FileModel> = mutableListOf()

            if (totalFiles == 0L) {
                _updateListLiveData.postValue(Event(emptyList()))
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
                            if (Files.isReadable(file))
                                fileList.add(file.toFileModel())
                        }
                }
                withContext(Dispatchers.Main) {
                    _updateListLiveData.postValue(Event(fileList.toList()))
                }
                processedFiles += currentBatchSize
            }
        }
    }

    fun cancel() {
        if (fileListJob != null && fileListJob?.isActive == true) {
            fileListJob?.cancel()
        }
    }

    fun moveFilesAndDirectories(destinationPath: Path) {
        val operationItemList: MutableList<FileModel> = mutableListOf()
        viewModelScope.launch {
            val job = async {
                for (i in selectedItemList.indices) {
                    val file = selectedItemList[i]
                    val targetPath = destinationPath.resolve(file.name)
                    if (Files.exists(targetPath)) {
                        _conflictQuestionLiveData.postValue(Event(targetPath.toFile()))
                        val result = conflictDialogDeferred.await()
                        conflictDialogDeferred = CompletableDeferred()

                        if (!result.second) {
                            file.conflictStrategy = result.first
                            operationItemList.add(file)
                        } else {
                            if (i < selectedItemList.size - 1) {
                                for (remainingFile in selectedItemList.subList(
                                    i + 1,
                                    selectedItemList.size
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

    private fun removeSearchCallback() {
        searchRunnable?.let { handler.removeCallbacks(it) }
        searchTask.cancelSearch()
    }

    fun searchFiles(query: String?) {
        removeSearchCallback()
        if (query.isNullOrEmpty()) {
            currentPath?.let { getFilesList(it) }
        } else {
            if (query.length > 3) {
                searchRunnable = Runnable {
                    searchTask.search(Paths.get(currentPath), query) { result ->
                        _updateListLiveData.postValue(Event(result.map { it.toFileModel() }))
                    }
                }
                searchRunnable?.let { handler.postDelayed(it, SEARCH_DELAY) }
            }
        }

    }
}