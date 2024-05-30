package com.donut.mixmessage.ui.component.encoder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donut.mixmessage.decode.image.FileContent
import com.donut.mixmessage.decode.image.ImageContent
import com.donut.mixmessage.decode.image.VideoContent
import com.donut.mixmessage.ui.component.common.ClearableTextField
import com.donut.mixmessage.util.common.UnitBlock
import com.donut.mixmessage.util.common.copyToClipboard
import com.donut.mixmessage.util.common.performHapticFeedBack
import com.donut.mixmessage.util.common.readClipBoardText
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.encode.decodeText
import com.donut.mixmessage.util.encode.encoders.bean.CoderResult

@Composable
fun DecodeTextResultComponent(noScroll: Boolean = false, decodeResult: CoderResult) {
    val decodeResultText = decodeResult.text
        .ifEmpty { "解码失败" }

    val isError = decodeResult.isFail

    if (noScroll) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0x2F69D2FF),
            ),
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                SelectionContainer {
                    Text(text = decodeResultText)
                }
                Text(text = decodeResult.getInfo(), fontSize = 10.sp, color = Color.Gray)
            }
        }
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
            performHapticFeedBack()
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
            label = { Text("输入内容") }
        )

        val decodeResult by remember(inputText.text) {
            mutableStateOf(decodeText(inputText.text))
        }

        DecodeResultContent(decodeResult = decodeResult, noScroll = false)

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                performHapticFeedBack()
                inputText = TextFieldValue(readClipBoardText())
            }) {
            Text(text = "解码剪贴板")
        }
    }
}

@Composable
fun DecodeResultContent(decodeResult: CoderResult, noScroll: Boolean = true) {
    @Composable
    fun Card(block: @Composable UnitBlock) {
        ElevatedCard(
            colors = CardDefaults.cardColors(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(200.dp)
        ) {
            block()
        }
    }
    decodeResult.isImage { url, fileName ->
        Card {
            ImageContent(imageUrl = url, decodeResult.password, fileName)
        }
        return
    }

    decodeResult.isVideo { url, fileName ->
        Card {
            VideoContent(url, decodeResult.password, fileName)
        }
        return
    }
    decodeResult.isFile { url, fileName ->
        Card {
            FileContent(url, decodeResult.password, fileName)
        }
        return
    }
    DecodeTextResultComponent(decodeResult = decodeResult, noScroll = noScroll)
}