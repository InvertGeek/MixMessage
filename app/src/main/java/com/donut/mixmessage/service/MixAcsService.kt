package com.donut.mixmessage.service

import android.view.accessibility.AccessibilityEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cn.vove7.andro_accessibility_api.AccessibilityApi
import cn.vove7.auto.core.AppScope
import cn.vove7.auto.core.viewfinder.AcsNode
import cn.vove7.auto.core.viewfinder.SmartFinder
import cn.vove7.auto.core.viewnode.ViewNode
import com.donut.mixmessage.R
import com.donut.mixmessage.activity.PopUpActivity
import com.donut.mixmessage.activity.openDecodeDialog
import com.donut.mixmessage.app
import com.donut.mixmessage.appScope
import com.donut.mixmessage.kv
import com.donut.mixmessage.ui.component.routes.password.startLock
import com.donut.mixmessage.ui.component.routes.settings.enableFloat
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.debug
import com.donut.mixmessage.util.common.removeBrace
import com.donut.mixmessage.util.encode.encoders.bean.CoderResult
import com.hjq.window.EasyWindow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


var AUTO_SCAN_BUTTONS by cachedMutableOf(true, "auto_scan_buttons")

var SCAN_BUTTON_WHEN_CLICK by cachedMutableOf(true, "auto_scan_buttons_click")

fun setAutoScanButtons(value: Boolean) {
    AUTO_SCAN_BUTTONS = value
}

fun setScanButtonWhenClick(value: Boolean) {
    SCAN_BUTTON_WHEN_CLICK = value
}


class MixAccessibilityService : AccessibilityApi() {

    override val enableListenPageUpdate: Boolean
        get() = true

    //页面更新回调
    override fun onPageUpdate(currentScope: AppScope) {

    }


    override fun onServiceConnected() {
        context = this
        super.onServiceConnected()
        requireBaseAccessibility()
        EasyWindow.with(app)
            .setTag("invisible_float")
            .setWidth(0)
            .setHeight(0)
            .setContentView(R.layout.invisible_float)
            .show()
        if (enableFloat) {
            startFloat()
        }
        updateButtons()
    }

    companion object {

        var context: MixAccessibilityService? by mutableStateOf(null)

        var ENABLE_SINGLE_CLICK by mutableStateOf(
            kv.decodeBool(
                "AUTO_DECODE_ENABLE_SINGLE_CLICK",
                true
            )
        )
        var ENABLE_LONG_CLICK by mutableStateOf(
            kv.decodeBool(
                "AUTO_DECODE_ENABLE_LONG_CLICK",
                true
            )
        )
        var ENABLE_SELECT_TEXT by mutableStateOf(
            kv.decodeBool(
                "AUTO_DECODE_ENABLE_SELECT_TEXT",
                false
            )
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        super.onAccessibilityEvent(event)
        if (event == null) {
            return
        }
        if (event.packageName == app.packageName) {
            return
        }
        if (PopUpActivity.context?.isDestroyed == false) {
            return
        }
        val type = event.eventType
        val source = event.source
        val text = source?.text ?: event.text.toString().removeBrace()
        var shouldOpen = false
        if (type == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            if (checkDialogOpenTextValue(text.toString())) {
                return openDecodeDialog(result = CoderResult.Failed)
            }
            if (source != null && SCAN_BUTTON_WHEN_CLICK) {
                if (source.isEditable) {
//                    debug("update input")
                    INPUT_EDITABLE_CACHE = ViewNode(source)
                }
                if (AcsNode.wrap(source).checkButtonText()) {
//                    debug("update button")
                    SEND_BUTTON_CACHE = ViewNode(source)
                }
            }
            if (ENABLE_SINGLE_CLICK) {
//                debug("text: $text")
                shouldOpen = true
            }
        }
//        Log.e("test", "onAccessibilityEvent: ${event?.eventType} ${event?.action} ${event?.text}")
        if (type == AccessibilityEvent.TYPE_VIEW_LONG_CLICKED && ENABLE_LONG_CLICK) {
            debug("long click text: $text")
            shouldOpen = true
        }
        if (type == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
            debug("select text: $text")
            shouldOpen = true
        }

        if (text.trim().isEmpty() || source?.isEditable == true) {
            return
        }

        if (shouldOpen) openDecodeDialog(text.toString())
    }


    private fun updateButtons() {
        appScope.run {
            startLock()
        }
        appScope.launch(Dispatchers.IO) {
            if (AUTO_SCAN_BUTTONS) {
                val tmpButtonCache = mutableListOf<ViewNode>()
                val tmpEdiTextCache = mutableListOf<ViewNode>()

                SmartFinder().otherAppNodes().where {
                    val viewNode = ViewNode(it)
                    if (it.checkButtonText()) {
                        tmpButtonCache.add(viewNode)
                        return@where true
                    }
                    if (it.isEditable) {
                        tmpEdiTextCache.add(viewNode)
                        return@where true
                    }
                    false
                }.findAll()

                INPUT_EDITABLE_CACHE = tmpEdiTextCache.findSendInput() ?: INPUT_EDITABLE_CACHE
                SEND_BUTTON_CACHE = tmpButtonCache.findSendButton() ?: SEND_BUTTON_CACHE
            }
            delay(400)
            updateButtons()
        }
    }

}

fun stopFloat() {
    EasyWindow.cancelByTag("mix_message_float")
}

fun startFloat() {
    stopFloat()
    EasyWindow.with(app)
        .setTag("mix_message_float")
        .setContentView(R.layout.float_icon)
        .setWidth(150)
        .setHeight(150)
        .setVerticalWeight(100F)
        .setOnClickListener(
            R.id.float_decoder
        ) { window, view ->
            openDecodeDialog(result = CoderResult.Failed)
        }
        .setDraggable().show()
}