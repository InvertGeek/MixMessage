package com.donut.mixfile.server.core.utils


import com.donut.mixfile.server.core.aes.generateRandomByteArray
import com.donut.mixfile.server.core.utils.extensions.mb
import io.ktor.client.request.forms.FormBuilder
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.OutgoingContent
import io.ktor.http.defaultForFilePath
import io.ktor.http.encodeURLParameter
import io.ktor.http.quote
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header
import io.ktor.server.routing.RoutingContext
import io.ktor.util.pipeline.PipelineContext
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.CancellationException
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.jvm.javaio.toOutputStream
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.ServerSocket
import java.net.URI
import java.util.concurrent.CopyOnWriteArrayList
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.random.Random

fun String.sanitizeFileName(): String {
    // 定义非法字符，包括控制字符、文件系统非法字符、路径遍历等
    val illegalChars = "[\\x00-\\x1F\\x7F/\\\\:*?\"<>|]".toRegex()
    // Windows 保留文件名（大小写不敏感）
    val reservedNames = setOf(
        "CON", "PRN", "AUX", "NUL",
        "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
        "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
    )

    // 处理文件名
    var sanitized = this
        // 替换非法字符为下划线
        .replace(illegalChars, "_")
        .trim()

    if (sanitized.all { it == '.' }) {
        sanitized = "unnamed_file"
    }

    if (sanitized.uppercase() in reservedNames) {
        sanitized = "_$sanitized"
    }

    return sanitized.takeLast(255).ifEmpty { "unnamed_file" }
}


fun genRandomString(
    length: Int = 32,
    charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
): String {
    return (1..length)
        .map { Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")
}

fun isValidURL(urlString: String): Boolean {
    return try {
        val uri = URI.create(urlString)

        // 获取协议和主机名
        val protocol = uri.scheme
        val host = uri.host

        // 检查协议和主机名是否为空
        if (protocol.isNullOrBlank() || host.isNullOrBlank()) {
            return false
        }

        // 可选：限制协议类型
        protocol in listOf("http", "https")
    } catch (e: IllegalArgumentException) {
        false
    }
}


fun fileFormHeaders(
    suffix: String = ".gif",
    mimeType: String = "image/gif",
): Headers {
    return Headers.build {
        append(HttpHeaders.ContentType, mimeType)
        append(
            HttpHeaders.ContentDisposition,
            "filename=\"${genRandomString(5)}${suffix}\""
        )
    }
}


fun concurrencyLimit(
    limit: Int,
    route: suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit,
): suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit {
    val tasks = CopyOnWriteArrayList<() -> Unit>()
    return route@{
        while (tasks.size > limit) {
            val remove = tasks.removeAt(0)
            ignoreError {
                remove()
            }
        }
        val cancel: () -> Unit = {
            launch {
                throw Throwable("服务器达到并发限制")
            }
        }
        tasks.add(cancel)
        route(Unit)
        tasks.remove(cancel)
    }
}

inline fun <T> ignoreError(block: () -> T): T? {
    try {
        return block()
    } catch (_: Exception) {

    }
    return null
}


fun getRandomEncKey() = generateRandomByteArray(256)

fun compressGzip(input: String): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    GZIPOutputStream(byteArrayOutputStream).use { gzip ->
        gzip.write(input.toByteArray())
    }
    return byteArrayOutputStream.toByteArray()
}

fun decompressGzip(compressed: ByteArray, limit: Int = 200.mb): String {
    val byteArrayInputStream = ByteArrayInputStream(compressed)
    GZIPInputStream(byteArrayInputStream).use { gzip ->
        val buffer = ByteArray(1024 * 64)
        val output = ByteArrayOutputStream()
        var bytesRead: Int
        var totalBytes = 0

        // 循环读取数据，直到结束或达到限制
        while (gzip.read(buffer).also { bytesRead = it } != -1) {
            if (totalBytes + bytesRead > limit) {
                throw Exception("压缩文件大小超过限制")
            }
            output.write(buffer, 0, bytesRead)
            totalBytes += bytesRead
        }

        return output.toByteArray().decodeToString()
    }
}

fun String.encodeURL(): String {
    return encodeURLParameter()
}

fun String.parseFileMimeType() = ContentType.defaultForFilePath(this)

@OptIn(InternalAPI::class)
fun FormBuilder.add(key: String, value: Any?, headers: Headers = Headers.Empty) {
    append(key.quote(), value ?: "", headers)
}

class StreamContent(private val stream: InputStream, val length: Long = 0) :
    OutgoingContent.WriteChannelContent() {
    override suspend fun writeTo(channel: ByteWriteChannel) {
        stream.copyTo(channel.toOutputStream())
    }

    override val contentLength: Long
        get() = length

}

suspend fun <T> retry(
    times: Int = 3,
    delay: Long = 500,
    block: suspend () -> T
): T {
    repeat(times - 1) {
        try {
            return block()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            delay(delay)
        }
    }
    return block() // 最后一次尝试
}

fun RoutingContext.getHeader(name: String) = call.request.header(name)

fun findAvailablePort(startPort: Int = 9527, endPort: Int = 65535): Int? {
    for (port in startPort..endPort) {
        ignoreError<Int> {
            // 尝试绑定到指定端口
            ServerSocket(port).use { serverSocket ->
                // 成功绑定，返回该端口
                return serverSocket.localPort
            }
        }
    }
    return null
}