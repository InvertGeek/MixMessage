package com.donut.mixmessage.ui.component.routes.settings.routes

import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.donut.mixmessage.service.MixAccessibilityService
import com.donut.mixmessage.ui.component.common.CommonSwitch
import com.donut.mixmessage.ui.component.nav.MixNavPage
import com.donut.mixmessage.ui.component.nav.NavTitle

val AutoDecode = MixNavPage(
    displayNavBar = false,
    useTransition = true,) {
    NavTitle(title = "自动解码设置", showBackIcon = true)
    Text(
        text = "推荐使用长按,部分界面单击无法识别到文字",
        fontSize = 14.sp,
        color = Color(0xFFAAAAAA)
    )
    CommonSwitch(
        checked = MixAccessibilityService.ENABLE_SINGLE_CLICK,
        text = "单击文字触发:"
    ) {
        MixAccessibilityService.ENABLE_SINGLE_CLICK = it
    }
    CommonSwitch(
        checked = MixAccessibilityService.ENABLE_LONG_CLICK,
        text = "长按文字触发:"
    ) {
        MixAccessibilityService.ENABLE_LONG_CLICK = it
    }
    CommonSwitch(
        checked = MixAccessibilityService.ENABLE_SELECT_TEXT,
        text = "选择文字触发:",
        "其他情况: 例如微信公众号文章中的文字(点击或长按),并不是选择复制的文字"
    ) {
        MixAccessibilityService.ENABLE_SELECT_TEXT = it
    }
}
