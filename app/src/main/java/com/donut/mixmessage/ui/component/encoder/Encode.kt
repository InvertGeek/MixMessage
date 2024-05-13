package com.donut.mixmessage.ui.component.encoder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donut.mixmessage.ui.component.common.ClearableTextField
import com.donut.mixmessage.ui.component.routes.password.showPasswordsDialog
import com.donut.mixmessage.ui.component.routes.settings.selectDefaultEncoder
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.copyToClipboard
import com.donut.mixmessage.util.common.readClipBoardText
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.encode.DEFAULT_ENCODER
import com.donut.mixmessage.util.encode.DEFAULT_PASSWORD
import com.donut.mixmessage.util.encode.encodeText
import com.donut.mixmessage.util.encode.encoders.ZeroWidthEncoder
import com.donut.mixmessage.util.encode.encoders.bean.CoderResult


var encoderText by mutableStateOf(TextFieldValue())

var encodeResult by mutableStateOf(encodeText(encoderText.text))

var copyWhenRefresh by cachedMutableOf(true, "copy_when_refresh")

fun setEnableCopyWhenRefresh(enable: Boolean) {
    copyWhenRefresh = enable
}

@Composable
fun EncodeButton(
    text: String,
    modifier: Modifier = Modifier,
    useTextButton: Boolean = false,
    onClick: () -> Unit
) {
    if (useTextButton) {
        TextButton(onClick = onClick, modifier = modifier) {
            Text(text = text)
        }
    } else {
        Button(onClick = onClick, modifier = modifier) {
            Text(text = text)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EncodeInputComponent(
    noScroll: Boolean = false,
    showPasteButton: Boolean = true,
    useTextButton: Boolean = false,
    extra: @Composable ((CoderResult) -> Unit)? = null,
) {
    val maxLines = if (noScroll) Int.MAX_VALUE else 5

    ClearableTextField(
        value = encoderText,
        onValueChange = { newValue ->
            encoderText = newValue
        },
        maxLines = maxLines,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("输入内容") }
    )

    LaunchedEffect(
        encoderText, DEFAULT_ENCODER, ZeroWidthEncoder.encodeResultPrefix,
        DEFAULT_PASSWORD
    ) {
        encodeResult = encodeText(encoderText.text)
    }

    TextField(
        value = TextFieldValue(encodeResult.textWithPrefix()),
        onValueChange = {},
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        maxLines = maxLines,
        placeholder = { Text("编码结果") },
        supportingText = {
            Text(text = encodeResult.getInfo())
        }
//            label = { Text("结果") }
    )
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 0.dp),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        EncodeButton("刷新结果", useTextButton = useTextButton) {
            encodeResult = encodeText(encoderText.text)
            if (!copyWhenRefresh) {
                return@EncodeButton showToast("刷新成功")
            }
            encodeResult.textWithPrefix().copyToClipboard()
            showToast("刷新并复制成功")
        }

        EncodeButton("复制结果", useTextButton = useTextButton) {
            encodeResult.textWithPrefix().copyToClipboard()
            showToast("复制结果成功")
        }

        EncodeButton(text = "加密方法", useTextButton = useTextButton) {
            selectDefaultEncoder()
        }
        EncodeButton("密钥列表", useTextButton = useTextButton) {
            showPasswordsDialog()
        }
        if (showPasteButton) {
            EncodeButton("粘贴内容", useTextButton = useTextButton) {
                encoderText = TextFieldValue(readClipBoardText())
            }
            EncodeButton("清空内容", useTextButton = useTextButton) {
                encoderText = TextFieldValue()
            }
        }
        extra?.invoke(encodeResult)
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EncodeComponent() {
    // 创建两个可变状态来保存输入框中的文本内容
//    var encodeText by remember { mutableStateOf(TextFieldValue()) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "编码", fontSize = 20.sp, fontWeight = FontWeight.Bold) // 指定字体大小为 20sp)
        EncodeInputComponent() {

        }

    }
}