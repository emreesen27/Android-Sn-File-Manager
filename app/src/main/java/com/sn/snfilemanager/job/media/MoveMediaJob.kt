package com.sn.snfilemanager.job.media

import android.net.Uri
import com.sn.mediastorepv.data.ConflictStrategy
import com.sn.mediastorepv.data.Media
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.base.BaseJob
import com.sn.snfilemanager.core.extensions.getUniqueFileNameWithCounter
import com.sn.snfilemanager.core.extensions.infoToast
import com.sn.snfilemanager.core.extensions.postNotification
import com.sn.snfilemanager.core.extensions.scanFile
import com.sn.snfilemanager.job.JobCompletedCallback
import com.sn.snfilemanager.job.JobType
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class MoveMediaJob(
    private val sourcesMedia: List<Media>,
    private val targetPath: Path,
    private val isCopy: Boolean,
    private val callback: JobCompletedCallback,
) : BaseJob() {
    private var movedItemCount: Int = 0
    private var scannedItemCount: Int = 0
    private var title = if (isCopy) R.string.copying else R.string.moving
    private val totalItemCount: Int = sourcesMedia.size
    private val movedItemPathList: MutableList<String> = mutableListOf()

    override fun run() {
        handler.post { service.infoToast(service.getString(title)) }
        moveMedia()
    }

    override fun onCompleted() {
        callback.jobOnCompleted<Nothing>(JobType.COPY, null)
        scanFile(movedItemPathList) {
            scannedItemCount++
            if (movedItemPathList.size == scannedItemCount) {
                callback.scannedOnCompleted()
            }
        }
    }

    private fun moveMedia() {
        for (media in sourcesMedia) {
            val sourcePath = Paths.get(media.data)
            var targetPath = targetPath.resolve(sourcePath.fileName)

            if (Files.exists(targetPath)) {
                if (media.conflictStrategy == ConflictStrategy.SKIP) {
                    continue
                } else if (media.conflictStrategy == ConflictStrategy.KEEP_BOTH) {
                    targetPath = targetPath.getUniqueFileNameWithCounter()
                }
            }

            val inputStream = service.contentResolver.openInputStream(media.uri)
            val destinationUri = Uri.fromFile(targetPath.toFile())
            val outputStream = service.contentResolver.openOutputStream(destinationUri)

            inputStream?.use { input ->
                outputStream?.use { output ->
                    input.copyTo(output)
                }
            }
            movedItemCount++
            updateProgress()

            movedItemPathList.add(sourcePath.toFile().absolutePath)
            movedItemPathList.add(targetPath.toFile().absolutePath)

            if (!isCopy) {
                service.contentResolver.delete(media.uri, null, null)
            }
        }
    }

    private fun updateProgress() {
        val progress = ((movedItemCount.toDouble() / totalItemCount.toDouble()) * 100).toInt()
        val title = if (isCopy) R.string.copy_key else R.string.move
        postNotification(title, progress)
    }
}
