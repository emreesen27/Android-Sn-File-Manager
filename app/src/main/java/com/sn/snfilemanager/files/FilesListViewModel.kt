package com.sn.snfilemanager.files

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class FilesListViewModel @Inject constructor() : ViewModel() {

    private val directoryList: MutableList<String> = mutableListOf()

    fun updateDirectoryList(directoryPath: String) {
        if (!directoryList.contains(directoryPath))
            directoryList.add(directoryPath)
    }

    fun getFilesList(directoryPath: String): List<File> {
        val directory = File(directoryPath)
        return directory.listFiles()?.toList() ?: emptyList()
    }

    fun getDirectoryList() = directoryList

}