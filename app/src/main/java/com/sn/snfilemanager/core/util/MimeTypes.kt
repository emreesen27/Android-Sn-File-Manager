package com.sn.snfilemanager.core.util

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class MimeTypes(val types: String, val values: List<String>) : Parcelable {
    IMAGES("images", listOf("all","jpeg", "jpg", "png", "gif", "bmp", "WebP", "HEIF")),
    VIDEOS("videos", listOf("all","mp4", "3gp", "avi", "mkv", "wmv")),
    AUDIOS("audios", listOf("all","mp3", "aac", "wav", "ogg", "mid", "flac", "amr")),
    DOCUMENT("document", listOf("all", "pdf", "ppt", "doc", "xls", "txt", "docx", "pptx", "xml", "xlsx", "log","psd","ai","indd")),
    ARCHIVE("archive", listOf("zip", "rar", "7z","tar")),
    APK("apk", listOf("apk"));
}
