package com.donut.mixmessage.ui.component.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.donut.mixmessage.util.common.addComposeView
import com.donut.mixmessage.util.common.performHapticFeedBack

class MixDialogBuilder(private var title: String) {
    private var content = @Composable {}
    private var positiveButton = @Composable {}
    private var negativeButton = @Composable {}
    private var neutralButton = @Composable {}
    private var close: () -> Unit = {}

    fun setContent(content: @Composable () -> Unit) {
        this.content = content
    }

    fun closeDialog() {
        close()
    }

    fun setDefaultNegative(text: String = "取消") {
        setNegativeButton(text) { closeDialog() }
    }

    @Composable
    private fun BuildButton(text: String, callBack: (close: () -> Unit) -> Unit) {
        return TextButton(onClick = {
            callBack(close)
        }) {
            Text(text = text)
        }
    }

    fun setPositiveButton(text: String, callBack: (close: () -> Unit) -> Unit) {
        positiveButton = {
            BuildButton(text = text, callBack)
        }
    }

    fun setNegativeButton(text: String, callBack: (close: () -> Unit) -> Unit) {
        negativeButton = {
            BuildButton(text = text, callBack)
        }
    }

    fun setBottomContent(text: String, callBack: (close: () -> Unit) -> Unit) {
        neutralButton = {
            BuildButton(text = text, callBack)
        }
    }

    fun setBottomContent(content: @Composable () -> Unit) {
        neutralButton = {
            content()
        }
    }

    fun show() {
        close = showAlertDialog(title, content, positiveButton, negativeButton, neutralButton)
    }
}


fun showAlertDialog(
    title: String,
    content: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit = {},
    dismissButton: (@Composable () -> Unit)? = null,
    neutralButton: @Composable () -> Unit = {},
): () -> Unit {
    performHapticFeedBack()
    return addComposeView { removeView ->
        val mixedDismissButton = @Composable {
            neutralButton()
            (dismissButton ?: {
                TextButton(onClick = {
                    removeView()
                }) {
                    Text(text = "关闭")
                }
            })()
        }
        AlertDialog(
            modifier = Modifier
                .systemBarsPadding()
                .heightIn(0.dp, 600.dp),
            title = {
                Text(text = title, fontWeight = FontWeight.Bold)
            },
            onDismissRequest = {
                removeView()
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState()),
                ) {
                    content()
                }
            },
            confirmButton = confirmButton,
            dismissButton = mixedDismissButton
        )
    }
}