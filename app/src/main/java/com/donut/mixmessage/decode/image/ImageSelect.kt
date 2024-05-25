package com.donut.mixmessage.decode.image

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.DialogProperties
import com.donut.mixmessage.currentActivity
import com.donut.mixmessage.decode.DecodeActivity
import com.donut.mixmessage.decode.sendResult
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.ui.component.common.SingleSelectItemList
import com.donut.mixmessage.ui.component.encoder.encoderText
import com.donut.mixmessage.util.common.isNull
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.encode.encodeText
import com.donut.mixmessage.util.encode.encoders.bean.CoderResult
import com.donut.mixmessage.util.encode.getCurrentPassword
import com.donut.mixmessage.util.image.startUploadImage
import com.donut.mixmessage.util.image.toImageData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun selectFile() {
    MixDialogBuilder("文件类型").apply {
        setContent {
            SingleSelectItemList(listOf("图片", "视频", "文件"), null) {
                when (it) {
                    "图片" -> selectImage { data -> data.toImageData() }
                    "视频" -> selectImage(
                        "video/*",
                        CoderResult.VIDEO_IDENTIFIER
                    )

                    "文件" -> selectImage("*/*", CoderResult.FILE_IDENTIFIER)
                }
                closeDialog()
            }
        }
        setDefaultNegative()
        show()
    }
}

fun selectImage(
    type: String = "image/*",
    identifier: String = CoderResult.IMAGE_IDENTIFIER,
    block: (ByteArray) -> ByteArray = { it }
) {
    DecodeActivity.mixFileSelector.openSelect(arrayOf(type)) { data, fileName ->
        MixDialogBuilder(
            "上传中", properties = DialogProperties(
                dismissOnClickOutside = false
            )
        ).apply {
            setContent {
                val scope = rememberCoroutineScope()
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
                LaunchedEffect(Unit) {
                    scope.launch(Dispatchers.IO) {
                        val password = getCurrentPassword()
                        val url = startUploadImage(
                            block(data),
                            password,
                        )
                        url.isNull {
                            closeDialog()
                            showToast("上传失败")
                            return@launch
                        }
                        showToast("上传成功!")
                        withContext(Dispatchers.Main) {
                            encoderText =
                                TextFieldValue(CoderResult.media(url!!, fileName, identifier))
                            sendResult(encodeText(encoderText.text, password))
                        }
                        currentActivity.finish()
                    }
                }
            }
            setDefaultNegative()
            show()
        }
    }
}