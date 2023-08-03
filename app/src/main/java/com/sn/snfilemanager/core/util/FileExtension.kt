package com.sn.snfilemanager.core.util

import com.sn.snfilemanager.R

enum class FileExtension(val extension: String, val iconResId: Int) {
    TXT("txt", R.drawable.ic_txt),
    PDF("pdf", R.drawable.ic_pdf),
    MP3("mp3", R.drawable.ic_mp3),
    MP4("mp4", 0),
    GP("3gp", 0),
    AVI("avi", 0),
    MOV("mov", 0),
    MKV("mkv", 0),
    WMW("wmv", 0),
    JPG("jpg", 0),
    JPEG("jpeg", 0),
    PNG("png", 0),
    GIF("gif", 0),
    BMP("bmp", 0),
    WEBP("webp", 0),
    HEIF("heif", 0),
    BAT("bat", R.drawable.ic_bat),
    EXE("exe", R.drawable.ic_exe),
    WAV("wav", R.drawable.ic_wav),
    OGG("ogg", R.drawable.ic_ogg),
    PPT("ppt", R.drawable.ic_ppt),
    XLS("xls", R.drawable.ic_xls),
    APK("apk", R.drawable.ic_apk),
    ZIP("zip", R.drawable.ic_zip),
    EPS("eps", R.drawable.ic_eps);


    companion object {
        fun getIconResourceId(extension: String): Int {
            return FileExtension.values().find { it.extension == extension.lowercase() }?.iconResId
                ?: R.drawable.ic_unknown
        }

        fun isVideoExtension(extension: String): Boolean {
            return listOf("mp4", "3gp", "avi", "mkv", "wmv", "mov").contains(extension)
        }
    }
}