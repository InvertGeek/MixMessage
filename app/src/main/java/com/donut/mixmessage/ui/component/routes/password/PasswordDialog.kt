package com.donut.mixmessage.ui.component.routes.password

import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.util.common.copyToClipboard
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.encode.DEFAULT_PASSWORD
import com.donut.mixmessage.util.encode.PASSWORDS
import com.donut.mixmessage.util.encode.addPassword
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
            SelectionContainer {
                Text(text = passwordText)
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
        setNegativeButton("删除密钥") {
            if (passwordText == "123" || passwordText == DEFAULT_PASSWORD) {
                showToast("默认密钥不能删除")
                return@setNegativeButton
            }
            openCommonConfirmDialog("删除该密钥") {
                removePassword(passwordText)
                showToast("删除成功")
                it()
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
            })
        }
        setNegativeButton("取消") {
            closeDialog()
        }
        setPositiveButton("确定") {
            if (passValue.text.isEmpty()) {
                showToast("密钥不能为空")
                return@setPositiveButton
            }
            if (PASSWORDS.contains(passValue.text)) {
                showToast("密钥已存在")
                return@setPositiveButton
            }
            addPassword(passValue.text)
            showToast("添加成功")
            closeDialog()
        }
        show()
    }
}