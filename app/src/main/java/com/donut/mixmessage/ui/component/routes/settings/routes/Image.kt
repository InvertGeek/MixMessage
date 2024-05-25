package com.donut.mixmessage.ui.component.routes.settings.routes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.ui.component.common.SingleSelectItemList
import com.donut.mixmessage.ui.component.nav.MixNavPage
import com.donut.mixmessage.ui.component.nav.NavTitle
import com.donut.mixmessage.ui.component.routes.settings.SettingButton
import com.donut.mixmessage.ui.theme.colorScheme
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
    SettingButton(text = "图片API: $CURRENT_IMAGE_API") {
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
}
