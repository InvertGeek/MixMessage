package com.donut.mixmessage.util.image

import android.app.Activity
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Movie
import android.graphics.drawable.AnimatedImageDrawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toUri
import com.donut.mixmessage.app
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.catchError
import com.donut.mixmessage.util.common.genRandomString
import com.donut.mixmessage.util.common.isFalse
import com.donut.mixmessage.util.common.isTrue
import com.donut.mixmessage.util.encode.decryptAES
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import kotlin.random.Random

var IMAGE_COMPRESS_RATE by cachedMutableOf(80, "image_compress_rate")

fun Bitmap.compressToByteArray(
    useWebp: Boolean = true,
    webpQuality: Int = IMAGE_COMPRESS_RATE.toInt(),
    lossless: Boolean = false,
    gifHeader: Boolean = false
): ByteArray {
    val bitmap = this
    val stream = ByteArrayOutputStream()
    gifHeader.isTrue {
        stream.write("GIF89a".toByteArray())
    }
    useWebp.isTrue {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            bitmap.compress(
                if (lossless) Bitmap.CompressFormat.WEBP_LOSSLESS else Bitmap.CompressFormat.WEBP_LOSSY,
                webpQuality,
                stream
            )
        } else {
            bitmap.compress(Bitmap.CompressFormat.WEBP, webpQuality, stream)
        }
    }.isFalse {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, stream)
    }
    return stream.toByteArray()
}

fun createBlankBitmap(width: Int, height: Int): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(Color.rgb(Random.nextInt(255), Random.nextInt(255), Random.nextInt(255)))
    return bitmap
}


fun ByteArray.toURI(name: String = "file-cache.bin"): Uri {
    val file = File(app.cacheDir, name)
    file.writeBytes(this)
    return file.toUri()
}

val forceCacheInterceptor = Interceptor { chain ->
    val response = chain.proceed(chain.request())
    val cacheControl = CacheControl.Builder()
        .maxAge(3, TimeUnit.DAYS)
        .build()
    response.newBuilder()
        .removeHeader("Pragma")
        .removeHeader("Cache-Control")
        .header("Cache-Control", cacheControl.toString())
        .build()
}

fun saveFileToStorage(
    activity: Activity,
    file: ByteArray,
    displayName: String,
    directory: String,
    storeUri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
): Uri? {
    val resolver = activity.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
//        put(MediaStore.MediaColumns.MIME_TYPE, "image/gif")
        put(MediaStore.MediaColumns.RELATIVE_PATH, directory)
    }

    val fileUri = resolver.insert(storeUri, contentValues)
    fileUri?.let { uri ->
        resolver.openOutputStream(uri)?.use {
            it.write(file)
        }
    }
    return fileUri
}

fun genDecodeInterceptor(password: String): Interceptor {
    return Interceptor { chain ->
        val originalResponse = chain.proceed(
            chain.request()
        )
        val maxFileSize = 25 * 1024 * 1024L
        val body = originalResponse.peekBody(maxFileSize)
        val originalBytes = body.bytes()
        val fileData = decryptAES(
            splitArray(originalBytes).second,
            password
        )
        originalResponse.newBuilder()
            .header("content-length", fileData.size.toString())
            .body(fileData.toResponseBody(body.contentType()))
            .build()
    }
}

inline fun <T> Uri.withStream(block: (InputStream) -> T): T? {
    return app.contentResolver.openInputStream(this)?.use {
        block(it)
    }
}

fun Uri.getFileSize(): Long {
    return withStream {
        it.available().toLong()
    } ?: 0
}

fun Uri.toByteArray(): ByteArray {
    return withStream {
        it.readBytes()
    } ?: byteArrayOf()
}

fun ByteArray.toImageData(): ByteArray {
    var byteArray = this
    checkIsGif(byteArray).isFalse {
        byteArray = byteArray.toBitmap().compressToByteArray()
    }
    return byteArray
}

private fun checkIsGif(data: ByteArray): Boolean {
    catchError {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ImageDecoder.createSource(data)
            } else {
                ImageDecoder.createSource(ByteBuffer.wrap(data))
            }
            val drawable = ImageDecoder.decodeDrawable(source)
            if (drawable is AnimatedImageDrawable) {
                return true
            }
        } else {
            val movie = Movie.decodeStream(data.inputStream())
            return movie != null
        }
    }
    return false
}

fun ByteArray.toBitmap(): Bitmap {
    return BitmapFactory.decodeByteArray(this, 0, this.size)
}

fun Int.toByteArray(): ByteArray {
    return byteArrayOf(
        (this shr 24).toByte(),
        (this shr 16).toByte(),
        (this shr 8).toByte(),
        this.toByte()
    )
}

fun ByteArray.endToInt(): Int {
    require(this.size >= 4) { "ByteArray must have at least 4 bytes" }
    return (this[size - 1].toInt() and 0xFF) or
            (this[size - 2].toInt() and 0xFF shl 8) or
            (this[size - 3].toInt() and 0xFF shl 16) or
            (this[size - 4].toInt() and 0xFF shl 24)
}

fun combineArray(array1: ByteArray, array2: ByteArray): ByteArray {
    return array1 + array2 + array1.size.toByteArray()
}

fun splitArray(array: ByteArray): Pair<ByteArray, ByteArray> {
    if (array.size < 4) {
        return Pair(byteArrayOf(), byteArrayOf())
    }
    val delimiterIndex = array.endToInt()
    if (delimiterIndex > array.size - 4 || delimiterIndex < 0) {
        return Pair(byteArrayOf(), byteArrayOf())
    }
    return Pair(
        array.copyOfRange(0, delimiterIndex),
        array.copyOfRange(delimiterIndex, array.size - 4)
    )
}

fun fileFormHeaders(
    suffix: String = ".webp",
    mimeType: String = "image/webp"
): Headers {
    return Headers.build {
        append(HttpHeaders.ContentType, mimeType)
        append(HttpHeaders.ContentDisposition, "filename=\"${genRandomString(5)}${suffix}\"")
    }
}
