package com.donut.mixmessage.service

import cn.vove7.auto.core.viewfinder.AcsNode
import cn.vove7.auto.core.viewfinder.ConditionGroup
import cn.vove7.auto.core.viewfinder.SmartFinder
import cn.vove7.auto.core.viewnode.ViewNode
import com.donut.mixmessage.app
import com.donut.mixmessage.appScope
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.isFalse
import com.donut.mixmessage.util.common.isNotNull
import com.donut.mixmessage.util.common.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

const val BUTTON_CLASS_NAME = "android.widget.Button"
const val EDIT_TEXT_CLASS_NAME = "android.widget.EditText"

var INPUT_EDITABLE_CACHE: ViewNode? = null
var SEND_BUTTON_CACHE: ViewNode? = null

var SEND_BUTTON_IDENTIFIER by cachedMutableOf("发送", "send_button_identifier")
var DIALOG_OPEN_IDENTIFIER by cachedMutableOf("mix", "dialog_open_identifier")
var SEARCH_BUTTON_TIMEOUT by cachedMutableOf(1000L * 10, "search_button_timeout")


fun setSendButtonIdentifier(value: String) {
    SEND_BUTTON_IDENTIFIER = value
}

fun checkSendButtonTextValue(value: String) = checkSplitTextValue(value, SEND_BUTTON_IDENTIFIER)

fun checkSplitTextValue(value: String, identifier: String): Boolean {
    val matchValues = identifier.split(" ")
    for (matchValue in matchValues) {
        if (value.replace(" ", "").contentEquals(matchValue, true)) {
            return true
        }
    }
    return false
}

fun checkDialogOpenTextValue(value: String) = checkSplitTextValue(value, DIALOG_OPEN_IDENTIFIER)

fun ViewNode.setText(text: String) {
    if (this.parent != null) {
        this.trySetText(text)
    }
}


fun inputAndSendText(text: String) {

    if (IS_ACS_ENABLED.isFalse()) {
        return showToast("请先开启无障碍服务")
    }

    appScope.launch(Dispatchers.IO) {
        delay(100)
        findInput()?.let {
            it.setText(text)
            delay(50)
            findSendButton()?.click()
        }
    }
}


private suspend fun findSendButton(): ViewNode? {
    if (SEND_BUTTON_CACHE?.parent.isNotNull()) {
        return SEND_BUTTON_CACHE
    }
    return withTimeoutOrNull(SEARCH_BUTTON_TIMEOUT) {
        return@withTimeoutOrNull findViews()
            .where { it.checkButtonText() }
            .findAll().toList().findSendButton().also {
                if (it != null) {
                    SEND_BUTTON_CACHE = it
                }
            }
    }
}

private suspend fun findInput(): ViewNode? {
    if (INPUT_EDITABLE_CACHE?.parent.isNotNull()) {
        return INPUT_EDITABLE_CACHE
    }
    return withTimeoutOrNull(SEARCH_BUTTON_TIMEOUT) {
        return@withTimeoutOrNull findViews().where { it.isEditable }
            .findAll().toList().findSendInput().also {
                if (it != null) {
                    INPUT_EDITABLE_CACHE = it
                }
            }
    }
}

fun AcsNode.checkButtonText(): Boolean {
    return checkSendButtonTextValue(this.text?.toString() ?: "") || checkSendButtonTextValue(
        this.contentDescription?.toString() ?: ""
    )
}

fun AcsNode.checkButtonDesc(): Boolean {
    return checkSendButtonTextValue(this.contentDescription?.toString() ?: "")
}

fun List<ViewNode>.otherAppNodes(): List<ViewNode> {
    return this.filter { it.packageName != app.packageName }
}

fun ConditionGroup.otherAppNodes(): ConditionGroup {
    return and { it.packageName != app.packageName }
}

fun List<ViewNode>.findSendButton(): ViewNode? {
    val buttonList = this.otherAppNodes().filter { it.node.checkButtonText() }
        .sortedBy {
            var sortValue = 0;
            val conditions = listOf(
                it.isClickable(),
                it.className == BUTTON_CLASS_NAME,
                it.node.checkButtonDesc(),
                it.node.isEnabled
            )
            for (condition in conditions) {
                if (condition) {
                    sortValue--
                }
            }
            return@sortedBy sortValue
        }

//    Log.d("test", "buttons $buttonList")
    return buttonList.firstOrNull() ?: SEND_BUTTON_CACHE
}

fun List<ViewNode>.findSendInput(): ViewNode? {
    val inputList =
        this.otherAppNodes().filter { it.node.isEditable }
            .sortedBy {
                var sortValue = 0;
                if (it.className == EDIT_TEXT_CLASS_NAME) {
                    sortValue--
                }
                return@sortedBy sortValue
            }
    return inputList.firstOrNull() ?: INPUT_EDITABLE_CACHE
}

fun findViews(): ConditionGroup {
    return SmartFinder().where { it.packageName != app.packageName }
}