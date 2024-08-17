package com.donut.mixmessage.ui.component.routes.password

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.util.common.copyToClipboard
import com.donut.mixmessage.util.common.formatTime
import com.donut.mixmessage.util.common.isNotNull
import com.donut.mixmessage.util.common.isTrue
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.encode.PassKey
import com.donut.mixmessage.util.encode.RoundKey
import com.donut.mixmessage.util.encode.getCurrentPass
import com.donut.mixmessage.util.encode.manualAddPassword
import com.donut.mixmessage.util.encode.removePassword
import com.donut.mixmessage.util.encode.setDefaultPassword

fun openCommonConfirmDialog(actionText: String, onConfirm: () -> Unit) {

    MixDialogBuilder("提示").apply {
        setContent {
            Text(text = "确定${actionText}吗？")
        }
        setPositiveButton("确定") {
            onConfirm()
            closeDialog()
        }
        setNegativeButton("取消") {
            closeDialog()
        }
        show()
    }
}


fun openPasswordDialog(passwordText: String) {
    MixDialogBuilder("查看密钥").apply {
        setContent {
            val currentPassword = getCurrentPass(passwordText)
            val key = PassKey(currentPassword)
            var roundKey: RoundKey? = null
            if (key.isRoundKey()) {
                roundKey = RoundKey(key)
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                SelectionContainer {
                    Text(text = currentPassword)
                }
                roundKey.isNotNull {
                    roundKey!!
                    Text(
                        text = "轮换密钥 ${roundKey.name} 信息: ",
                        color = Color.Gray
                    )
                    Text(
                        text = "每 ${roundKey.hour} 小时轮换一次 已轮换次数: ${roundKey.round}",
                        color = Color.Gray
                    )
                    if (roundKey.getTimeDiff() > 0) {
                        Text(
                            text = "距离当前时间剩余轮换次数: ${roundKey.getTimeDiff()}",
                            color = Color.Gray
                        )
                    }
                    Text(
                        text = "密钥生成时间: ${formatTime(roundKey.date)}",
                        color = Color.Gray
                    )
                    Text(
                        text = "现在能解密 ${formatTime(roundKey.getDecodeBeforeTime())} 之后发送的信息",
                        color = Color.Gray
                    )
                    Text(
                        text = "现在发送的信息在 ${formatTime(roundKey.getDecodeAfterTime())} 之后将无法解密",
                        color = Color.Gray
                    )
                }
            }
        }
        setPositiveButton("复制密钥") {
            it()
            passwordText.copyToClipboard()
        }
        setNeutralButton("设为默认加密密钥") {
            setDefaultPassword(passwordText)
            showToast("设置成功")
            it()
        }
        if (!passwordText.contentEquals("123")) {
            setNegativeButton("删除密钥") {
                openCommonConfirmDialog("删除该密钥") {
                    removePassword(passwordText)
                    showToast("删除成功")
                    it()
                }
            }
        }
        show()
    }
}


fun openAddPasswordDialog() {
    MixDialogBuilder("添加密钥").apply {
        var passValue by mutableStateOf(TextFieldValue())
        setContent {
            OutlinedTextField(value = passValue, onValueChange = {
                passValue = it
            }, label = {
                Text(text = "输入密钥")
            }, modifier = Modifier.fillMaxWidth())
        }
        setNegativeButton("取消") {
            closeDialog()
        }
        setPositiveButton("确定") {
            manualAddPassword(passValue.text).isTrue {
                closeDialog()
            }
        }
        show()
    }
}