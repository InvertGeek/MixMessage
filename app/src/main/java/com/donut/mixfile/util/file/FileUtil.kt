package com.donut.mixfile.util.file


import com.donut.mixfile.server.core.objects.FileDataLog
import com.donut.mixfile.server.core.objects.MixShareInfo
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow


var uploadLogs = listOf<FileDataLog>()

fun addUploadLog(shareInfo: MixShareInfo) {
    val uploadLog = shareInfo.toDataLog()
    if (uploadLogs.size > 1000) {
        uploadLogs = uploadLogs.drop(1)
    }
    uploadLogs = uploadLogs + uploadLog
}

fun formatFileSize(bytes: Long, forceMB: Boolean = false): String {
    if (bytes <= 0) return "0 B"
    if (forceMB && bytes > 1024 * 1024) {
        return String.format(
            Locale.US,
            "%.2f MB",
            bytes / 1024.0 / 1024.0
        )
    }
    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB")
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt().coerceAtMost(units.size - 1)

    return String.format(
        Locale.US,
        "%.2f %s",
        bytes / 1024.0.pow(digitGroups.toDouble()),
        units[digitGroups]
    )
}


