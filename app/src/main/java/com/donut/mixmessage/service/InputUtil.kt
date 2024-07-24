package com.donut.mixmessage.service

import android.widget.Button
import android.widget.EditText
import androidx.compose.ui.text.input.TextFieldValue
import cn.vove7.auto.core.viewfinder.AcsNode
import cn.vove7.auto.core.viewfinder.ConditionGroup
import cn.vove7.auto.core.viewfinder.SmartFinder
import cn.vove7.auto.core.viewnode.ViewNode
import com.donut.mixmessage.app
import com.donut.mixmessage.decode.reOpenDecodeDialog
import com.donut.mixmessage.ui.component.encoder.encoderText
import com.donut.mixmessage.ui.component.routes.settings.routes.DETECT_TEXT_SEND
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.debug
import com.donut.mixmessage.util.common.isFalse
import com.donut.mixmessage.util.common.isNotNull
import com.donut.mixmessage.util.common.isNull
import com.donut.mixmessage.util.common.isTrue
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.common.toInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

var INPUT_EDITABLE_CACHE: ViewNode? = null
var SEND_BUTTON_CACHE: ViewNode? = null

var SEND_BUTTON_IDENTIFIER by cachedMutableOf("发送 send", "send_button_identifier")
var DIALOG_OPEN_IDENTIFIER by cachedMutableOf("mix mx", "dialog_open_identifier")
var SEARCH_BUTTON_TIMEOUT by cachedMutableOf(1000L * 10, "search_button_timeout")


fun setSendButtonIdentifier(value: String) {
    SEND_BUTTON_IDENTIFIER = value
}

fun ViewNode?.isValid(): Boolean {
    return this?.parent.isNotNull()
}

fun checkSendButtonTextValue(value: String) = checkSplitTextValue(value, SEND_BUTTON_IDENTIFIER)

fun checkSplitTextValue(value: String, identifier: String): Boolean {
    val matchValues = identifier.split(" ")
    for (matchValue in matchValues) {
        value.replace(" ", "").contentEquals(matchValue, true).isTrue {
            return true
        }
    }
    return false
}

fun checkDialogOpenTextValue(value: String) = checkSplitTextValue(value, DIALOG_OPEN_IDENTIFIER)

fun ViewNode.setText(text: String) {
    this.parent.isNotNull {
        this.trySetText(text)
    }
}

fun ViewNode?.getText(): String {
    this.isNull {
        return ""
    }
    this!!
    val node = this.node
    node.refresh()
    node.isTextSelectable.isFalse {
        return ""
    }
    return node.text?.toString() ?: ""
}

suspend fun trySendText(text: String, input: ViewNode?, originalInputText: String): String? {
    if (IS_ACS_ENABLED.isFalse()) {
        return "请先开启无障碍服务"
    }
    input.isNull {
        return "没有搜索到输入框"
    }
    input!!
    if (originalInputText.isEmpty() || checkDialogOpenTextValue(originalInputText)) {
        input.setText(text)
    } else input.appendText(" $text")
    delay(200)
    val currentText = input.getText()
    if (DETECT_TEXT_SEND
        && !currentText.contains(text)
        && currentText.isNotEmpty()
        && !checkDialogOpenTextValue(currentText)
    ) {
        return "内容超过字数限制: ${(if (originalInputText.isEmpty()) 0 else originalInputText.length + 1) + text.length}/${currentText.length}"
    }
    val button = findSendButton()
    debug("button: $button")
    button.isNull {
        return "没有搜索到发送按钮"
    }
    button?.click()
    if (DETECT_TEXT_SEND) {
        delay(200)
        //如果检测到未发送成功则代表发送按钮无效
        if (input.getText().isNotEmpty()) {
            SEND_BUTTON_CACHE = null
            return "发送失败,发送按钮无效!"
        }
    }
    return null
}


suspend fun inputAndSendText(text: String) {
    delay(200)
    val input = findInput()
    val originalInputText = input.getText()
    val sendResult = trySendText(text, input, originalInputText)
    if (sendResult != null) {
        input?.setText(originalInputText)
        reOpenDecodeDialog()
        delay(200)
        showToast(sendResult)
        return
    }
    withContext(Dispatchers.Main) {
        encoderText = TextFieldValue()
    }
}


private suspend fun findSendButton(): ViewNode? {
    SEND_BUTTON_CACHE.isValid().isTrue {
        return SEND_BUTTON_CACHE
    }
    return withTimeoutOrNull(SEARCH_BUTTON_TIMEOUT) {
        findViews().where { it.checkButton() }
            .findAll().toList().findSendButton().also {
                if (it != null) {
                    SEND_BUTTON_CACHE = it
                }
            }
    }
}

private suspend fun findInput(): ViewNode? {
    INPUT_EDITABLE_CACHE.isValid().isTrue {
        return INPUT_EDITABLE_CACHE
    }
    return withTimeoutOrNull(SEARCH_BUTTON_TIMEOUT) {
        findViews().where { it.isEditable }
            .findAll().toList().findSendInput().also {
                if (it != null) {
                    INPUT_EDITABLE_CACHE = it
                }
            }
    }
}

fun AcsNode.checkButtonText(): Boolean {
    return checkSendButtonTextValue(
        this.text?.toString() ?: ""
    )
}

fun AcsNode.checkButton(): Boolean {
    return checkButtonText() || checkButtonDesc() || checkButtonID()
}

fun AcsNode.checkButtonDesc(): Boolean {
    return checkSendButtonTextValue(this.contentDescription?.toString() ?: "")
}

fun AcsNode.checkButtonID(): Boolean {
    return (this.viewIdResourceName ?: "").contains("send")
}

fun List<ViewNode>.otherAppNodes(): List<ViewNode> {
    return this.filter { it.packageName != app.packageName }
}

fun ConditionGroup.otherAppNodes(): ConditionGroup {
    return and { it.packageName != app.packageName }
}

fun List<ViewNode>.findSendButton(): ViewNode? {
    return this.otherAppNodes().filter { it.node.checkButton() }.maxByOrNull {
        listOf(
            it.isClickable(),
            it.className.contentEquals(Button::class.java.name),
            it.node.checkButtonText(),
            it.node.checkButtonDesc(),
            it.node.checkButtonID(),
            it.node.isEnabled
        ).sumOf { condition -> condition.toInt() }
    }
}

fun List<ViewNode>.findSendInput(): ViewNode? {
    return this.otherAppNodes().filter { it.node.isEditable }.maxByOrNull {
        it.className.contentEquals(EditText::class.java.name).toInt()
    }

}

fun findViews(): ConditionGroup {
    return SmartFinder().otherAppNodes()
}