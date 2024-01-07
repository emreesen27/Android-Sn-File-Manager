package com.sn.snfilemanager.core.extensions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.TransactionTooLargeException
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.emreesen.sntoast.SnToast
import com.emreesen.sntoast.Type
import com.sn.snfilemanager.BuildConfig
import com.sn.snfilemanager.R
import java.io.File

fun Context.toast(msg: String, type: Type) {
    SnToast.Builder().context(this).type(type).message(msg)
        .backgroundColor(R.color.main_color).textSize(15)
        .typeface(ResourcesCompat.getFont(applicationContext, R.font.adamina)).build()
}

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

fun Context.openFileWithOtherApp(filePath: String, fileType: String) {
    val file = File(filePath)
    val uri = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.provider", file)

    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(uri, fileType)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

    try {
        if (intent.resolveActivity(this.packageManager) != null) {
            startActivity(Intent.createChooser(intent, getString(R.string.open_with)))
        } else {
            toast(getString(R.string.no_app_open_with), Type.INFORMATION)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


fun Context.shareFiles(uris: List<Uri>): Boolean {
    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
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
        toast(getString(R.string.no_app_share), Type.INFORMATION)
        false
    } catch (e: Exception) {
        if (e.cause is TransactionTooLargeException) {
            toast(getString(R.string.max_share_file), Type.INFORMATION)
        } else {
            e.message?.let { toast(it, Type.ERROR) }
        }
        false
    }
}