package com.donut.mixmessage.decode.file

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import com.donut.mixfile.server.core.utils.StreamContent
import com.donut.mixmessage.app
import com.donut.mixmessage.currentActivity
import com.donut.mixmessage.decode.sendResult
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.ui.component.encoder.encoderText
import com.donut.mixmessage.util.common.AsyncEffect
import com.donut.mixmessage.util.common.errorDialog
import com.donut.mixmessage.util.common.getFileSize
import com.donut.mixmessage.util.common.isTrue
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.encode.encodeText
import com.donut.mixmessage.util.mixfile.server
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

fun selectFile(doSend: Boolean = true) {
    currentActivity?.fileSelector?.openSelect(arrayOf("*/*")) { uri, fileName ->
        MixDialogBuilder(
            "上传中",
            autoClose = false
        ).apply {
            setContent {
                val progressContent = remember {
                    ProgressContent("上传中")
                }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    progressContent.LoadingContent()
                }
                AsyncEffect {
                    val resolver = app.contentResolver
                    val fileSize = errorDialog("读取文件失败") {
                        uri.getFileSize()
                    } ?: return@AsyncEffect
                    val fileStream = resolver.openInputStream(uri)
                    if (fileStream == null) {
                        showToast("打开文件失败")
                        closeDialog()
                        return@AsyncEffect
                    }
                    val stream = StreamContent(fileStream, fileSize)
                    val fileCode = putUploadFile(stream, fileName, false, progressContent)
                    if (fileCode.isEmpty()) {
                        showToast("上传失败")
                        closeDialog()
                        return@AsyncEffect
                    }
                    encoderText = TextFieldValue("mf://$fileCode")
                    showToast("上传成功!")
                    closeDialog()
                    doSend.isTrue {
                        sendResult(encodeText(encoderText.text))
                    }
                }
            }
            setDefaultNegative()
            show()
        }
    }
}

val localClient = HttpClient(OkHttp).config {
    install(HttpTimeout) {
        requestTimeoutMillis = 1000 * 60 * 60 * 24 * 30L
        socketTimeoutMillis = 1000 * 60 * 60
        connectTimeoutMillis = 1000 * 60 * 60
    }
}

suspend fun putUploadFile(
    data: Any?,
    name: String,
    add: Boolean = true,
    progressContent: ProgressContent = ProgressContent(),
): String {
    return errorDialog<String>("上传失败") {
        val response = localClient.put {
            url("http://127.0.0.1:${server.serverPort}/api/upload")
            onUpload(progressContent.ktorListener)
            parameter("name", name)
            parameter("add", add)
            setBody(data)
        }
        val message = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw Exception("上传失败: $message")
        }
        return message
    } ?: ""
}



