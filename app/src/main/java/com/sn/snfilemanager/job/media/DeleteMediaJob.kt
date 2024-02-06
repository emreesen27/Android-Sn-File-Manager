package com.sn.snfilemanager.job.media

import com.sn.mediastorepv.data.Media
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.base.BaseJob
import com.sn.snfilemanager.core.extensions.infoToast
import com.sn.snfilemanager.core.extensions.postNotification
import com.sn.snfilemanager.job.JobCompletedCallback
import com.sn.snfilemanager.job.JobType

class DeleteMediaJob(
    private val sourcesMedia: List<Media>,
    private val callback: JobCompletedCallback,
) : BaseJob() {
    private var deletedCount: Int = 0
    private val totalItemCount = sourcesMedia.size

    override fun run() {
        handler.post { service.infoToast(service.getString(R.string.deleting)) }
        deleteMedia()
    }

    override fun onCompleted() {
        callback.jobOnCompleted(JobType.DELETE, sourcesMedia)
    }

    private fun deleteMedia() {
        for (media in sourcesMedia) {
            val deleteResult = media.uri.let { service.contentResolver.delete(it, null, null) }
            if (deleteResult != 0) {
                deletedCount++
                updateProgress()
            }
        }
    }

    private fun updateProgress() {
        val progress = ((deletedCount.toDouble() / totalItemCount.toDouble()) * 100).toInt()
        postNotification(R.string.delete, progress)
    }
}
