package com.donut.mixmessage.ui.component.routes.settings.routes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.donut.mixmessage.ACS_NOTIFY
import com.donut.mixmessage.currentActivity
import com.donut.mixmessage.ui.component.common.CommonSwitch
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.ui.component.common.SingleSelectItemList
import com.donut.mixmessage.ui.component.nav.MixNavPage
import com.donut.mixmessage.ui.component.nav.NavTitle
import com.donut.mixmessage.ui.component.routes.settings.START_BLANK_SCREEN
import com.donut.mixmessage.ui.component.routes.settings.SettingButton
import com.donut.mixmessage.ui.theme.Theme
import com.donut.mixmessage.ui.theme.currentTheme
import com.donut.mixmessage.ui.theme.enableAutoDarkMode
import com.donut.mixmessage.util.common.ENABLE_HAPTIC_FEEDBACK
import com.donut.mixmessage.util.common.LogoUtil
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.encode.TIME_LOCK_REVERSE
import com.donut.mixmessage.util.objects.MixActivity
import okhttp3.internal.toLongOrDefault

var ALLOW_SCREENSHOT by cachedMutableOf(false, "allow_screenshot")

val OtherPage = MixNavPage(displayNavBar = false, gap = 10.dp, useTransition = true) {
    NavTitle(title = "其他设置", showBackIcon = true)
    OutlinedTextField(
        value = TIME_LOCK_REVERSE.toString(),
        onValueChange = { newValue ->
            TIME_LOCK_REVERSE = newValue.toLongOrDefault(0).coerceAtLeast(0).coerceAtMost(10)
        },
        maxLines = 1,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("时间锁回溯(天),尝试使用过去的日期解密内容") }
    )
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
    CommonSwitch(
        checked = ALLOW_SCREENSHOT,
        text = "允许截图:",
        "是否允许在APP进行截图",
    ) {
        ALLOW_SCREENSHOT = it
        currentActivity.refreshAllowScreenShot()
    }
    SettingButton(text = "APP伪装: ") {
        MixDialogBuilder("APP伪装").apply {
            setContent {
                SingleSelectItemList(
                    items = LogoUtil.Logo.entries.toList(),
                    getLabel = { it.label },
                    currentOption = LogoUtil.Logo.entries.firstOrNull {
                        it.packageName == MixActivity.getMainContext()!!.componentName?.className
                    } ?: LogoUtil.Logo.DEFAULT
                ) { option ->
                    LogoUtil.changeLogo(option)
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
                    items = Theme.entries,
                    getLabel = { it.label },
                    currentOption = Theme.entries.firstOrNull {
                        it.name == currentTheme
                    } ?: Theme.DEFAULT
                ) { option ->
                    currentTheme = option.name
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
    CommonSwitch(
        checked = ACS_NOTIFY,
        text = "无障碍提醒:",
        "未开启无障碍进入时显示弹窗提醒",
    ) {
        ACS_NOTIFY = it
    }
}
