package com.donut.mixmessage.service

import android.view.accessibility.AccessibilityEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cn.vove7.andro_accessibility_api.AccessibilityApi
import cn.vove7.auto.core.AppScope
import cn.vove7.auto.core.viewfinder.AcsNode
import cn.vove7.auto.core.viewnode.ViewNode
import com.donut.mixmessage.R
import com.donut.mixmessage.app
import com.donut.mixmessage.appScope
import com.donut.mixmessage.decode.DecodeActivity
import com.donut.mixmessage.decode.openDecodeDialog
import com.donut.mixmessage.ui.component.routes.password.startLock
import com.donut.mixmessage.ui.component.routes.settings.enableFloat
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.isAccessibilityServiceEnabled
import com.donut.mixmessage.util.common.isEqual
import com.donut.mixmessage.util.common.isFalse
import com.donut.mixmessage.util.common.isNotNullAnd
import com.donut.mixmessage.util.common.isTrue
import com.donut.mixmessage.util.common.showError
import com.donut.mixmessage.util.encode.encoders.bean.CoderResult
import com.donut.mixmessage.util.encode.updateRoundKeys
import com.hjq.window.EasyWindow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.system.exitProcess


var SCAN_BUTTON_WHEN_CLICK by cachedMutableOf(true, "auto_scan_buttons_click")
var IS_ACS_ENABLED by mutableStateOf(isAccessibilityServiceEnabled())

fun setScanButtonWhenClick(value: Boolean) {
    SCAN_BUTTON_WHEN_CLICK = value
}


class MixAccessibilityService : AccessibilityApi() {

    override val enableListenPageUpdate: Boolean
        get() = true

    //页面更新回调
    override fun onPageUpdate(currentScope: AppScope) {

    }

    override fun onDestroy() {
        IS_ACS_ENABLED = false
        super.onDestroy()
        exitProcess(0)
    }


    override fun onServiceConnected() {
        IS_ACS_ENABLED = true
        EasyWindow.cancelByTag("invisible_float")
        EasyWindow.with(app)
            .setTag("invisible_float")
            .setWidth(0)
            .setHeight(0)
            .setContentView(R.layout.invisible_float)
            .show()
        if (enableFloat) {
            startFloat()
        }
        loopTask()
        super.onServiceConnected()
    }

    companion object {
        var ENABLE_SINGLE_CLICK by cachedMutableOf(true, "AUTO_DECODE_ENABLE_SINGLE_CLICK")
        var ENABLE_LONG_CLICK by cachedMutableOf(true, "AUTO_DECODE_ENABLE_LONG_CLICK")
        var ENABLE_SELECT_TEXT by cachedMutableOf(true, "AUTO_DECODE_ENABLE_SELECT_TEXT")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        super.onAccessibilityEvent(event)
        if (event == null) {
            return
        }
        if (event.packageName == app.packageName) {
            return
        }
        if (DecodeActivity.IS_ACTIVE) {
            return
        }
        val type = event.eventType
        val source = event.source
        val text = source?.text ?: event.text.toString().removeSurrounding("[", "]")

        var shouldOpen = false
        type.isEqual(AccessibilityEvent.TYPE_VIEW_CLICKED) {
            checkDialogOpenTextValue(text.toString()).isTrue {
                return openDecodeDialog(result = CoderResult.Failed)
            }
            source.isNotNullAnd(SCAN_BUTTON_WHEN_CLICK) {
                INPUT_EDITABLE_CACHE.isValid().isFalse {
                    it.isEditable.isTrue { INPUT_EDITABLE_CACHE = ViewNode(it) }
                }
                SEND_BUTTON_CACHE.isValid().isFalse {
                    it.isClickable.isTrue {
                        AcsNode.wrap(it).checkButton()
                            .isTrue { SEND_BUTTON_CACHE = ViewNode(it) }
                    }
                }
            }
            shouldOpen = ENABLE_SINGLE_CLICK
        }
        type.isEqual(AccessibilityEvent.TYPE_VIEW_LONG_CLICKED) {
            shouldOpen = ENABLE_LONG_CLICK
        }
        type.isEqual(AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
            shouldOpen = ENABLE_SELECT_TEXT
        }

        if (text.trim().isEmpty() || source?.isEditable == true) {
            return
        }

        shouldOpen.isTrue {
            openDecodeDialog(text.toString())
        }
    }


    private fun loopTask() {
        appScope.launch {
            try {
                startLock()
                updateRoundKeys()
                delay(1000 * 10)
            } catch (e: Exception) {
                showError(e)
            }
            loopTask()
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
        .setOnClickListener { _, _ ->
            openDecodeDialog(result = CoderResult.Failed)
        }
        .setDraggable().show()
}