package com.sn.snfilemanager.core.extensions

import android.webkit.MimeTypeMap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun String.getDirectoryNameFromPath(): String {
    val lastSeparatorIndex = this.lastIndexOf("/")
    return if (lastSeparatorIndex != -1) {
        this.substring(0, lastSeparatorIndex)
    } else {
        this
    }
}

fun String.getFileExtension(): String? {
    val lastDotIndex = this.lastIndexOf(".")
    if (lastDotIndex >= 0) {
        return this.substring(lastDotIndex + 1)
    }
    return null
}

fun String.getMimeType(): String? {
    val extension = MimeTypeMap.getFileExtensionFromUrl(this)
    val ext = if (extension.isNullOrEmpty()) this.getFileExtension() else extension
    return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
}

fun String.toDate(format: String = "dd/MM/yyyy"): Date? {
    return try {
        SimpleDateFormat(format, Locale.getDefault()).parse(this)
    } catch (e: Exception) {
        null
    }
}
