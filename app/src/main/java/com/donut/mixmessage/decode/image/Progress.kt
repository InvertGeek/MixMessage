package com.donut.mixmessage.decode.image

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

class ProgressInterceptor(private val progressListener: ProgressListener) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        return originalResponse.newBuilder()
            .body(ProgressResponseBody(originalResponse.body!!, progressListener))
            .build()
    }
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"

    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()

    return String.format(
        Locale.US,
        "%.1f %s",
        bytes / 1024.0.pow(digitGroups.toDouble()),
        units[digitGroups]
    )
}

class ProgressContent(
    private var tip: String = "下载中"
) {
    private var progress: Float by mutableFloatStateOf(0f)
    private var bytesWritten: Long by mutableLongStateOf(0)
    private var contentLength: Long by mutableLongStateOf(0)

    val interceptor = ProgressInterceptor { bytes, length, done ->
        updateProgress(bytes, length)
    }

    val ktorListener: suspend (bytesWritten: Long, bytesTotal: Long?) -> Unit = { bytes, length ->
        updateProgress(bytes, length ?: 0L)
    }

    fun updateProgress(written: Long = bytesWritten, total: Long = contentLength) {
        bytesWritten = written
        contentLength = total.coerceAtLeast(1)
        progress = bytesWritten.toFloat() / contentLength.toFloat()
    }

    @Composable
    fun LoadingContent(show: Boolean = true) {
        LoadingBar(
            progress = progress,
            bytesWritten,
            contentLength,
            tip = tip,
            show
        )
    }

    fun increaseBytesWritten(bytes: Long, total: Long) {
        bytesWritten += bytes
        contentLength = total.coerceAtLeast(1)
        updateProgress()
    }

}

@Composable
fun LoadingBar(
    progress: Float,
    bytesWritten: Long,
    contentLength: Long,
    tip: String,
    show: Boolean = true
) {

    val sizeDp by animateDpAsState(if (show) 600.dp else 0.dp, label = "progress")

    Column(
        modifier = Modifier
            .heightIn(0.dp, sizeDp)
            .alpha(if (show) 1f else 0f)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (progress <= 0) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(text = "${tip}: ${formatFileSize(bytesWritten)}/${formatFileSize(contentLength)}")
            }
        }
    }
}


class ProgressResponseBody(
    private val responseBody: ResponseBody,
    private val progressListener: ProgressListener
) : ResponseBody() {

    private var bufferedSource: BufferedSource? = null
    override fun contentLength(): Long {
        return responseBody.contentLength()
    }

    override fun contentType(): MediaType? {
        return responseBody.contentType()
    }

    override fun source(): BufferedSource {
        if (bufferedSource == null) {
            bufferedSource = source(responseBody.source()).buffer()
        }
        return bufferedSource!!
    }

    private fun source(source: Source): Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L

            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                progressListener.update(
                    totalBytesRead,
                    responseBody.contentLength(),
                    bytesRead == -1L
                )
                return bytesRead
            }
        }
    }
}

fun interface ProgressListener {
    fun update(bytesRead: Long, contentLength: Long, done: Boolean)
}