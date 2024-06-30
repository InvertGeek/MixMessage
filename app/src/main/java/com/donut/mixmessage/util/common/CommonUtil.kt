package com.donut.mixmessage.util.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import com.donut.mixmessage.app
import com.donut.mixmessage.currentActivity
import java.net.URL
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

fun String.copyToClipboard(showToast: Boolean = true) {
    val clipboard = getClipBoard()
    val clip = ClipData.newPlainText("Copied Text", this)
    clipboard.setPrimaryClip(clip)
    if (showToast) showToast("复制成功")
}

fun String.removeBrackets(): String {
    if (this.startsWith("[") && this.endsWith("]")) {
        return this.substring(1, this.length - 1)
    }
    return this
}

class CachedDelegate<T>(val getKeys: () -> Array<Any?>, private val initializer: () -> T) {
    private var cache: T? = null
    private var keys: Array<Any?> = arrayOf()

    operator fun getValue(thisRef: Any?, property: Any?): T {
        val newKeys = getKeys()
        if (cache == null || !keys.contentEquals(newKeys)) {
            keys = newKeys
            cache = initializer()
        }
        return cache!!
    }

    operator fun setValue(thisRef: Any?, property: Any?, value: T) {
        cache = value
    }
}

tailrec fun String.hashToMD5String(round: Int = 1): String {
    val digest = hashMD5()
    val sb = StringBuilder()
    for (b in digest) {
        sb.append(String.format("%02x", b))
    }
    if (round > 1) {
        return sb.toString().hashToMD5String(round - 1)
    }
    return sb.toString()
}

fun String.hashMD5() = calculateHash("MD5")

fun String.hashSHA256() = calculateHash("SHA-256")

fun String.calculateHash(algorithm: String): ByteArray {
    val md = MessageDigest.getInstance(algorithm)
    md.update(this.toByteArray())
    return md.digest()
}

inline fun String.isUrl(block: (URL) -> Unit = {}): Boolean {
    val urlPattern =
        Regex("^https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)\$")
    val result = urlPattern.matches(this)
    if (result) {
        ignoreError {
            block(URL(this))
        }
    }
    return result
}

fun getUrlHost(url: String): String? {
    url.isUrl {
        return it.host
    }
    return null
}

fun String.truncate(maxLength: Int): String {
    return if (this.length > maxLength) {
        this.substring(0, maxLength) + "..."
    } else {
        this
    }
}

@OptIn(ExperimentalEncodingApi::class)
fun ByteArray.encodeToBase64() = Base64.encode(this)

@OptIn(ExperimentalEncodingApi::class)
fun String.decodeBase64() = Base64.decode(this)

fun String.encodeToBase64() = this.toByteArray().encodeToBase64()

fun getClipBoard(context: Context = app.applicationContext): ClipboardManager {
    return context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
}

fun <T> List<T>.at(index: Long): T {
    var fixedIndex = index % this.size
    if (fixedIndex < 0) {
        fixedIndex += this.size
    }
    return this[fixedIndex.toInt()]
}

fun <T> List<T>.at(index: Int): T {
    return this.at(index.toLong())
}

infix fun <T> List<T>.elementEquals(other: List<T>): Boolean {
    if (this.size != other.size) return false

    val tracker = BooleanArray(this.size)
    var counter = 0

    root@ for (value in this) {
        destination@ for ((i, o) in other.withIndex()) {
            if (tracker[i]) {
                continue@destination
            } else if (value?.equals(o) == true) {
                counter++
                tracker[i] = true
                continue@root
            }
        }
    }

    return counter == this.size
}

fun Uri.getFileName(): String {
    var fileName = ""
    currentActivity.contentResolver.query(this, null, null, null, null)?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        it.moveToFirst()
        fileName = it.getString(nameIndex)
    }
    return fileName
}

typealias UnitBlock = () -> Unit

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

inline fun catchError(tag: String = "", block: () -> Unit) {
    try {
        block()
    } catch (e: Exception) {
        showError(e, tag)
    }
}

inline fun ignoreError(tag: String = "", block: () -> Unit) {
    try {
        block()
    } catch (_: Exception) {

    }
}


fun getCurrentDate(reverseDays: Long = 0): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return formatter.format(Date(System.currentTimeMillis() - (reverseDays * 86400 * 1000)))
}

fun getCurrentTime(): String {
    val currentTime = Date()
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    return formatter.format(currentTime)
}

fun genRandomString(length: Int = 32): String {
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    return (1..length)
        .map { kotlin.random.Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
}

fun showError(e: Throwable, tag: String = "") {
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

