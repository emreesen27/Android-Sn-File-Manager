package com.sn.snfilemanager.core.util

import com.sn.snfilemanager.R

enum class FileExtension(val extension: String, val iconResId: Int) {
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
    TXT("txt", R.drawable.ic_txt),
    PDF("pdf", R.drawable.ic_pdf),
    MP3("mp3", R.drawable.ic_mp3),
    BAT("bat", R.drawable.ic_bat),
    EXE("exe", R.drawable.ic_exe),
    WAV("wav", R.drawable.ic_wav),
    OGG("ogg", R.drawable.ic_ogg),
    PPT("ppt", R.drawable.ic_ppt),
    XLS("xls", R.drawable.ic_xls),
    APK("apk", R.drawable.ic_apk),
    ZIP("zip", R.drawable.ic_zip),
    EPS("eps", R.drawable.ic_eps),
    DOC("doc", R.drawable.ic_doc),
    OTF("otf", R.drawable.ic_otf),
    PPTX("pptx", R.drawable.ic_pptx),
    TIF("tif", R.drawable.ic_tif),
    PSD("psd", R.drawable.ic_psd),
    DLL("dll", R.drawable.ic_dll),
    DAT("dat", R.drawable.ic_dat),
    PHP("php", R.drawable.ic_php),
    JS("js", R.drawable.ic_js),
    RAR("rar", R.drawable.ic_rar),
    ASF("asd", R.drawable.ic_asf),
    CAB("cab", R.drawable.ic_cab),
    CAD("cad", R.drawable.ic_cad),
    AI("ai", R.drawable.ic_ai),
    DS("3ds", R.drawable.ic_3ds),
    CPP("cpp", R.drawable.ic_cpp),
    CDR("cdr", R.drawable.ic_cdr),
    CSS("css", R.drawable.ic_css),
    INDD("indd", R.drawable.ic_indd),
    TEX("text", R.drawable.ic_tex),
    HTML("html", R.drawable.ic_html),
    MIDI("midi", R.drawable.ic_midi),
    DMG("dmg", R.drawable.ic_dmg_file),
    XML("xml", R.drawable.ic_xml),
    ;

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
