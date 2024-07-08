package com.donut.mixmessage.ui.component.routes.settings.routes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.donut.mixmessage.ui.component.common.CommonSwitch
import com.donut.mixmessage.ui.component.nav.MixNavPage
import com.donut.mixmessage.ui.component.nav.NavTitle
import com.donut.mixmessage.ui.component.routes.settings.useDefaultPrefix
import com.donut.mixmessage.util.encode.encoders.ZeroWidthEncoder

val PrefixPage = MixNavPage(displayNavBar = false, gap = 10.dp, useTransition = true) {
    NavTitle(title = "空位加密前缀(伪装文本)设置", showBackIcon = true)
    OutlinedTextField(
        value = ZeroWidthEncoder.encodePrefix,
        onValueChange = { newValue ->
            ZeroWidthEncoder.encodePrefix = newValue
        },
        maxLines = 1,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("空位加密前缀 %r=随机数字 %e=随机表情") }
    )
    CommonSwitch(
        checked = useDefaultPrefix,
        text = "一键发送时使用自动前缀:",
        "关闭则每次手动输入前缀",
    ) {
        useDefaultPrefix = it
    }
    CommonSwitch(
        checked = ZeroWidthEncoder.usePoemPrefix,
        text = "使用古诗前缀:",
        "智能回复下一句对应诗句,无则随机使用唐诗三百首诗句",
    ) {
        ZeroWidthEncoder.usePoemPrefix = it
    }
    CommonSwitch(
        checked = ZeroWidthEncoder.useIdiomPrefix,
        text = "使用成语前缀:",
        "智能的成语接龙前缀(优先级高于古诗前缀)",
    ) {
        ZeroWidthEncoder.useIdiomPrefix = it
    }
}