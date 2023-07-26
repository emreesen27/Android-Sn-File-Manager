package com.sn.snfilemanager.extensions

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

fun Long.toHumanReadableByteCount(): String {
    if (this < 1000) {
        return "$this B"
    }
    // val units = context.resources.getStringArray(R.array.byte_units)
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

fun Long.convertLastModifiedToDate(): String {
    val dateFormat = SimpleDateFormat("dd MM yyyy", Locale.getDefault())
    val date = Date(this)
    return dateFormat.format(date)
}