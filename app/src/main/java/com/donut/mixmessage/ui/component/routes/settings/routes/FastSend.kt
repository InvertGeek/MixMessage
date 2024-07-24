package com.donut.mixmessage.ui.component.routes.settings.routes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.donut.mixmessage.service.DIALOG_OPEN_IDENTIFIER
import com.donut.mixmessage.service.SCAN_BUTTON_WHEN_CLICK
import com.donut.mixmessage.service.SEARCH_BUTTON_TIMEOUT
import com.donut.mixmessage.service.SEND_BUTTON_IDENTIFIER
import com.donut.mixmessage.service.setScanButtonWhenClick
import com.donut.mixmessage.service.setSendButtonIdentifier
import com.donut.mixmessage.ui.component.common.CommonSwitch
import com.donut.mixmessage.ui.component.nav.MixNavPage
import com.donut.mixmessage.ui.component.nav.NavTitle
import com.donut.mixmessage.util.common.cachedMutableOf
import okhttp3.internal.toLongOrDefault

var DETECT_TEXT_SEND by cachedMutableOf(true, "DETECT_TEXT_LENGTH")

val FastSend = MixNavPage(displayNavBar = false, useTransition = true) {
    NavTitle(title = "一键发送设置", showBackIcon = true)
    OutlinedTextField(
        value = SEND_BUTTON_IDENTIFIER,
        onValueChange = { newValue ->
            setSendButtonIdentifier(newValue)
        },
        maxLines = 1,
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 10.dp),
        label = { Text("发送按钮关键词,使用空格分割,将会以关键词判断是否为发送按钮") }
    )
    OutlinedTextField(
        value = DIALOG_OPEN_IDENTIFIER,
        onValueChange = { newValue ->
            DIALOG_OPEN_IDENTIFIER = newValue
        },
        maxLines = 1,
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 10.dp),
        label = { Text("快捷窗口关键词,使用空格分割,点击此关键词直接弹出快捷窗口") }
    )
    OutlinedTextField(
        value = SEARCH_BUTTON_TIMEOUT.toString(),
        onValueChange = { newValue ->
            SEARCH_BUTTON_TIMEOUT = newValue.toLongOrDefault(100).coerceAtLeast(100)
        },
        maxLines = 1,
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 10.dp),
        label = { Text("搜索按钮超时时间(毫秒)") }
    )
    CommonSwitch(
        checked = SCAN_BUTTON_WHEN_CLICK,
        text = "点击发送和输入自动更新:",
        "点击任何匹配发送字样的按钮和文字以及输入框，智能识别为一键发送使用的按钮和输入框",
    ) {
        setScanButtonWhenClick(it)
    }
    CommonSwitch(
        checked = DETECT_TEXT_SEND,
        text = "智能识别发送结果:",
        "开启后将智能检测输入框字数限制,是否发送成功等情况,失败则取消发送并重新显示窗口",
    ) {
        DETECT_TEXT_SEND = it
    }
}