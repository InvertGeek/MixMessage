package com.donut.mixmessage.decode.image

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.DialogProperties
import com.donut.mixmessage.decode.DecodeActivity
import com.donut.mixmessage.decode.sendResult
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.ui.component.common.SingleSelectItemList
import com.donut.mixmessage.ui.component.encoder.encoderText
import com.donut.mixmessage.ui.component.routes.settings.routes.selectImageAPI
import com.donut.mixmessage.util.common.isNull
import com.donut.mixmessage.util.common.isTrue
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.encode.encodeText
import com.donut.mixmessage.util.encode.encoders.bean.CoderResult
import com.donut.mixmessage.util.encode.getCurrentPassword
import com.donut.mixmessage.util.image.startUploadImage
import com.donut.mixmessage.util.image.toImageData
import com.donut.mixmessage.util.objects.MixFileSelector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun selectFile(doSend: Boolean = true, selector: MixFileSelector = DecodeActivity.mixFileSelector) {
    MixDialogBuilder("文件类型").apply {
        setContent {
            SingleSelectItemList(listOf("图片", "视频", "文件"), null) {
                when (it) {
                    "图片" -> selectImage(
                        doSend = doSend,
                        selector = selector
                    ) { data -> data.toImageData() }

                    "视频" -> selectImage(
                        "video/*",
                        CoderResult.VIDEO_IDENTIFIER,
                        doSend,
                        selector,
                    )

                    "文件" -> selectImage(
                        "*/*",
                        CoderResult.FILE_IDENTIFIER,
                        doSend,
                        selector = selector
                    )
                }
                closeDialog()
            }
        }
        setPositiveButton("图片API") {
            selectImageAPI()
        }
        setDefaultNegative()
        show()
    }
}

fun selectImage(
    type: String = "image/*",
    identifier: String = CoderResult.IMAGE_IDENTIFIER,
    doSend: Boolean = true,
    selector: MixFileSelector = DecodeActivity.mixFileSelector,
    block: (ByteArray) -> ByteArray = { it },
) {
    selector.openSelect(arrayOf(type)) { data, fileName ->
        MixDialogBuilder(
            "上传中", properties = DialogProperties(
                dismissOnClickOutside = false
            )
        ).apply {
            setContent {
                val scope = rememberCoroutineScope()
                val progressContent = ProgressContent("上传中")
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    progressContent.LoadingContent()
                }
                LaunchedEffect(Unit) {
                    scope.launch(Dispatchers.IO) {
                        val password = getCurrentPassword()
                        val url = startUploadImage(
                            block(data),
                            password,
                            progressContent
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
                            doSend.isTrue {
                                sendResult(encodeText(encoderText.text, password))
                            }
                        }
                        closeDialog()
                    }
                }
            }
            setDefaultNegative()
            show()
        }
    }
}