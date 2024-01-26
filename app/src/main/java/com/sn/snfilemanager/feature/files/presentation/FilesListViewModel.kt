package com.sn.snfilemanager.feature.files.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sn.mediastorepv.data.ConflictStrategy
import com.sn.snfilemanager.core.util.Event
import com.sn.snfilemanager.core.util.RootPath
import com.sn.snfilemanager.feature.files.data.FileModel
import com.sn.snfilemanager.providers.filepath.FilePathProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject
import kotlin.streams.toList

@HiltViewModel
class FilesListViewModel @Inject constructor(
    private val filePathProvider: FilePathProvider
) : ViewModel() {

    var currentPath: String? = null
    private var selectedItemList: MutableList<FileModel> = mutableListOf()
    private val directoryList: MutableList<String> = mutableListOf()
    var isCopy: Boolean = false
    var firstInit = false

    private val _conflictQuestionLiveData: MutableLiveData<Event<File>> =
        MutableLiveData()
    val conflictQuestionLiveData: LiveData<Event<File>> = _conflictQuestionLiveData

    private val _startMoveJobLiveData: MutableLiveData<Event<Pair<List<FileModel>, Path>>> =
        MutableLiveData()
    val startMoveJobLiveData: LiveData<Event<Pair<List<FileModel>, Path>>> = _startMoveJobLiveData

    private val _progressLiveData: MutableLiveData<Event<Int>> = MutableLiveData()
    val progressLiveData: LiveData<Event<Int>> = _progressLiveData

    var conflictDialogDeferred = CompletableDeferred<Pair<ConflictStrategy, Boolean>>()

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

    fun getFilesList(path: String): List<Path> {
        val directory = Paths.get(path)
        return Files.list(directory).toList()
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
}