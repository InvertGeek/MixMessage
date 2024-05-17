package com.donut.mixmessage.ui.component.routes.settings.routes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.donut.mixmessage.ui.component.common.CommonSwitch
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.ui.component.common.SingleSelectItemList
import com.donut.mixmessage.ui.component.nav.MixNavPage
import com.donut.mixmessage.ui.component.nav.NavTitle
import com.donut.mixmessage.ui.component.routes.settings.START_BLANK_SCREEN
import com.donut.mixmessage.ui.component.routes.settings.SettingButton
import com.donut.mixmessage.ui.component.routes.settings.useDefaultPrefix
import com.donut.mixmessage.ui.theme.Theme
import com.donut.mixmessage.ui.theme.currentTheme
import com.donut.mixmessage.ui.theme.enableAutoDarkMode
import com.donut.mixmessage.util.common.ENABLE_HAPTIC_FEEDBACK
import com.donut.mixmessage.util.common.LogoUtil
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.encode.encoders.ZeroWidthEncoder
import com.donut.mixmessage.util.objects.MixActivity

val OtherPage = MixNavPage(displayNavBar = false, gap = 10.dp, useTransition = true) {
    NavTitle(title = "其他设置", showBackIcon = true)
    OutlinedTextField(
        value = ZeroWidthEncoder.encodeResultPrefix,
        onValueChange = { newValue ->
            ZeroWidthEncoder.setShiftEncodeResultPrefix(newValue)
        },
        maxLines = 1,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("空位加密前缀 %r=随机数字 %e=随机表情") }
    )
    CommonSwitch(
        checked = useDefaultPrefix,
        text = "一键发送时使用自动前缀(空位加密):",
        "关闭则每次手动输入前缀",
    ) {
        useDefaultPrefix = it
    }
    CommonSwitch(
        checked = START_BLANK_SCREEN,
        text = "启动白屏:",
        "开启后APP启动显示白屏(双指放大解锁)",
    ) {
        if (it) {
            showToast("使用双指放大解锁")
        }
        START_BLANK_SCREEN = it
    }
    CommonSwitch(
        checked = ENABLE_HAPTIC_FEEDBACK,
        text = "触觉反馈:",
        "点击等操作时提供触觉反馈",
    ) {
        ENABLE_HAPTIC_FEEDBACK = it
    }
    SettingButton(text = "APP伪装: ") {
        MixDialogBuilder("APP伪装").apply {
            setContent {
                SingleSelectItemList(
                    items = LogoUtil.Logo.entries.map { it.label },
                    currentOption = LogoUtil.Logo.entries.firstOrNull {
                        it.packageName == MixActivity.getMainContext()!!.componentName?.className
                    }?.label ?: ""
                ) { option ->
                    LogoUtil.changeLogo(LogoUtil.Logo.entries.firstOrNull {
                        it.label == option
                    } ?: LogoUtil.Logo.DEFAULT)
                    closeDialog()
                }
            }
            show()
        }
    }
    SettingButton(text = "颜色主题: ") {
        MixDialogBuilder("颜色主题").apply {
            setContent {
                SingleSelectItemList(
                    items = Theme.entries.map { it.label },
                    currentOption = Theme.entries.firstOrNull {
                        it.name == currentTheme
                    }?.label ?: ""
                ) { option ->
                    currentTheme =
                        Theme.entries.firstOrNull { it.label == option }?.name ?: Theme.DEFAULT.name
                    closeDialog()
                }
            }
            show()
        }
    }
    CommonSwitch(
        checked = enableAutoDarkMode,
        text = "自动深色模式:",
        "跟随系统自动切换深色模式",
    ) {
        enableAutoDarkMode = it
    }
}
