package com.donut.mixmessage.util.common

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import androidx.core.net.toUri
import com.donut.mixmessage.app
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

fun String.copyWithDialog(description: String = "") {
    val str = this
    MixDialogBuilder("复制${description}到剪贴板?").apply {
        setDefaultNegative()
        setPositiveButton("确定") {
            str.copyToClipboard()
            closeDialog()
        }
        show()
    }
}

fun CoroutineScope.loopTask(
    delay: Long,
    initDelay: Long = 0,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    block: suspend () -> Unit
) = launch(dispatcher) {
    delay(initDelay)
    while (true) {
        block()
        delay(delay)
    }
}

class CachedDelegate<T>(val getKeys: () -> Array<Any?>, private val initializer: () -> T) {
    private var cache: T = initializer()
    private var keys: Array<Any?> = getKeys()

    operator fun getValue(thisRef: Any?, property: Any?): T {
        val newKeys = getKeys()
        if (!keys.contentEquals(newKeys)) {
            keys = newKeys
            cache = initializer()
        }
        return cache
    }

    operator fun setValue(thisRef: Any?, property: Any?, value: T) {
        cache = value
    }
}


tailrec fun String.hashToMD5String(round: Int = 1): String {
    val digest = hashMD5()
    if (round > 1) {
        return digest.toHex().hashToMD5String(round - 1)
    }
    return digest.toHex()
}

fun ByteArray.toHex(): String {
    val sb = StringBuilder()
    for (b in this) {
        sb.append(String.format("%02x", b))
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

fun ByteArray.calculateHash(algorithm: String): String {
    val md = MessageDigest.getInstance(algorithm)
    md.update(this)
    return md.digest().toHex()
}

fun ByteArray.hashSHA256() = calculateHash("SHA-256")

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

@OptIn(ExperimentalEncodingApi::class)
fun ByteArray.encodeToBase64() = Base64.encode(this)

@OptIn(ExperimentalEncodingApi::class)
fun String.decodeBase64() = Base64.decode(this)

@OptIn(ExperimentalEncodingApi::class)
fun String.decodeBase64String() = Base64.decode(this).decodeToString()

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
    app.contentResolver.query(this, null, null, null, null)?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        it.moveToFirst()
        fileName = it.getString(nameIndex)
    }
    return fileName
}

fun readRawText(id: Int) = app.resources.openRawResource(id).readBytes().decodeToString()

typealias UnitBlock = () -> Unit

fun readClipBoardText(): String {
    val clipboard = getClipBoard()
    val clip = clipboard.primaryClip
    if (clip != null && clip.itemCount > 0) {
        val text = clip.getItemAt(0).text
        return text?.toString() ?: ""
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

fun formatTime(date: Date): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    return formatter.format(date)
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


fun isValidUri(uriString: String): Boolean {
    try {
        val uri = uriString.toUri()
        return uri.scheme != null
    } catch (e: Exception) {
        return false
    }
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

