package com.donut.mixmessage.decode.image

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import com.donut.mixmessage.decode.DecodeActivity
import com.donut.mixmessage.decode.sendResult
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.ui.component.common.SingleSelectItemList
import com.donut.mixmessage.ui.component.encoder.encoderText
import com.donut.mixmessage.ui.component.routes.settings.routes.ENABLE_IMAGE_COMPRESS
import com.donut.mixmessage.ui.component.routes.settings.routes.selectImageAPI
import com.donut.mixmessage.util.common.AsyncEffect
import com.donut.mixmessage.util.common.isNull
import com.donut.mixmessage.util.common.isTrue
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.encode.encodeText
import com.donut.mixmessage.util.encode.encoders.bean.CoderResult
import com.donut.mixmessage.util.encode.encryptAES
import com.donut.mixmessage.util.encode.generateRandomByteArray
import com.donut.mixmessage.util.image.startUploadImage
import com.donut.mixmessage.util.image.toImageData
import com.donut.mixmessage.util.objects.MixFileSelector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun selectFile(doSend: Boolean = true, selector: MixFileSelector = DecodeActivity.mixFileSelector) {
    MixDialogBuilder("文件类型").apply {
        setContent {
            SingleSelectItemList(listOf("图片", "视频", "文件"), null) {
                when (it) {
                    "图片" -> selectImage(
                        doSend = doSend,
                        selector = selector
                    ) { data -> if (ENABLE_IMAGE_COMPRESS) data.toImageData() else data }

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
                    val password = generateRandomByteArray(16)
                    val encryptedFileData = encryptAES(block(data), password)
                    val url = startUploadImage(
                        encryptedFileData,
                        progressContent
                    )
                    url.isNull {
                        closeDialog()
                        showToast("上传失败")
                        return@AsyncEffect
                    }
                    showToast("上传成功!")
                    withContext(Dispatchers.Main) {
                        encoderText =
                            TextFieldValue(
                                CoderResult.media(
                                    url!!,
                                    fileName,
                                    password,
                                    encryptedFileData.size,
                                    identifier,
                                )
                            )
                        doSend.isTrue {
                            sendResult(encodeText(encoderText.text))
                        }
                    }
                    closeDialog()
                }
            }
            setDefaultNegative()
            show()
        }
    }
}