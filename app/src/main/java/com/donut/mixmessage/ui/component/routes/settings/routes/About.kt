package com.donut.mixmessage.ui.component.routes.settings.routes

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donut.mixmessage.currentActivity
import com.donut.mixmessage.ui.component.common.MaterialDialogBuilder
import com.donut.mixmessage.ui.component.nav.MixNavPage
import com.donut.mixmessage.ui.component.nav.NavTitle
import com.donut.mixmessage.ui.component.routes.settings.SettingBox
import com.donut.mixmessage.ui.theme.LightColorScheme
import com.donut.mixmessage.util.encode.ENCODE_COUNT
import com.donut.mixmessage.util.encode.SUCCESS_DECODE_COUNT
import com.donut.mixmessage.util.encode.resetStaticCount


@OptIn(ExperimentalLayoutApi::class)
val AboutPage = MixNavPage(
    "settings_about",
    gap = 10.dp,
    displayNavBar = false,
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
        OutlinedButton(onClick = {
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
    ClickableText(
        text = buildAnnotatedString {
            append("项目地址: https://gitlab.com/invertgeek1/MixMessage")
        },
        onClick = {
            MaterialDialogBuilder("确定打开?").apply {
                setPositiveButton("确定") {
                    val intent =
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://gitlab.com/invertgeek1/MixMessage")
                        )
                    currentActivity.startActivity(intent)
                }
                setDefaultNegative()
                show()
            }
        }
    )
}
