package com.donut.mixmessage.decode.image

import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.donut.mixmessage.currentActivity
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.util.common.UnitBlock
import com.donut.mixmessage.util.common.isNotNull
import com.donut.mixmessage.util.common.isNull
import com.donut.mixmessage.util.common.isNullAnd
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.image.ImageAPI
import com.donut.mixmessage.util.image.saveFileToStorage
import com.donut.mixmessage.util.image.toURI
import io.sanghun.compose.video.RepeatMode
import io.sanghun.compose.video.VideoPlayer
import io.sanghun.compose.video.controller.VideoPlayerControllerConfig
import io.sanghun.compose.video.uri.VideoPlayerMediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun VideoContent(url: String, password: String, fileName: String) {
    val progress = ProgressContent(tip = "视频加载中")
    var error: @Composable UnitBlock? by remember {
        mutableStateOf(null)
    }
    var uri: Uri? by remember {
        mutableStateOf(null)
    }
    var fileData: ByteArray? by remember {
        mutableStateOf(null)
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            fileData = ImageAPI.downloadEncryptedData(
                url,
                password,
                progress.interceptor
            )
            fileData.isNull {
                error = { ErrorMessage(msg = "视频加载失败") }
                return@launch
            }
            uri = fileData?.toURI("video")
        }
    }
    error?.invoke()
    uri.isNullAnd(error.isNull()) {
        progress.LoadingContent()
    }
    uri.isNotNull {
        VideoPlayerContent(uri = uri!!, fileData!!, fileName)
    }
    UrlContent(url = url)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoPlayerContent(uri: Uri, fileData: ByteArray, fileName: String) {
    VideoPlayer(
        mediaItems = listOf(
            VideoPlayerMediaItem.StorageMediaItem(
                storageUri = uri
            ),
        ),
        controllerConfig = VideoPlayerControllerConfig(
            showSpeedAndPitchOverlay = false,
            showSubtitleButton = false,
            showCurrentTimeAndTotalTime = true,
            showBufferingProgress = false,
            showForwardIncrementButton = false,
            showBackwardIncrementButton = false,
            showBackTrackButton = false,
            showNextTrackButton = false,
            showRepeatModeButton = false,
            controllerShowTimeMilliSeconds = 5_000,
            controllerAutoShow = true,
            showFullScreenButton = false
        ),
        handleLifecycle = false,
        autoPlay = true,
        usePlayerController = true,
        enablePip = false,
        handleAudioFocus = true,
        repeatMode = RepeatMode.ALL,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(600.dp)

    )
    TextButton(modifier = Modifier.fillMaxWidth(), onClick = {
        MixDialogBuilder("保存视频到本地?").apply {
            setDefaultNegative()
            setPositiveButton("确定") {
                closeDialog()
                saveFileToStorage(
                    currentActivity,
                    fileData,
                    fileName,
                    Environment.DIRECTORY_MOVIES,
                    storeUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                )
                showToast("保存成功")
            }
            show()
        }
    }) {
        Text(text = "保存本地")
    }
}