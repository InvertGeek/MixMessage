package com.donut.mixmessage.util.image.apis.bfs

import com.donut.mixmessage.decode.image.ProgressContent
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.catchError
import com.donut.mixmessage.util.common.default
import com.donut.mixmessage.util.common.isNull
import com.donut.mixmessage.util.image.ImageAPI
import com.donut.mixmessage.util.image.fileFormHeaders
import io.ktor.client.call.body
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody

var BFS_COOKIE by cachedMutableOf(
    "",
    "bfs_cookie"
)

object BFS : ImageAPI("https://api.bilibili.com/x/dynamic/feed/draw/", "BILIBILI") {
    private fun getCookies() = BFS_COOKIE.split("; ").associate { cookie ->
        val (key, value) = cookie.split("=")
        key to value
    }

    override suspend fun uploadImage(image: ByteArray, progressContent: ProgressContent): String? {
        catchError {
            val result = client.post("upload_bfs") {
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("biz", "article")
                            append("csrf", getCookies()["bili_jct"].default(""))
                            append("file_up", image, fileFormHeaders())
                        },
                    )
                )
                onUpload(progressContent.ktorListener)
                header("cookie", BFS_COOKIE.trim())
            }.body<BFSResponse>()

            val resultData = result.data
            result.data.isNull {
                return null
            }
            return resultData?.imageUrl
        }
        return null
    }
}