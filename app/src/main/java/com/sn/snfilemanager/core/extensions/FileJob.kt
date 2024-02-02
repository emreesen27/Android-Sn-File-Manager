package com.sn.snfilemanager.core.extensions

import android.media.MediaScannerConnection
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.CATEGORY_PROGRESS
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.base.BaseJob

fun BaseJob.postNotification(
    @StringRes title: Int,
    progress: Int,
) {
    val notificationBuilder =
        NotificationCompat.Builder(service, "FileOperationChannel")
    val notification =
        notificationBuilder.setContentTitle("File Operation Service")
            .setContentText(service.getString(title))
            .setSmallIcon(R.drawable.ic_app_icon)
            .setPriority(PRIORITY_HIGH)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(CATEGORY_PROGRESS)
            .setProgress(100, progress, false)
            .build()
    service.notificationManager.notify(id, notification)
}

fun BaseJob.scanFile(
    path: List<String>,
    callback: ((String) -> Unit)? = null,
) {
    MediaScannerConnection.scanFile(
        service,
        path.toTypedArray(),
        path.map { it.getMimeType() }.toTypedArray(),
    ) { scanPath, _ ->
        callback?.invoke(scanPath)
    }
}
