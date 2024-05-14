package com.donut.mixmessage.ui.component.routes.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donut.mixmessage.MainActivity
import com.donut.mixmessage.app
import com.donut.mixmessage.kv
import com.donut.mixmessage.service.DIALOG_OPEN_IDENTIFIER
import com.donut.mixmessage.service.IS_ACS_ENABLED
import com.donut.mixmessage.service.MixAccessibilityService
import com.donut.mixmessage.service.SCAN_BUTTON_WHEN_CLICK
import com.donut.mixmessage.service.SEARCH_BUTTON_TIMEOUT
import com.donut.mixmessage.service.SEND_BUTTON_IDENTIFIER
import com.donut.mixmessage.service.setScanButtonWhenClick
import com.donut.mixmessage.service.setSendButtonIdentifier
import com.donut.mixmessage.service.startFloat
import com.donut.mixmessage.service.stopFloat
import com.donut.mixmessage.ui.component.common.CommonSwitch
import com.donut.mixmessage.ui.component.common.MaterialDialogBuilder
import com.donut.mixmessage.ui.component.common.SingleSelectItemList
import com.donut.mixmessage.ui.component.encoder.copyWhenRefresh
import com.donut.mixmessage.ui.component.encoder.setEnableCopyWhenRefresh
import com.donut.mixmessage.ui.theme.LightColorScheme
import com.donut.mixmessage.util.common.LogoUtil
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.isAccessibilityServiceEnabled
import com.donut.mixmessage.util.common.isFalse
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.encode.DEFAULT_ENCODER
import com.donut.mixmessage.util.encode.ENCODERS
import com.donut.mixmessage.util.encode.ENCODE_COUNT
import com.donut.mixmessage.util.encode.SUCCESS_DECODE_COUNT
import com.donut.mixmessage.util.encode.USE_RANDOM_ENCODER
import com.donut.mixmessage.util.encode.USE_RANDOM_PASSWORD
import com.donut.mixmessage.util.encode.encoders.ZeroWidthEncoder
import com.donut.mixmessage.util.encode.encoders.bean.USE_STRICT_ENCODE
import com.donut.mixmessage.util.encode.encoders.bean.setUseStrictEncode
import com.donut.mixmessage.util.encode.resetStaticCount
import com.donut.mixmessage.util.encode.setDefaultEncoder
import com.donut.mixmessage.util.encode.setUseRandomEncoder
import com.donut.mixmessage.util.encode.setUseRandomPassword
import com.donut.mixmessage.util.objects.MixActivity


var enableFloat by cachedMutableOf(false, "enable_float")

var useDefaultPrefix by cachedMutableOf(true, "use_default_prefix")

var START_BLANK_SCREEN by cachedMutableOf(false, "start_blank_screen")

@Composable
fun AutoDecodeSetting() {
    Text(text = "自动解码触发设置", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    Text(
        text = "推荐使用长按,部分界面单击无法识别到文字",
        fontSize = 14.sp,
        color = Color(0xFFAAAAAA)
    )
    CommonSwitch(
        checked = MixAccessibilityService.ENABLE_SINGLE_CLICK,
        text = "单击文字触发:",
        onCheckedChangeListener = {
            MixAccessibilityService.ENABLE_SINGLE_CLICK = it
            kv.encode("AUTO_DECODE_ENABLE_SINGLE_CLICK", it)
        })
    CommonSwitch(
        checked = MixAccessibilityService.ENABLE_LONG_CLICK,
        text = "长按文字触发:",
        onCheckedChangeListener = {
            MixAccessibilityService.ENABLE_LONG_CLICK = it
            kv.encode("AUTO_DECODE_ENABLE_LONG_CLICK", it)
        })
    CommonSwitch(
        checked = MixAccessibilityService.ENABLE_SELECT_TEXT,
        text = "选择文字触发:",
        onCheckedChangeListener = {
            MixAccessibilityService.ENABLE_SELECT_TEXT = it
            kv.encode("AUTO_DECODE_ENABLE_SELECT_TEXT", it)
        }, "其他情况: 例如微信公众号文章中的文字(点击或长按),并不是选择复制的文字"
    )
}

fun selectDefaultEncoder() {
    MaterialDialogBuilder("默认加密方法").apply {
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
fun SettingBox(content: @Composable () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth(),
        border = BorderStroke(2.dp, Color(0xFF036BBD)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x00ADE4FF),
        ),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Settings() {

    SettingBox {
        Text(text = "通用", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        CommonSwitch(checked = enableFloat, text = "悬浮窗开关:", onCheckedChangeListener = {
            if (!android.provider.Settings.canDrawOverlays(app) && it) {
                showToast("请先开启悬浮窗权限")
                return@CommonSwitch
            }
            enableFloat = it
            kv.encode("enable_float", it)
            if (!it) {
                return@CommonSwitch stopFloat()
            }
            startFloat()
        })

        CommonSwitch(
            checked = USE_RANDOM_PASSWORD,
            text = "启用随机密码:",
            onCheckedChangeListener = {
                setUseRandomPassword(it)
            },
            "启用后加密时将随机选择已有的密钥"
        )
        CommonSwitch(
            checked = USE_RANDOM_ENCODER,
            text = "启用随机编码:",
            onCheckedChangeListener = {
                setUseRandomEncoder(it)
            },
            "启用后加密时将使用随机编码方法"
        )
        CommonSwitch(
            checked = USE_STRICT_ENCODE,
            text = "启用严格编码:",
            onCheckedChangeListener = {
                setUseStrictEncode(it)
            },
            "启用后将会加密所有字符内容(包括特殊字符),只支持除移位加密以外的算法"
        )
        CommonSwitch(
            checked = copyWhenRefresh,
            text = "刷新时自动复制:",
            onCheckedChangeListener = {
                setEnableCopyWhenRefresh(it)
            },
            "启用后刷新加密结果后自动复制新内容"
        )
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
                color = LightColorScheme.primary,
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

    SettingBox {
        AutoDecodeSetting()
    }
    SettingBox {
        Text(text = "一键发送设置", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        CommonSwitch(
            checked = SCAN_BUTTON_WHEN_CLICK,
            text = "点击发送和输入自动更新:",
            onCheckedChangeListener = {
                setScanButtonWhenClick(it)
            },
            "启用后点击任何发送字样的按钮和文字以及输入框，将会设置为一键发送使用的输入框"
        )
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
                SEARCH_BUTTON_TIMEOUT = (newValue.toLongOrNull() ?: 100).coerceAtLeast(100)
            },
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 10.dp),
            label = { Text("搜索按钮超时时间(毫秒)") }
        )
    }
    SettingBox {
        Text(text = "其他", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        FlowRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "默认加密方法: $DEFAULT_ENCODER",
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Button(onClick = {
                selectDefaultEncoder()
            }) {
                Text(text = "设置")
            }
        }
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
            onCheckedChangeListener = {
                useDefaultPrefix = it
                kv.encode("use_default_prefix", it)
            },
            "关闭则每次手动输入前缀"
        )
        CommonSwitch(
            checked = START_BLANK_SCREEN,
            text = "启动白屏:",
            onCheckedChangeListener = {
                if (it) {
                    showToast("使用双指放大解锁")
                }
                START_BLANK_SCREEN = it
                kv.encode("start_blank_screen", it)
            },
            "开启后APP启动显示白屏(双指放大解锁)"
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "APP伪装:",
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Button(onClick = {
                MaterialDialogBuilder("APP伪装").apply {
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
            }) {
                Text(text = "设置")
            }
        }
    }
    SettingBox {
        Text(text = "统计信息", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        FlowRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "已加密信息次数: ",
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Text(
                text = "$ENCODE_COUNT",
                color = LightColorScheme.primary,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
        FlowRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "已成功解密信息次数: ",
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Text(
                text = "$SUCCESS_DECODE_COUNT",
                color = LightColorScheme.primary,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
        Button(onClick = {
            MaterialDialogBuilder("确定重置统计?").apply {
                setPositiveButton("确定") {
                    resetStaticCount()
                    it()
                }
                show()
            }
        }) {
            Text(text = "重置")
        }
    }
}