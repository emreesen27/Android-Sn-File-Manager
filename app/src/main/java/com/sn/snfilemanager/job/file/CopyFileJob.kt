package com.sn.snfilemanager.job.file

import com.sn.mediastorepv.data.ConflictStrategy
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.base.BaseJob
import com.sn.snfilemanager.core.extensions.getUniqueFileNameWithCounter
import com.sn.snfilemanager.core.extensions.infoToast
import com.sn.snfilemanager.core.extensions.postNotification
import com.sn.snfilemanager.core.extensions.scanFile
import com.sn.snfilemanager.feature.files.data.FileModel
import com.sn.snfilemanager.job.JobCompletedCallback
import com.sn.snfilemanager.job.JobType
import java.io.IOException
import java.nio.file.FileVisitOption
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.EnumSet

class CopyFileJob(
    private val sourceFiles: List<FileModel>,
    private val targetPath: Path,
    private val isCopy: Boolean,
    private val completed: JobCompletedCallback,
) : BaseJob() {
    private var movedItemCount: Int = 0
    private var title = if (isCopy) R.string.copying else R.string.moving
    private val totalItemCount: Long = calculateItemCount(sourceFiles)
    private val movedItemPathList: MutableList<String> = mutableListOf()

    override fun run() {
        handler.post { service.infoToast(service.getString(title)) }
        moveFilesAndDirectories()
    }

    override fun onCompleted() {
        completed.jobOnCompleted<Nothing>(JobType.COPY, null)
        scanFile(movedItemPathList) {
            completed.scannedOnCompleted()
        }
    }

    private fun moveFilesAndDirectories() {
        for (sourceFile in sourceFiles) {
            val sourcePath = Paths.get(sourceFile.absolutePath)
            var targetPath = targetPath.resolve(sourcePath.fileName)
            if (Files.exists(targetPath)) {
                if (sourceFile.conflictStrategy == ConflictStrategy.SKIP) {
                    continue
                }
                if (sourceFile.conflictStrategy == ConflictStrategy.KEEP_BOTH) {
                    targetPath = targetPath.getUniqueFileNameWithCounter()
                }
                if (Files.isDirectory(sourcePath)) {
                    moveDirectoryContents(sourcePath, targetPath, isCopy)
                } else {
                    copyOrMoveFile(sourcePath, targetPath, isCopy)
                }
            } else {
                if (Files.isDirectory(sourcePath)) {
                    moveDirectoryContents(sourcePath, targetPath, isCopy)
                } else {
                    copyOrMoveFile(sourcePath, targetPath, isCopy)
                }
            }
        }
    }

    private fun moveDirectoryContents(
        sourcePath: Path,
        targetPath: Path,
        isCopy: Boolean,
    ) {
        Files.walkFileTree(
            sourcePath,
            EnumSet.noneOf(FileVisitOption::class.java),
            Int.MAX_VALUE,
            object : SimpleFileVisitor<Path>() {
                override fun visitFile(
                    file: Path,
                    attrs: BasicFileAttributes,
                ): FileVisitResult {
                    val targetFile = targetPath.resolve(sourcePath.relativize(file))
                    copyOrMoveFile(file, targetFile, isCopy)
                    return FileVisitResult.CONTINUE
                }

                override fun preVisitDirectory(
                    dir: Path,
                    attrs: BasicFileAttributes,
                ): FileVisitResult {
                    val targetDir = targetPath.resolve(sourcePath.relativize(dir))
                    Files.createDirectories(targetDir)
                    return FileVisitResult.CONTINUE
                }

                override fun visitFileFailed(
                    file: Path,
                    exc: IOException,
                ): FileVisitResult {
                    return FileVisitResult.CONTINUE
                }
            },
        )
    }

    private fun copyOrMoveFile(
        source: Path,
        target: Path,
        isCopy: Boolean,
    ) {
        movedItemPathList.add(source.toFile().absolutePath)
        movedItemPathList.add(target.toFile().absolutePath)

        Files.newInputStream(source).use { input ->
            Files.newOutputStream(target).use { output ->
                input.copyTo(output)
            }
        }
        movedItemCount++
        updateProgress()

        if (!isCopy) {
            Files.deleteIfExists(source)
        }
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
        val progress = ((movedItemCount.toDouble() / totalItemCount.toDouble()) * 100).toInt()
        postNotification(title, progress)
    }
}
