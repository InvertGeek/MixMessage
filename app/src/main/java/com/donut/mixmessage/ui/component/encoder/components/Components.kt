package com.donut.mixmessage.ui.component.encoder.components

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.donut.mixfile.server.core.utils.encodeURL
import com.donut.mixfile.server.core.utils.resolveMixShareInfo
import com.donut.mixmessage.decode.file.FileContent
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.ui.component.encoder.DecodeTextResultComponent
import com.donut.mixmessage.ui.theme.colorScheme
import com.donut.mixmessage.util.common.TipText
import com.donut.mixmessage.util.common.UnitBlock
import com.donut.mixmessage.util.common.copyToClipboard
import com.donut.mixmessage.util.common.copyWithDialog
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.encode.encoders.bean.CoderResult
import com.donut.mixmessage.util.mixfile.server
import io.ktor.http.URLBuilder

@Composable
fun CardTextArea(text: String, tip: String, color: Color = Color.Unspecified) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x2F69D2FF),
        ),
    ) {
        Column(modifier = Modifier) {
            SelectionContainer(modifier = Modifier.padding(10.dp)) {
                HighlightAndClickableUrls(text, color)
            }
            TipText(content = tip) {
                text.copyWithDialog("内容")
            }
        }
    }
}

@Composable
fun HighlightAndClickableUrls(text: String, color: Color) {
    val context = LocalContext.current
    val urIPattern =
        "([a-zA-Z0-9]+)://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}(\\.)?[a-zA-Z0-9()]{1,6}(\\b)?([-a-zA-Z0-9()@:%_+.~#?&/=]*)".toRegex()


    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        urIPattern.findAll(text).forEach { matchResult ->
            val startIndex = matchResult.range.first
            val endIndex = matchResult.range.last + 1

            // 添加之前的普通文本
            withStyle(style = SpanStyle(fontSize = 16.sp, color = color)) {
                append(text.substring(lastIndex, startIndex))
            }

            // 添加高亮的 URL 并且添加点击注释
            pushStringAnnotation(tag = "URI", annotation = matchResult.value)
            withStyle(
                style = SpanStyle(
                    color = colorScheme.primary,
                    fontSize = 16.sp,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(matchResult.value)
            }
            pop()

            lastIndex = endIndex
        }
        // 添加剩余的普通文本
        withStyle(style = SpanStyle(fontSize = 16.sp, color = color)) {
            append(text.substring(lastIndex))
        }
    }

    ClickableText(
        style = TextStyle(),
        text = annotatedString,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "URI", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    MixDialogBuilder("打开URI?").apply {
                        setContent {
                            Text(text = annotation.item, color = colorScheme.primary)
                        }
                        setNegativeButton("复制") {
                            annotation.item.copyToClipboard()
                            closeDialog()
                        }
                        setPositiveButton("确定") {
                            closeDialog()
                            try {
                                val intent =
                                    Intent(Intent.ACTION_VIEW, annotation.item.toUri())
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                showToast("没有可以处理此URI的应用")
                            }
                        }
                        show()
                    }
                }
        }
    )
}


@Composable
fun TextCoderResultContent(
    text: String,
    decodeResult: CoderResult,
    color: Color = Color.Unspecified
) {
    CardTextArea(
        text,
        decodeResult.getInfo(),
        color = color
    )
}

@Composable
fun DecodeResultContent(decodeResult: CoderResult, noScroll: Boolean = true) {

    @Composable
    fun Card(block: @Composable () -> Unit) {
        ElevatedCard(
            colors = CardDefaults.cardColors(),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(200.dp)
        ) {
            block()
        }
    }

    val mixShareInfo = resolveMixShareInfo(decodeResult.text)

    if (mixShareInfo != null) {
        FileContent(URLBuilder("http://127.0.0.1:${server.serverPort}/api/download/${mixShareInfo.fileName.encodeURL()}").apply {
            parameters.apply {
                append("s", mixShareInfo.toString())
            }
        }.toString(), mixShareInfo.fileName, mixShareInfo.fileSize)
        return
    }

    decodeResult.isPublicKey { publicKey ->
        Card {
            RSAEncryptComponent(decodeResult, publicKey = publicKey, noScroll)
        }
        return
    }

    decodeResult.isPrivateMessage {
        RSADecryptComponent(decodeResult)
        return
    }

    DecodeTextResultComponent(decodeResult = decodeResult, noScroll = noScroll)

}