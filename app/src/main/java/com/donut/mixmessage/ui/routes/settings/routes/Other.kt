package com.donut.mixmessage.ui.routes.settings.routes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.donut.mixmessage.ACS_NOTIFY
import com.donut.mixmessage.currentActivity
import com.donut.mixmessage.ui.component.common.CommonSwitch
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.ui.component.common.SingleSelectItemList
import com.donut.mixmessage.ui.nav.MixNavPage
import com.donut.mixmessage.ui.nav.NavTitle
import com.donut.mixmessage.ui.routes.settings.CALCULATOR_LOCK
import com.donut.mixmessage.ui.routes.settings.START_BLANK_SCREEN
import com.donut.mixmessage.ui.routes.settings.SettingButton
import com.donut.mixmessage.ui.theme.Theme
import com.donut.mixmessage.ui.theme.currentTheme
import com.donut.mixmessage.ui.theme.enableAutoDarkMode
import com.donut.mixmessage.util.common.LogoUtil
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.encode.TIME_LOCK_REVERSE
import com.donut.mixmessage.util.objects.MixActivity
import com.donut.mixmessage.visible

var ALLOW_SCREENSHOT by cachedMutableOf(false, "allow_screenshot")

val OtherPage = MixNavPage(displayNavBar = false, gap = 10.dp, useTransition = true) {
    NavTitle(title = "其他设置", showBackIcon = true)
    Column {
        Text(
            modifier = Modifier.padding(10.dp, 0.dp),
            text = "时间锁回溯(天),尝试使用过去的日期解密内容: $TIME_LOCK_REVERSE",
            color = colorScheme.primary
        )
        Slider(
            value = TIME_LOCK_REVERSE.toFloat() / 10f,
            steps = 100,
            modifier = Modifier.fillMaxWidth(),
            onValueChange = {
                TIME_LOCK_REVERSE = (it * 10).toLong()
            }
        )
    }

    CommonSwitch(
        checked = START_BLANK_SCREEN,
        text = "启动白屏:",
        "开启后APP启动显示白屏(双指放大解锁)",
    ) {
        if (it) {
            CALCULATOR_LOCK = false
            visible = false
            showToast("使用双指放大解锁")
        }
        START_BLANK_SCREEN = it
    }

    CommonSwitch(
        checked = CALCULATOR_LOCK,
        text = "计算器锁定:",
        "开启后APP启动显示计算器(输入66/66后点击等号解锁)",
    ) {
        if (it) {
            START_BLANK_SCREEN = false
            visible = false
            showToast("输入66/66后点击等号解锁")
        }
        CALCULATOR_LOCK = it
    }
    CommonSwitch(
        checked = ALLOW_SCREENSHOT,
        text = "允许截图:",
        "是否允许在APP进行截图(建议禁用,可防止截屏录屏和其他应用读取屏幕内容)",
    ) {
        ALLOW_SCREENSHOT = it
        currentActivity?.refreshAllowScreenShot()
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
