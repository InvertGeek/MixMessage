package com.donut.mixmessage.ui.component.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.donut.mixmessage.util.common.addComposeView
import com.donut.mixmessage.util.common.performHapticFeedBack

class MaterialDialogBuilder(private var title: String) {
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

    fun setNeutralButton(text: String, callBack: (close: () -> Unit) -> Unit) {
        neutralButton = {
            BuildButton(text = text, callBack)
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
            title = {
                Text(text = title, fontWeight = FontWeight.Bold)
            },
            onDismissRequest = {
                removeView()
            },
            text = content,
            confirmButton = confirmButton,
            dismissButton = mixedDismissButton
        )
    }
}