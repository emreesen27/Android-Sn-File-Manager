package com.sn.snfilemanager.feature.files

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path

class FileSearchTask {
    private var job: Job? = null
    fun search(path: Path, fileName: String, callback: (List<Path>) -> Unit) {
        job = CoroutineScope(Dispatchers.IO).launch {
            val results = mutableListOf<Path>()
            searchFiles(path, fileName, results)
            withContext(Dispatchers.Main) {
                callback(results)
            }
        }
    }

    fun cancelSearch() {
        job?.cancel()
    }

    private fun searchFiles(path: Path, fileName: String, results: MutableList<Path>) {
        if (!Files.isDirectory(path) && Files.isReadable(path)) {
            if (path.fileName.toString().contains(fileName)) {
                results.add(path)
            }
            return
        }

        Files.list(path).use { stream ->
            stream.forEach { subPath ->
                if (Files.isReadable(subPath))
                    searchFiles(subPath, fileName, results)
            }
        }
    }
}