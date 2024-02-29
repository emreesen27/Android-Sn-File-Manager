package com.sn.snfilemanager.core.extensions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.TransactionTooLargeException
import android.widget.Toast
import androidx.core.content.FileProvider
import com.sn.snfilemanager.BuildConfig
import com.sn.snfilemanager.R
import es.dmoral.toasty.Toasty
import java.io.File

fun Context.getUrisForFile(fileList: List<File>): List<Uri> {
    return fileList.map { file ->
        FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.provider", file)
    }
}

fun Context.infoToast(msg: String) {
    Toasty.custom(this, msg, R.drawable.ic_info, R.color.main_color, Toast.LENGTH_SHORT, true, true)
        .show()
}

fun Context.warningToast(msg: String) {
    Toasty.custom(
        this,
        msg,
        R.drawable.ic_info,
        R.color.orange_folder_secondary,
        Toast.LENGTH_SHORT,
        true,
        true,
    ).show()
}

fun Context.errorToast(msg: String) {
    Toasty.custom(this, msg, R.drawable.ic_error, R.color.soft_red, Toast.LENGTH_SHORT, true, true)
        .show()
}

fun Context.openFile(
    filePath: String,
    fileType: String?,
) {
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

fun Context.openFileWithOtherApp(
    filePath: String,
    fileType: String?,
) {
    val file = File(filePath)
    val uri = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.provider", file)

    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(uri, fileType)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

    try {
        if (intent.resolveActivity(this.packageManager) != null) {
            startActivity(Intent.createChooser(intent, getString(R.string.open_with)))
        } else {
            infoToast(getString(R.string.no_app_open_with))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context.shareFiles(uris: List<Uri>): Boolean {
    val intent =
        Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

    val chooser = Intent.createChooser(intent, getString(R.string.share))
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    return try {
        startActivity(chooser)
        true
    } catch (e: ActivityNotFoundException) {
        infoToast(getString(R.string.no_app_share))
        false
    } catch (e: Exception) {
        if (e.cause is TransactionTooLargeException) {
            infoToast(getString(R.string.max_share_file))
        } else {
            e.message?.let { errorToast(it) }
        }
        false
    }
}

fun Context.startActivitySafely(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        // Todo
    }
}

fun Context.openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivitySafely(intent)
}

fun Context.getPackage(): String = "package:${this.packageName}"
