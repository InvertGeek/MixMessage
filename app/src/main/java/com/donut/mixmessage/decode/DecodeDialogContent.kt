package com.donut.mixmessage.decode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donut.mixmessage.currentActivity
import com.donut.mixmessage.decode.image.selectFile
import com.donut.mixmessage.service.inputAndSendText
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.ui.component.encoder.DecodeResultContent
import com.donut.mixmessage.ui.component.encoder.EncodeInputComponent
import com.donut.mixmessage.ui.component.encoder.encoderText
import com.donut.mixmessage.ui.component.routes.settings.useDefaultPrefix
import com.donut.mixmessage.util.common.copyToClipboard
import com.donut.mixmessage.util.common.isFalse
import com.donut.mixmessage.util.common.performHapticFeedBack
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.common.truncate
import com.donut.mixmessage.util.encode.encoders.ZeroWidthEncoder
import com.donut.mixmessage.util.encode.encoders.bean.CoderResult


fun openPrefixSelectDialog(callback: (String) -> Unit) {
    MixDialogBuilder("消息前缀").apply {
        var prefix by mutableStateOf("")
        setContent {
            OutlinedTextField(
                value = prefix,
                onValueChange = { newValue ->
                    prefix = newValue
                },
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("本次消息前缀") }
            )
        }
        setPositiveButton("确认发送") {
            callback(prefix)
            closeDialog()
        }
        show()
    }
}

fun sendResult(encodeResult: CoderResult) {
    performHapticFeedBack()
    if (encodeResult.textCoder == ZeroWidthEncoder && !useDefaultPrefix) {
        openPrefixSelectDialog {
            inputAndSendText(encodeResult.textWithPrefix(it))
            currentActivity.finish()
        }
        return
    }
    inputAndSendText(encodeResult.textWithPrefix())
    currentActivity.finish()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DecodeTextDialog(decodeResult: CoderResult) {

    DialogContainer {
        LaunchedEffect(true) {
            performHapticFeedBack()
        }
        if (!decodeResult.isFail) {
            AssistChip(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    MixDialogBuilder("原文").apply {
                        setContent {
                            Column {
                                SelectionContainer {
                                    Text(text = decodeResult.originText)
                                }
                                Text(
                                    text = decodeResult.getInfo(true),
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        setPositiveButton("一键复制") {
                            decodeResult.originText.copyToClipboard()
                            closeDialog()
                        }
                        show()
                    }
                },
                label = {
                    Text(text = decodeResult.originText.truncate(10))
                },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = "info",
                        modifier = Modifier.size(AssistChipDefaults.IconSize)
                    )
                }
            )
            DecodeResultContent(decodeResult = decodeResult)
        }
        EncodeInputComponent(
            noScroll = true,
            showPasteButton = false,
            useTextButton = true
        ) { encodeResult ->
            val hasText = encodeResult.textWithPrefix().isNotEmpty()
            Button(
                onClick = {
                    hasText.isFalse {
                        selectFile()
                        return@Button
                    }
                    sendResult(encodeResult)
                },
                elevation = ButtonDefaults.elevatedButtonElevation(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = if (hasText) "一键发送" else "选择文件")
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {

                OutlinedButton(
                    onClick = {
                        performHapticFeedBack()
                        DecodeActivity.LAST_FORCE_CLOSE = System.currentTimeMillis()
                        showToast("3秒内不会再显示此窗口")
                        currentActivity.finish()
                    },
                    modifier = Modifier.weight(1f)

                ) {
                    Text(
                        text = "3秒内不再弹出"
                    )
                }
                ElevatedButton(
                    onClick = {
                        performHapticFeedBack()
                        encoderText = TextFieldValue()
                        currentActivity.finish()
                    },
                    modifier = Modifier.weight(1f)

                ) {
                    Text(
                        text = if (encodeResult.textWithPrefix()
                                .isNotEmpty()
                        ) "关闭并清空" else "关闭"
                    )
                }
            }
        }
    }
}