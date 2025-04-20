package com.donut.mixmessage.decode.image

import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.donut.mixmessage.currentActivity
import com.donut.mixmessage.ui.theme.colorScheme
import com.donut.mixmessage.util.common.AsyncEffect
import com.donut.mixmessage.util.common.TipText
import com.donut.mixmessage.util.common.isFalse
import com.donut.mixmessage.util.common.isNotNull
import com.donut.mixmessage.util.common.isNotTrue
import com.donut.mixmessage.util.common.isNull
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.image.ImageAPI
import com.donut.mixmessage.util.image.saveFileToStorage

@Composable
fun FileContent(url: String, password: ByteArray, fileName: String, size: Int) {

    val progress = remember {
        ProgressContent()
    }

    var download by remember {
        mutableStateOf(false)
    }

    var fileData: ByteArray? by remember {
        mutableStateOf(null)
    }

    var error: @Composable (() -> Unit)? by remember {
        mutableStateOf(null)
    }


    AsyncEffect(download) {
        download.isFalse {
            return@AsyncEffect
        }
        fileData = ImageAPI.downloadEncryptedData(
            url,
            password,
            size,
            progress.interceptor
        )
        fileData.isNull {
            error = { ErrorMessage(msg = "文件下载失败") }
            return@AsyncEffect
        }
        saveFileToStorage(
            currentActivity,
            fileData!!,
            fileName,
            Environment.DIRECTORY_DOWNLOADS,
            MediaStore.Files.getContentUri("external")
        )
        showToast("下载成功!")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(200.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val showProgress = download && error.isNull() && fileData.isNull()
            progress.LoadingContent(showProgress)
            fileData.isNotNull {
                Text(
                    text = "文件已保存到手机下载目录",
                    color = colorScheme.primary
                )
            }
            error.isNotNull {
                it.invoke()
            }

            Text(text = "文件(${formatFileSize(size.toLong())}): ", color = colorScheme.primary)

            Text(text = fileName)

            download.isNotTrue {
                Button(onClick = {
                    download = true
                }) {
                    Text(text = "下载文件")
                }
            }
        }
    }
    TipText(
        "文件大小: ${formatFileSize(size.toLong())}"
    )
}