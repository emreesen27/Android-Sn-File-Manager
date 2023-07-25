package com.sn.snfilemanager.extensions

import java.text.DecimalFormat

fun Long.toHumanReadableByteCount(): String {
    if (this < 1000) {
        return "$this B"
    }
    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB")
    var value = this.toDouble()
    var unitIndex = 0
    while (value >= 1000 && unitIndex < units.size - 1) {
        value /= 1000
        unitIndex++
    }
    val decimalFormat = DecimalFormat("#,##0.##")
    return "${decimalFormat.format(value)} ${units[unitIndex]}"
}