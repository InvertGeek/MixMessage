package com.donut.mixmessage.ui.component.routes.settings.routes

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donut.mixmessage.currentActivity
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.ui.component.nav.MixNavPage
import com.donut.mixmessage.ui.component.nav.NavTitle
import com.donut.mixmessage.ui.component.routes.settings.SettingBox
import com.donut.mixmessage.ui.theme.colorScheme
import com.donut.mixmessage.util.encode.ENCODE_COUNT
import com.donut.mixmessage.util.encode.SUCCESS_DECODE_COUNT
import com.donut.mixmessage.util.encode.resetStaticCount


@OptIn(ExperimentalLayoutApi::class)
val AboutPage = MixNavPage(
    gap = 10.dp,
    displayNavBar = false,
    useTransition = true,
) {
    NavTitle(title = "关于", showBackIcon = true)
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
                color = colorScheme.primary,
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
                color = colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
        OutlinedButton(onClick = {
            MixDialogBuilder("确定重置统计?").apply {
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
    Text(
        color = colorScheme.primary,
        text = "项目地址: https://gitlab.com/ivgeek/MixMessage",
        modifier = Modifier.clickable {
            MixDialogBuilder("确定打开?").apply {
                setPositiveButton("确定") {
                    val intent =
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://gitlab.com/ivgeek/MixMessage")
                        )
                    currentActivity.startActivity(intent)
                    closeDialog()
                }
                setDefaultNegative()
                show()
            }
        }
    )
    Text(
        color = Color.Gray,
        text = """
        加密算法: 默认使用的是aes-gcm算法(移位编码除外),通用标准，目前国际上无人破解,会填充16字节的随机偏移,相同内容使用相同密钥加密会每次都将产生唯一的结果，永远不会重复
        文件上传: 上传的所有文件都会用aes算法加密后隐藏到一张空白图片中作为图片上传
        移位加密: 移位加密无法加密特殊字符,目前已知缺陷是容易受到已知明文攻击,
        也就是对方已经知道一条你发送的密文对应的明文是什么,有4亿分之一左右的概率解密出接下来你使用相同密钥加密的信息(能够解密出的有效信息长度为已知明文的最大长度)
        精简模式: 和移位加密原理相同,容易受到已知明文攻击,已经知道对应明文的情况下单条信息破解概率为6万分之一左右
    """.trimIndent()
    )
}
