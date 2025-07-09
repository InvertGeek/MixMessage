package com.donut.mixmessage.util.mixfile

import com.alibaba.fastjson2.toJSONString
import com.donut.mixfile.server.core.MixFileServer
import com.donut.mixfile.server.core.Uploader
import com.donut.mixfile.server.core.uploaders.A1Uploader
import com.donut.mixfile.server.core.uploaders.A2Uploader
import com.donut.mixfile.server.core.uploaders.A3Uploader
import com.donut.mixfile.util.file.uploadLogs
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.ui.component.common.SingleSelectItemList
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.mixfile.image.createBlankBitmap
import com.donut.mixmessage.util.mixfile.image.toGif
import java.io.InputStream

val UPLOADERS = listOf(A1Uploader, A2Uploader, A3Uploader)

var MIXFILE_UPLOADER by cachedMutableOf(A2Uploader.name, "mixfile_uploader")

fun getCurrentUploader() =
    UPLOADERS.firstOrNull { it.name.contentEquals(MIXFILE_UPLOADER) } ?: A1Uploader

fun selectUploader() {
    MixDialogBuilder("上传线路").apply {
        setContent {
            SingleSelectItemList(
                items = UPLOADERS,
                getLabel = { it.name },
                currentOption = UPLOADERS.firstOrNull {
                    it.name.contentEquals(MIXFILE_UPLOADER)
                } ?: A1Uploader
            ) { option ->
                MIXFILE_UPLOADER = option.name
                closeDialog()
            }
        }
        show()
    }
}

val server = object : MixFileServer(
    serverPort = 8014,
) {
    override val downloadTaskCount: Int
        get() = 5

    override val uploadTaskCount: Int
        get() = 10

    override val uploadRetryCount
        get() = 10

    override fun onError(error: Throwable) {

    }


    override fun getUploader(): Uploader {
        return getCurrentUploader()
    }

    override suspend fun getStaticFile(path: String): InputStream? {
        val classLoader = object {}.javaClass.classLoader
        // 加载资源文件，路径相对于 resources 目录
        return classLoader?.getResourceAsStream("files/${path}")
    }

    override suspend fun genDefaultImage(): ByteArray {
        return createBlankBitmap().toGif()
    }

    override suspend fun getFileHistory(): String {
        return uploadLogs.asReversed().toJSONString()
    }


}