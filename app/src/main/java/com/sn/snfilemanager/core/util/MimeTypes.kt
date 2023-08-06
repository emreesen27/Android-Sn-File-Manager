package com.sn.snfilemanager.core.util

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class MimeTypes(val types: String, val values: List<String>) : Parcelable {
    IMAGES("images", listOf("jpeg", "png", "gif", "bmp", "WebP", "HEIF")),
    VIDEOS("videos", listOf("mp4", "3gp", "avi", "mkv", "wmv")),
    AUDIOS("audios", listOf("mp3", "aac", "wav", "ogg", "mid", "flac", "amr")),
    DOCUMENT("document", listOf("pdf", "xml", "log", "docx")),
    APK("apk", listOf("apk"))
}
