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
import com.donut.mixmessage.ui.component.routes.settings.routes.OtherPage
import com.donut.mixmessage.ui.theme.colorScheme
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.encode.DEFAULT_ENCODER
import com.donut.mixmessage.util.encode.ENCODERS
import com.donut.mixmessage.util.encode.USE_RANDOM_ENCODER
import com.donut.mixmessage.util.encode.USE_RANDOM_PASSWORD
import com.donut.mixmessage.util.encode.encoders.bean.USE_STRICT_ENCODE
import com.donut.mixmessage.util.encode.encoders.bean.setUseStrictEncode
import com.donut.mixmessage.util.encode.setDefaultEncoder
import com.donut.mixmessage.util.encode.setUseRandomEncoder
import com.donut.mixmessage.util.encode.setUseRandomPassword


var enableFloat by cachedMutableOf(false, "enable_float")

var useDefaultPrefix by cachedMutableOf(true, "use_default_prefix")

var START_BLANK_SCREEN by cachedMutableOf(false, "start_blank_screen")


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
val Settings = MixNavPage("settings_main") {
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
        checked = USE_STRICT_ENCODE,
        text = "启用严格编码:",
        "启用后将会加密所有字符内容(包括特殊字符),只支持除移位加密以外的算法",
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
    SettingItem(title = "自动解码设置") {
        controller.navigate(AutoDecode.name)
    }
    SettingItem(title = "一键发送设置") {
        controller.navigate(FastSend.name)
    }
    SettingItem(title = "其他设置") {
        controller.navigate(OtherPage.name)
    }
    SettingItem(title = "关于") {
        controller.navigate(AboutPage.name)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingButton(text: String, buttonText: String = "设置", onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider()
        FlowRow(
            modifier = Modifier
                .fillMaxWidth().padding(0.dp,5.dp),
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
        border = BorderStroke(2.dp, Color(0xFF036BBD)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x00ADE4FF),
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