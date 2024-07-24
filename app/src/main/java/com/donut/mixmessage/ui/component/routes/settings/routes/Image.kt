package com.donut.mixmessage.ui.component.routes.settings.routes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.donut.mixmessage.ui.component.common.CommonSwitch
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.ui.component.common.SingleSelectItemList
import com.donut.mixmessage.ui.component.nav.MixNavPage
import com.donut.mixmessage.ui.component.nav.NavTitle
import com.donut.mixmessage.ui.component.routes.settings.SettingButton
import com.donut.mixmessage.ui.theme.colorScheme
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.image.CURRENT_IMAGE_API
import com.donut.mixmessage.util.image.IMAGE_APIS
import com.donut.mixmessage.util.image.IMAGE_COMPRESS_RATE
import com.donut.mixmessage.util.image.apis.FREEIMAGEHOST_KEY
import com.donut.mixmessage.util.image.apis.FreeImageHost
import com.donut.mixmessage.util.image.apis.bfs.BFS
import com.donut.mixmessage.util.image.apis.bfs.BFS_COOKIE
import com.donut.mixmessage.util.image.apis.imgbb.IMGBB
import com.donut.mixmessage.util.image.apis.imgbb.IMGBB_API_TOKEN
import com.donut.mixmessage.util.image.apis.smms.SMMS
import com.donut.mixmessage.util.image.apis.smms.SMMS_API_TOKEN

fun selectImageAPI() {
    MixDialogBuilder("图片API").apply {
        setContent {
            SingleSelectItemList(
                items = IMAGE_APIS,
                getLabel = { it.name },
                currentOption = IMAGE_APIS.firstOrNull {
                    it.name == CURRENT_IMAGE_API
                } ?: SMMS
            ) { option ->
                CURRENT_IMAGE_API = option.name
                closeDialog()
            }
        }
        show()
    }
}

var ENABLE_IMAGE_COMPRESS by cachedMutableOf(false, "ENABLE_IMAGE_COMPRESS")

@OptIn(ExperimentalLayoutApi::class)
val ImagePage = MixNavPage(
    gap = 10.dp,
    displayNavBar = false,
    useTransition = true,
) {
    NavTitle(title = "图片上传设置", showBackIcon = true)
    Column {
        Text(
            modifier = Modifier.padding(10.dp, 0.dp),
            text = "图片压缩质量(动图不会压缩): $IMAGE_COMPRESS_RATE",
            color = colorScheme.primary
        )
        Slider(
            value = IMAGE_COMPRESS_RATE.toFloat() / 100f,
            steps = 100,
            modifier = Modifier.fillMaxWidth(),
            onValueChange = {
                IMAGE_COMPRESS_RATE = (it * 100).toLong()
            }
        )
    }
    CommonSwitch(
        checked = ENABLE_IMAGE_COMPRESS,
        text = "启用图片压缩:",
        "启用后发送图片时将会进行有损压缩(建议上传失败时启用)",
    ) {
        ENABLE_IMAGE_COMPRESS = it
    }
    SettingButton(text = "图片API: $CURRENT_IMAGE_API") {
        selectImageAPI()
    }
    when (CURRENT_IMAGE_API) {
        SMMS.name -> {
            OutlinedTextField(
                value = SMMS_API_TOKEN,
                onValueChange = {
                    SMMS_API_TOKEN = it
                },
                label = { Text("SM.MS的API Token,在官网获取") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        IMGBB.name -> {
            OutlinedTextField(
                value = IMGBB_API_TOKEN,
                onValueChange = {
                    IMGBB_API_TOKEN = it
                },
                label = { Text("IMGBB的API Token,在官网获取") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        BFS.name -> {
            OutlinedTextField(
                value = BFS_COOKIE,
                onValueChange = {
                    BFS_COOKIE = it
                },
                label = { Text("BILIBILI的Cookie") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        FreeImageHost.name -> {
            OutlinedTextField(
                value = FREEIMAGEHOST_KEY,
                onValueChange = {
                    FREEIMAGEHOST_KEY = it
                },
                label = { Text("FreeImageHost key") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    Text(
        color = Color.Gray,
        text = """
        文件上传: 所有文件都会以图片的形式上传,无论是视频还是文档还是其他文件，都会将文件以随机的密钥使用aes-gcm算法加密后,
        封装到一张随机颜色的空白图片中,发送时会将本次随机的密钥和图片地址以及文件大小等信息二次加密后发送给对方
    """.trimIndent()
    )
}
