package com.donut.mixmessage.ui.component.routes.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.donut.mixmessage.app
import com.donut.mixmessage.service.IS_ACS_ENABLED
import com.donut.mixmessage.service.startFloat
import com.donut.mixmessage.service.stopFloat
import com.donut.mixmessage.ui.component.common.CommonSwitch
import com.donut.mixmessage.ui.component.common.LabelSwitch
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.ui.component.common.SingleSelectItemList
import com.donut.mixmessage.ui.component.encoder.copyWhenRefresh
import com.donut.mixmessage.ui.component.encoder.setEnableCopyWhenRefresh
import com.donut.mixmessage.ui.component.nav.MixNavPage
import com.donut.mixmessage.ui.component.nav.NavTitle
import com.donut.mixmessage.ui.component.nav.getNavController
import com.donut.mixmessage.ui.component.routes.settings.routes.AboutPage
import com.donut.mixmessage.ui.component.routes.settings.routes.AutoDecode
import com.donut.mixmessage.ui.component.routes.settings.routes.FastSend
import com.donut.mixmessage.ui.component.routes.settings.routes.ImagePage
import com.donut.mixmessage.ui.component.routes.settings.routes.OtherPage
import com.donut.mixmessage.ui.component.routes.settings.routes.PrefixPage
import com.donut.mixmessage.ui.component.routes.settings.routes.RSAPage
import com.donut.mixmessage.ui.theme.colorScheme
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.performHapticFeedBack
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.encode.DEFAULT_ENCODER
import com.donut.mixmessage.util.encode.ENCODERS
import com.donut.mixmessage.util.encode.USE_RANDOM_ENCODER
import com.donut.mixmessage.util.encode.USE_RANDOM_PASSWORD
import com.donut.mixmessage.util.encode.encoders.bean.USE_SIMPLE_MODE
import com.donut.mixmessage.util.encode.encoders.bean.setUseStrictEncode
import com.donut.mixmessage.util.encode.setDefaultEncoder
import com.donut.mixmessage.util.encode.setUseRandomEncoder
import com.donut.mixmessage.util.encode.setUseRandomPassword


var enableFloat by cachedMutableOf(false, "enable_float")

var useDefaultPrefix by cachedMutableOf(true, "use_default_prefix")

var CALCULATOR_LOCK by cachedMutableOf(false, "calculator_lock")

var START_BLANK_SCREEN by cachedMutableOf(false, "start_blank_screen")


@OptIn(ExperimentalLayoutApi::class)
fun selectDefaultEncoder() {
    MixDialogBuilder("默认加密方法").apply {
        setContent {
            SingleSelectItemList(
                items = ENCODERS.map { it.name },
                currentOption = DEFAULT_ENCODER
            ) {
                setDefaultEncoder(it)
                closeDialog()
            }
        }
        setBottomContent {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LabelSwitch(checked = USE_SIMPLE_MODE, label = "精简模式: ") {
                    if (it) {
                        MixDialogBuilder("确定开启?").apply {
                            setContent {
                                Text(
                                    text = """
                                            开启后将会使用精简模式,输出结果字数更短
                                            占用更少的字数,安全性较低,容易被暴力破解
                                        """.trimIndent().replace("\n", " ")
                                )
                            }
                            setDefaultNegative()
                            setPositiveButton("确定") {
                                performHapticFeedBack()
                                USE_SIMPLE_MODE = true
                                closeDialog()
                            }
                            show()
                        }
                        return@LabelSwitch
                    }
                    performHapticFeedBack()
                    USE_SIMPLE_MODE = false
                }
            }
        }
        show()
    }
}


@Composable
fun SettingItem(
    title: String,
    imageVector: ImageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
    onClick: () -> Unit,
) {
    Column {
        HorizontalDivider()
        Row(
            modifier = Modifier
                .clickable(
                    onClick = onClick
                )
                .fillMaxWidth()
                .padding(0.dp, 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Icon(imageVector = imageVector, contentDescription = title)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
val Settings = MixNavPage {
    NavTitle(title = "设置")
    CommonSwitch(checked = enableFloat, text = "悬浮窗开关:", onCheckedChangeListener = {
        if (!android.provider.Settings.canDrawOverlays(app) && it) {
            showToast("请先开启悬浮窗权限")
            return@CommonSwitch
        }
        enableFloat = it
        if (!it) {
            return@CommonSwitch stopFloat()
        }
        startFloat()
    })

    CommonSwitch(
        checked = USE_RANDOM_PASSWORD,
        text = "启用随机密码:",
        "启用后加密时将随机选择已有的密钥",
    ) {
        setUseRandomPassword(it)
    }
    CommonSwitch(
        checked = USE_RANDOM_ENCODER,
        text = "启用随机编码:",
        "启用后加密时将使用随机编码方法",
    ) {
        setUseRandomEncoder(it)
    }
    CommonSwitch(
        checked = USE_SIMPLE_MODE,
        text = "启用精简模式:",
        "启用后输出结果字数将会更短,安全性降低,容易被暴力破解",
    ) {
        setUseStrictEncode(it)
    }
    CommonSwitch(
        checked = copyWhenRefresh,
        text = "刷新时自动复制:",
        "启用后刷新加密结果后自动复制新内容",
    ) {
        setEnableCopyWhenRefresh(it)
    }
    SettingBox(
        modifier = Modifier.padding(0.dp, 10.dp)
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "当前无障碍状态: ",
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Text(
                text = if (IS_ACS_ENABLED) "已开启" else "未开启",
                color = colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
        if (!IS_ACS_ENABLED) {
            Text(
                text = """
                请开启无障碍权限才能让本软件功能正常运作
                设置->无障碍->MixMessage智能解码
            """.trimIndent(), color = Color.Red
            )
        }
    }

    val controller = getNavController()

    @Composable
    fun SettingPage(title: String, path: String) {
        SettingItem(title = title) {
            controller.navigate(path)
        }
    }

    SettingPage("自动解码设置", AutoDecode.name)
    SettingPage("一键发送设置", FastSend.name)
    SettingPage("图片上传设置", ImagePage.name)
    SettingPage("非对称加密设置", RSAPage.name)
    SettingPage("前缀设置", PrefixPage.name)
    SettingPage("其他设置", OtherPage.name)
    SettingPage("关于", AboutPage.name)

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingButton(text: String, buttonText: String = "设置", onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider()
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 5.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = text,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            OutlinedButton(onClick = onClick) {
                Text(text = buttonText)
            }
        }
    }
}


@Composable
fun SettingBox(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth(),
        border = BorderStroke(2.dp, colorScheme.primary),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent,
        ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(10.dp)
        ) {
            content()
        }
    }
}