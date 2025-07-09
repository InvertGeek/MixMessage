package com.donut.mixmessage.ui.component.encoder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.donut.mixmessage.ui.component.common.ClearableTextField
import com.donut.mixmessage.ui.component.encoder.components.DecodeResultContent
import com.donut.mixmessage.ui.component.encoder.components.TextCoderResultContent
import com.donut.mixmessage.util.common.copyToClipboard
import com.donut.mixmessage.util.common.readClipBoardText
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.encode.decodeText
import com.donut.mixmessage.util.encode.encoders.bean.CoderResult


@Composable
fun DecodeTextResultComponent(noScroll: Boolean = false, decodeResult: CoderResult) {
    val decodeResultText = if (decodeResult.isFail) "解码失败" else decodeResult.text

    val isError = decodeResult.isFail

    if (noScroll) {
        TextCoderResultContent(text = decodeResultText, decodeResult = decodeResult)
        return
    }

    TextField(
        value = TextFieldValue(decodeResultText),
        onValueChange = {},
        modifier = Modifier.fillMaxWidth(),
        readOnly = true,
        maxLines = 5,
        isError = isError,
        placeholder = { Text("解码结果") },
        supportingText = {
            Text(
                text = decodeResult.getInfo()
            )
        }
    )

    Button(
        onClick = {
            decodeResultText.copyToClipboard()
            showToast("复制结果成功")
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "复制解码结果")
    }

}

@Composable
fun DecodeComponent() {

    var inputText by remember {
        mutableStateOf(TextFieldValue())
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "解码", fontSize = 20.sp, fontWeight = FontWeight.Bold) // 指定字体大小为 20sp)
        ClearableTextField(
            value = inputText,
            onValueChange = { newValue ->
                inputText = newValue
            },
            maxLines = 5,
            modifier = Modifier.fillMaxWidth(),
            label = "输入内容"
        )

        val decodeResult by remember(inputText.text) {
            mutableStateOf(decodeText(inputText.text))
        }

        DecodeResultContent(decodeResult = decodeResult, noScroll = false)

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                inputText = TextFieldValue(readClipBoardText())
            }) {
            Text(text = "解码剪贴板")
        }
    }
}
