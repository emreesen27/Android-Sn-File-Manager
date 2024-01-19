package com.sn.snfilemanager.feature.files.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sn.filetaskpv.FileConflictStrategy
import com.sn.filetaskpv.FileOperationCallback
import com.sn.snfilemanager.core.extensions.getMimeType
import com.sn.snfilemanager.core.util.Event
import com.sn.snfilemanager.core.util.RootPath
import com.sn.snfilemanager.feature.files.data.FileModel
import com.sn.snfilemanager.providers.filepath.FilePathProvider
import com.sn.snfilemanager.providers.fileprovider.FileTaskProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject

@HiltViewModel
class FilesListViewModel @Inject constructor(
    private val filePathProvider: FilePathProvider,
    private val fileTaskProvider: FileTaskProvider
) : ViewModel() {

    var currentPath: String? = null
    private var selectedItemList: MutableList<FileModel> = mutableListOf()
    private val directoryList: MutableList<String> = mutableListOf()
    var isCopy: Boolean = false
    var firstInit = false

    private val _conflictQuestionLiveData: MutableLiveData<Event<File>> =
        MutableLiveData()
    val conflictQuestionLiveData: LiveData<Event<File>> = _conflictQuestionLiveData

    private val _moveLiveData: MutableLiveData<Event<List<Pair<String, String?>>>> =
        MutableLiveData()
    val moveLiveData: LiveData<Event<List<Pair<String, String?>>>> = _moveLiveData

    private val _deleteLiveData: MutableLiveData<Event<List<Pair<String, String?>>>> =
        MutableLiveData()
    val deleteLiveData: LiveData<Event<List<Pair<String, String?>>>> = _deleteLiveData

    private val _progressLiveData: MutableLiveData<Event<Int>> = MutableLiveData()
    val progressLiveData: LiveData<Event<Int>> = _progressLiveData

    var conflictDialogDeferred = CompletableDeferred<Pair<FileConflictStrategy, Boolean>>()

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

    fun getFilesList(path: String): List<File> {
        val directory = File(path)
        return directory.listFiles()?.toList() ?: emptyList()
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
        viewModelScope.launch {
            val result = fileTaskProvider.moveFilesAndDirectories(
                sourcePaths = getSelectedItemsPaths(),
                destinationPath = destinationPath,
                isCopy = isCopy,
                callback = object : FileOperationCallback {
                    override fun onProgress(progress: Int) {
                        _progressLiveData.postValue(Event(progress))
                    }

                    override suspend fun fileConflict(file: File): Pair<FileConflictStrategy, Boolean> {
                        _conflictQuestionLiveData.postValue(Event(file))
                        val result = conflictDialogDeferred.await()
                        conflictDialogDeferred = CompletableDeferred()
                        return result
                    }
                }
            )
            result.fold(
                onSuccess = { list ->
                    _moveLiveData.value = Event(mapFilePathsToMimeTypes(list))
                },
                onFailure = { exception ->
                    println("e:$exception")
                }
            )
        }
    }

    fun deleteFilesAndDirectories() {
        viewModelScope.launch {
            val result = fileTaskProvider.deleteFilesAndDirectories(getSelectedItemsPaths())
            result.fold(
                onSuccess = { deletedList ->
                    _deleteLiveData.value = Event(mapFilePathsToMimeTypes(deletedList))
                },
                onFailure = { exception ->
                    println("e$exception")
                }
            )
        }
    }

    private fun getSelectedItemsPaths(): List<Path> =
        selectedItemList.map { Paths.get(it.absolutePath) }

    private fun mapFilePathsToMimeTypes(fileList: List<String>): List<Pair<String, String?>> {
        return fileList.map {
            it to File(it).absolutePath.getMimeType()
        }.toMutableList()
    }
}