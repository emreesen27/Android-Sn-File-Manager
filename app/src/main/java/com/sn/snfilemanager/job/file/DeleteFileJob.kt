package com.sn.snfilemanager.job.file

import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.base.BaseJob
import com.sn.snfilemanager.core.extensions.infoToast
import com.sn.snfilemanager.core.extensions.postNotification
import com.sn.snfilemanager.core.extensions.scanFile
import com.sn.snfilemanager.feature.files.data.FileModel
import com.sn.snfilemanager.job.JobCompletedCallback
import com.sn.snfilemanager.job.JobType
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

class DeleteFileJob(
    private val sourceFiles: List<FileModel>,
    private val completed: JobCompletedCallback,
) : BaseJob() {
    private var deletedCount: Long = 0
    private val totalItemCount = calculateItemCount(sourceFiles)
    private val deletedItemPathList: MutableList<String> = mutableListOf()

    override fun run() {
        handler.post { service.infoToast(service.getString(R.string.deleting)) }
        delete()
    }

    override fun onCompleted() {
        completed.jobOnCompleted(JobType.DELETE, sourceFiles)
        scanFile(deletedItemPathList)
    }

    private fun delete() {
        sourceFiles.forEach { file ->
            val path = Paths.get(file.absolutePath)
            if (Files.exists(path)) {
                deleteFilesAndDirectories(path)
            }
        }
    }

    private fun deleteFilesAndDirectories(path: Path) {
        Files.walkFileTree(
            path,
            object : SimpleFileVisitor<Path>() {
                override fun visitFile(
                    file: Path,
                    attrs: BasicFileAttributes,
                ): FileVisitResult {
                    if (Files.deleteIfExists(file)) {
                        deletedCount++
                        updateProgress()
                        deletedItemPathList.add(file.toFile().absolutePath)
                    }
                    return FileVisitResult.CONTINUE
                }

                override fun visitFileFailed(
                    file: Path,
                    exc: java.io.IOException,
                ): FileVisitResult {
                    return FileVisitResult.CONTINUE
                }

                override fun postVisitDirectory(
                    dir: Path,
                    exc: java.io.IOException?,
                ): FileVisitResult {
                    if (Files.deleteIfExists(dir)) {
                        updateProgress()
                        deletedCount++
                    }
                    return FileVisitResult.CONTINUE
                }
            },
        )
    }

    private fun calculateItemCount(items: List<FileModel>): Long {
        return items.sumOf {
            if (Files.isDirectory(Paths.get(it.absolutePath))) {
                Files.walk(
                    Paths.get(
                        it.absolutePath,
                    ),
                ).count()
            } else {
                1
            }
        }
    }

    private fun updateProgress() {
        val progress = ((deletedCount.toDouble() / totalItemCount.toDouble()) * 100).toInt()
        postNotification(R.string.delete, progress)
    }
}
