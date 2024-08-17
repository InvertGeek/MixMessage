package com.donut.mixmessage.ui.component.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.window.DialogProperties
import com.donut.mixmessage.util.common.addComposeView

class MixDialogBuilder(
    private var title: String,
    private val tag: String = title,
    private val properties: DialogProperties = DialogProperties(
//        usePlatformDefaultWidth = false
    )
) {
    private var content = @Composable {}
    private var positiveButton = @Composable {}
    private var negativeButton = @Composable {}
    private var neutralButton = @Composable {}
    private var close: () -> Unit = {}

    companion object {
        val dialogCache = mutableMapOf<String, () -> Unit>()
    }

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

    fun setNeutralButton(text: String, callBack: (close: () -> Unit) -> Unit) {
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
        close = showAlertDialog(
            title,
            content,
            positiveButton,
            negativeButton,
            neutralButton,
            properties
        )
        dialogCache[tag]?.invoke()
        dialogCache[tag] = close
    }
}


fun showAlertDialog(
    title: String,
    content: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit = {},
    dismissButton: (@Composable () -> Unit)? = null,
    neutralButton: @Composable () -> Unit = {},
    properties: DialogProperties = DialogProperties()
): () -> Unit {
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
            properties = properties,
            title = {
                Text(text = title, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
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