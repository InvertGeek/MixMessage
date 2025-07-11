package com.donut.mixmessage.ui.component.encoder.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donut.mixmessage.decode.sendResult
import com.donut.mixmessage.ui.component.common.ClearableTextField
import com.donut.mixmessage.ui.component.common.LabelSwitch
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.ui.theme.colorScheme
import com.donut.mixmessage.util.common.copyToClipboard
import com.donut.mixmessage.util.common.hashSHA256
import com.donut.mixmessage.util.common.isTrue
import com.donut.mixmessage.util.encode.RSAUtil
import com.donut.mixmessage.util.encode.encodeText
import com.donut.mixmessage.util.encode.encoders.bean.CoderResult
import com.donut.mixmessage.util.encode.manualAddPassword
import com.donut.mixmessage.util.encode.setDefaultPassword
import java.security.PublicKey


@Composable
fun RSAEncryptComponent(
    decodeResult: CoderResult,
    publicKey: PublicKey,
    noScroll: Boolean = true
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(0.dp, 10.dp)
    ) {
        val fingerPrint = remember(publicKey) {
            publicKey.encoded.hashSHA256()
        }
        TextCoderResultContent(
            text = """
                当前信息为公钥,指纹如下:
                $fingerPrint
            """.trimIndent(),
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

        fun getEncodeResult(): CoderResult {
            val encryptedText = RSAUtil.encryptRSA(text.text, publicKey)
            return encodeText(CoderResult.PRIVATE_MESSAGE_IDENTIFIER + encryptedText, "123")
        }

        if (noScroll) {
            ElevatedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = text.text.trim().isNotEmpty(),
                onClick = {
                    sendResult(getEncodeResult())
                },
            ) {
                Text(text = "加密并发送")
            }
        } else {
            ElevatedButton(
                modifier = Modifier.fillMaxWidth(),
                enabled = text.text.trim().isNotEmpty(),
                onClick = {
                    getEncodeResult().textWithPrefix.copyToClipboard()
                },
            ) {
                Text(text = "复制加密结果")
            }
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
            "解密RSA内容失败,此条消息未使用你的公钥进行加密",
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

                        var refreshRSA by remember {
                            mutableStateOf(true)
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.Center,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            LabelSwitch(checked = setDefault, label = "设为默认: ") {
                                setDefault = it
                            }
                            LabelSwitch(checked = refreshRSA, label = "刷新RSA密钥对: ") {
                                refreshRSA = it
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(onClick = {
                                    manualAddPassword(password).isTrue {
                                        setDefault.isTrue {
                                            setDefaultPassword(password)
                                        }
                                        refreshRSA.isTrue {
                                            RSAUtil.regenerateKeyPair()
                                        }
                                        closeDialog()
                                    }
                                }) {
                                    Text(text = "确认")
                                }
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