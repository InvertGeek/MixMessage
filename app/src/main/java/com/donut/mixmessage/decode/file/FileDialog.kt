package com.donut.mixmessage.decode.file

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.donut.mixfile.server.core.utils.parseFileMimeType
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.util.common.TipText
import com.donut.mixmessage.util.common.startActivity
import com.donut.mixmessage.util.file.saveFile

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FileContent(url: String, fileName: String, size: Long) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            fileName,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = colorScheme.primary,
            textDecoration = TextDecoration.Underline
        )

        if (fileName.parseFileMimeType().toString().startsWith("video/")) {
            ElevatedButton({
                showVideoDialog(url)
            }) {
                Text("播放视频")
            }
        }

        if (fileName.parseFileMimeType().toString().startsWith("image/")) {
            ElevatedButton({
                showImageDialog(url)
            }) {
                Text("查看图片")
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(onClick = {
                val intent =
                    Intent(
                        Intent.ACTION_VIEW,
                        url.toUri()
                    )
                startActivity(intent)
            }) {
                Text(text = "在浏览器打开")
            }

            Button(onClick = {
                saveFile(url, fileName)
            }) {
                Text(text = "下载文件")
            }

        }

    }
    TipText(
        "大小: ${formatFileSize(size)}"
    )
}

fun showVideoDialog(url: String) {
    MixDialogBuilder("播放视频").apply {
        setContent {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.padding(10.dp)
            ) {
                VideoPlayerContent(url)
            }
        }
        setDefaultNegative("关闭")
        show()
    }

}

fun showImageDialog(url: String) {
    MixDialogBuilder("查看图片").apply {
        setContent {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.padding(10.dp)
            ) {
                ImageContent(url)
            }
        }
        setDefaultNegative("关闭")
        show()
    }

}