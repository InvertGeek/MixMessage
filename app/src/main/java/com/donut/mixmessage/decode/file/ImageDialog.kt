package com.donut.mixmessage.decode.file

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.donut.mixmessage.genImageLoader
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
fun ImageContent(
    imageUrl: String,
    @SuppressLint("ModifierParameter") modifier: Modifier? = null,
    scale: ContentScale = ContentScale.Fit,
) {
    val progress = remember {
        ProgressContent(tip = "图片加载中")
    }
    val zoomState = rememberZoomState()
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
//            .videoFrameMillis(0)
            .crossfade(true)
            .build(),
        error = {
            ErrorMessage(msg = "图片加载失败")
        },
        loading = {
            progress.LoadingContent()
        },
        imageLoader = genImageLoader(
            LocalContext.current,
            initializer = {
                OkHttpClient.Builder()
                    .addNetworkInterceptor(progress.interceptor)
                    .build()
            }),
        contentDescription = "图片",
        contentScale = scale,
        modifier = modifier ?: Modifier
            .fillMaxWidth()
            .heightIn(400.dp, 1000.dp)
            .zoomable(zoomState)

    )
}