package com.donut.mixmessage.ui.component.encoder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donut.mixmessage.decode.image.FileContent
import com.donut.mixmessage.decode.image.ImageContent
import com.donut.mixmessage.decode.image.VideoContent
import com.donut.mixmessage.decode.sendResult
import com.donut.mixmessage.ui.component.common.ClearableTextField
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.ui.theme.colorScheme
import com.donut.mixmessage.util.common.TipText
import com.donut.mixmessage.util.common.UnitBlock
import com.donut.mixmessage.util.common.copyToClipboard
import com.donut.mixmessage.util.common.isTrue
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.encode.RSAUtil
import com.donut.mixmessage.util.encode.addPassword
import com.donut.mixmessage.util.encode.encodeText
import com.donut.mixmessage.util.encode.encoders.bean.CoderResult
import com.donut.mixmessage.util.encode.setDefaultPassword
import java.security.PublicKey

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
                Text(text = text, color = color)
            }
            TipText(content = tip) {
                MixDialogBuilder("复制内容到剪贴板?").apply {
                    setDefaultNegative()
                    setPositiveButton("确定") {
                        text.copyToClipboard()
                        closeDialog()
                    }
                    show()
                }
            }
        }
    }
}

@Composable
fun TextCoderResultContent(
    text: String,
    decodeResult: CoderResult,
    color: Color = Color.Unspecified
) {
    CardTextArea(text, decodeResult.getInfo(), color)
}

@Composable
fun RSAEncryptComponent(decodeResult: CoderResult, publicKey: PublicKey) {
    val decodeResultText = remember {
        decodeResult.text.substring(CoderResult.PUBLIC_KEY_IDENTIFIER.length)
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(0.dp, 10.dp)
    ) {
        TextCoderResultContent(
            text = "公钥: $decodeResultText",
            decodeResult = decodeResult,
            color = colorScheme.primary
        )

        var text by remember {
            mutableStateOf(TextFieldValue())
        }
        ClearableTextField(
            value = text,
            label = "输入内容",
        ) {
            text = it
        }
        Text(
            text = "将会使用此公钥进行非对称加密,只有对方的私钥才能解密",
            color = colorScheme.primary,
            fontSize = 12.sp
        )
        ElevatedButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = text.text.trim().isNotEmpty(),
            onClick = {
                val encryptedText = RSAUtil.encryptRSA(text.text, publicKey)
                encodeText(CoderResult.PRIVATE_MESSAGE_IDENTIFIER + encryptedText, "123").also {
                    sendResult(it)
                }
            },
        ) {
            Text(text = "加密并发送")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RSADecryptComponent(decodeResult: CoderResult) {

    val decodeResultText = remember {
        decodeResult.text.substring(CoderResult.PRIVATE_MESSAGE_IDENTIFIER.length)
    }

    val password = remember {
        RSAUtil.decryptRSA(decodeResultText)
    }

    password.isEmpty().isTrue {
        CardTextArea(
            "解密失败",
            "解密RSA内容失败,可能对方使用了未知的公钥",
            color = colorScheme.error
        )
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(0.dp, 10.dp)
    ) {

        CardTextArea(
            password,
            "RSA加密内容,此内容使用您的公钥加密,只有你能解密查看",
            color = colorScheme.error
        )

        ElevatedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                MixDialogBuilder("确定添加到密钥列表?").apply {
                    setContent {
                        Text(text = "密钥: $password")
                    }
                    setBottomContent {
                        var setDefault by remember {
                            mutableStateOf(false)
                        }
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.Center,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FlowRow(
                                modifier = Modifier,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = "设为默认: ",
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                                Switch(
                                    checked = setDefault,
                                    onCheckedChange = {
                                        setDefault = it
                                    },
                                )
                            }
                            Button(onClick = {
                                addPassword(password)
                                setDefault.isTrue {
                                    setDefaultPassword(password)
                                }
                                showToast("添加成功!")
                                closeDialog()
                            }) {
                                Text(text = "确认")
                            }
                        }
                    }
                    show()
                }
            },
        ) {
            Text(text = "添加到密钥列表")
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
    decodeResult.isPublicKey { publicKey ->
        Card {
            RSAEncryptComponent(decodeResult, publicKey = publicKey)
        }
        return
    }
    decodeResult.isPrivateMessage {
        RSADecryptComponent(decodeResult)
        return
    }
    DecodeTextResultComponent(decodeResult = decodeResult, noScroll = noScroll)

}