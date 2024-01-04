package com.sn.snfilemanager.core.extensions

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.sn.snfilemanager.BuildConfig
import java.io.File

fun Context.openFile(filePath: String, fileType: String) {
    val file = File(filePath)
    val uri = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.provider", file)

    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(uri, fileType)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    try {
        startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
