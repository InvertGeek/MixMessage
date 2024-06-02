package com.donut.mixmessage.decode.image

import android.os.Environment
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.donut.mixmessage.currentActivity
import com.donut.mixmessage.genImageLoader
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.util.common.ZoomableView
import com.donut.mixmessage.util.common.copyToClipboard
import com.donut.mixmessage.util.common.isNull
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.image.forceCacheInterceptor
import com.donut.mixmessage.util.image.genDecodeInterceptor
import com.donut.mixmessage.util.image.saveFileToStorage
import okhttp3.OkHttpClient

@Composable
fun UrlContent(url: String) {
    Column(
        Modifier
            .clickable {
                MixDialogBuilder("复制地址到剪贴板?").apply {
                    setDefaultNegative()
                    setPositiveButton("确定") {
                        url.copyToClipboard()
                        closeDialog()
                    }
                    show()
                }
            }
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Text(
            text = "文件地址: $url",
            color = Color.Gray,
            style = TextStyle(
                fontSize = 10.sp,
                lineHeight = 12.sp
            ),
            modifier = Modifier
                .fillMaxWidth(),
        )
    }
}

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
fun ImageContent(imageUrl: String, password: String, fileName: String) {
    val progress = ProgressContent(tip = "图片加载中")
    var imageData: ByteArray? by remember {
        mutableStateOf(null)
    }
    ZoomableView {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            error = {
                ErrorMessage(msg = "图片加载失败")
            },
            imageLoader = genImageLoader(LocalContext.current,
                sourceListener = {
                    imageData = it
                },
                initializer = {
                    OkHttpClient.Builder()
                        .addNetworkInterceptor(progress.interceptor)
                        .addNetworkInterceptor(forceCacheInterceptor)
                        .addNetworkInterceptor(genDecodeInterceptor(password))
                        .build()
                }),
            loading = {
                progress.LoadingContent()
            },
            contentDescription = "图片",
            modifier = Modifier
//            .background(Color.Red)
                .fillMaxWidth()
                .heightIn(400.dp)
                .combinedClickable(
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
                ) {}

        )
    }
    UrlContent(url = imageUrl)
}