package com.donut.mixmessage.ui.component.encoder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donut.mixmessage.MainActivity
import com.donut.mixmessage.decode.image.selectFile
import com.donut.mixmessage.ui.component.common.ClearableTextField
import com.donut.mixmessage.ui.component.routes.password.showPasswordsDialog
import com.donut.mixmessage.ui.component.routes.settings.selectDefaultEncoder
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.copyToClipboard
import com.donut.mixmessage.util.common.performHapticFeedBack
import com.donut.mixmessage.util.common.readClipBoardText
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.encode.DEFAULT_ENCODER
import com.donut.mixmessage.util.encode.DEFAULT_PASSWORD
import com.donut.mixmessage.util.encode.RSAUtil
import com.donut.mixmessage.util.encode.USE_TIME_LOCK
import com.donut.mixmessage.util.encode.encodeText
import com.donut.mixmessage.util.encode.encoders.ZeroWidthEncoder
import com.donut.mixmessage.util.encode.encoders.bean.CoderResult
import com.donut.mixmessage.util.encode.encoders.bean.USE_SIMPLE_MODE


var encoderText by mutableStateOf(TextFieldValue())

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
    val clickAction = {
        performHapticFeedBack()
        onClick()
    }
    if (useTextButton) {
        TextButton(onClick = clickAction, modifier = modifier) {
            Text(text = text)
        }
    } else {
        Button(onClick = clickAction, modifier = modifier) {
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

    var encodeResult by remember(
        encoderText,
        DEFAULT_ENCODER,
        USE_SIMPLE_MODE,
        ZeroWidthEncoder.encodePrefix,
        DEFAULT_PASSWORD,
        USE_TIME_LOCK
    ) {
        mutableStateOf(encodeText(encoderText.text))
    }

    val maxLines = if (noScroll) Int.MAX_VALUE else 5

    ClearableTextField(
        value = encoderText,
        onValueChange = { newValue ->
            encoderText = newValue
        },
        maxLines = maxLines,
        modifier = Modifier.fillMaxWidth(),
        label = "输入内容"
    )

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
            EncodeButton("选择文件", useTextButton = useTextButton) {
                selectFile(false, MainActivity.mixFileSelector)
            }
        }
        extra?.invoke(encodeResult)
    }
    if (showPasteButton) {
        AnimatedVisibility(
            visible = encoderText.text.contentEquals("rsa", true),
            enter = slideInVertically { -it },
            exit = slideOutVertically { -it }
        ) {
            Button(
                onClick = {
                    encodeText(
                        CoderResult.PUBLIC_KEY_IDENTIFIER + RSAUtil.publicKeyStr,
                        "123"
                    ).also {
                        it.textWithPrefix().copyToClipboard()
                    }
                },
                elevation = ButtonDefaults.elevatedButtonElevation(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "复制公钥")
            }
        }
    }

}


@Composable
fun EncodeComponent() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "编码", fontSize = 20.sp, fontWeight = FontWeight.Bold) // 指定字体大小为 20sp)
        EncodeInputComponent()
    }
}