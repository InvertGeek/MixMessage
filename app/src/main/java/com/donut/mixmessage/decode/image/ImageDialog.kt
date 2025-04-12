package com.donut.mixmessage.decode.image

import android.os.Environment
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.donut.mixmessage.currentActivity
import com.donut.mixmessage.genImageLoader
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.util.common.TipText
import com.donut.mixmessage.util.common.isNull
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.image.forceCacheInterceptor
import com.donut.mixmessage.util.image.genDecodeInterceptor
import com.donut.mixmessage.util.image.saveFileToStorage
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import okhttp3.OkHttpClient

@Composable
fun ErrorMessage(msg: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(200.dp, 600.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = msg,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageContent(imageUrl: String, password: ByteArray, fileName: String, size: Int) {
    val progress = remember {
        ProgressContent(tip = "图片加载中")
    }
    var imageData: ByteArray? by remember {
        mutableStateOf(null)
    }
    val zoomState = rememberZoomState()
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        error = {
            ErrorMessage(msg = "图片加载失败")
        },
        imageLoader = genImageLoader(
            LocalContext.current,
            sourceListener = {
                imageData = it
            },
            initializer = {
                OkHttpClient.Builder()
                    .addNetworkInterceptor(progress.interceptor)
                    .addNetworkInterceptor(forceCacheInterceptor)
                    .addNetworkInterceptor(genDecodeInterceptor(password, size))
                    .build()
            }),
        loading = {
            progress.LoadingContent()
        },
        contentDescription = "图片",
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(400.dp, 1000.dp)
            .zoomable(zoomState)
            .combinedClickable(
                //不能传递null,否则zoom无法监听到事件
                onDoubleClick = {},
                onLongClick = {
                    imageData.isNull {
                        return@combinedClickable
                    }
                    MixDialogBuilder("保存图片到本地?").apply {
                        setDefaultNegative()
                        setPositiveButton("确定") {
                            closeDialog()
                            saveFileToStorage(
                                currentActivity,
                                imageData!!,
                                fileName,
                                Environment.DIRECTORY_PICTURES
                            )
                            showToast("保存成功")
                        }
                        show()
                    }
                }
            ) {

            }

    )
    TipText(
        "文件大小: ${formatFileSize(size.toLong())}"
    )
}