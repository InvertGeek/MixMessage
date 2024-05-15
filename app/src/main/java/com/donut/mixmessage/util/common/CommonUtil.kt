package com.donut.mixmessage.util.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.provider.Settings
import android.util.Log
import com.donut.mixmessage.app
import java.security.MessageDigest

fun String.copyToClipboard(showToast: Boolean = true) {
    val clipboard = getClipBoard()
    val clip = ClipData.newPlainText("Copied Text", this)
    clipboard.setPrimaryClip(clip)
    if (showToast) showToast("复制成功")
}

fun String.removeBrace(): String {
    if (this.startsWith("[") && this.endsWith("]")) {
        return this.substring(1, this.length - 1)
    }
    return this
}


tailrec fun String.calculateMD5(round: Int = 1): String {
    val md = MessageDigest.getInstance("MD5")
    md.update(this.toByteArray())
    val digest = md.digest()
    val sb = StringBuilder()
    for (b in digest) {
        sb.append(String.format("%02x", b))
    }
    if (round > 1) {
        return sb.toString().calculateMD5(round - 1)
    }
    return sb.toString()
}

fun String.truncate(maxLength: Int): String {
    return if (this.length > maxLength) {
        this.substring(0, maxLength) + "..."
    } else {
        this
    }
}

fun getClipBoard(context: Context = app.applicationContext): ClipboardManager {
    return context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
}

typealias UnitBlock = () -> Unit

inline fun <T> T?.isNull(block: UnitBlock = {}): Boolean {
    if (this == null) {
        block()
    }
    return this == null
}

inline fun <T> T?.isNotNull(block: UnitBlock = {}): Boolean {
    if (this != null) {
        block()
    }
    return this != null
}

inline fun Boolean?.isTrue(block: UnitBlock = {}): Boolean {
    if (this == true) {
        block()
    }
    return this == true
}

inline fun Boolean?.isFalse(block: UnitBlock = {}): Boolean {
    if (this == false) {
        block()
    }
    return this == false
}


fun readClipBoardText(): String {
    val clipboard = getClipBoard()
    val clip = clipboard.primaryClip
    if (clip != null && clip.itemCount > 0) {
        val text = clip.getItemAt(0).text
        return text.toString()
    }
    return ""
}

fun debug(text: String, tag: String = "test") {
    Log.d(tag, text)
}

fun catchError(tag: String = "", block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        showError(e, tag)
    }
}

fun showError(e: Exception, tag: String = "") {
    Log.e(
        "error",
        "${tag}发生错误: ${e.message} ${e.stackTraceToString()}"
    )
}

fun isAccessibilityServiceEnabled(): Boolean {
    val accessibilityService = Settings.Secure.getString(
        app.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    return accessibilityService?.contains(app.packageName) == true
}

