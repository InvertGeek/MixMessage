package com.donut.mixmessage.ui.routes.settings.routes

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.donut.mixmessage.ui.nav.MixNavPage
import com.donut.mixmessage.ui.nav.NavTitle
import com.donut.mixmessage.ui.routes.settings.SettingButton
import com.donut.mixmessage.util.mixfile.MIXFILE_UPLOADER
import com.donut.mixmessage.util.mixfile.selectUploader


@OptIn(ExperimentalLayoutApi::class)
val FileUploadPage = MixNavPage(
    gap = 10.dp,
    displayNavBar = false,
    useTransition = true,
) {
    NavTitle(title = "文件上传设置", showBackIcon = true)
    SettingButton(text = "上传线路: $MIXFILE_UPLOADER") {
        selectUploader()
    }
    Text(
        color = Color.Gray,
        text = """
        文件上传: 所有文件都会以图片的形式上传,无论是视频还是文档还是其他文件，都会将文件以随机的密钥使用AES-GCM-256算法加密后,
        封装到多张随机颜色的空白图片中,最后将所有图片信息聚合到索引信息中,再把索引信息上传为一张图片,下载时根据索引逆向还原
    """.trimIndent()
    )
}
