package com.donut.mixmessage.util.image.apis.imgbb

import com.donut.mixmessage.decode.image.ProgressContent
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.catchError
import com.donut.mixmessage.util.common.isFalse
import com.donut.mixmessage.util.image.ImageAPI
import com.donut.mixmessage.util.image.fileFormHeaders
import io.ktor.client.call.body
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody

var IMGBB_API_TOKEN by cachedMutableOf("c5519d0780770a7f1d2bec104240fecd", "imgbb_api_token")

object IMGBB : ImageAPI("https://api.imgbb.com/1/", "IMGBB") {

    override suspend fun uploadImage(image: ByteArray, progressContent: ProgressContent): String? {
        catchError {
            val result = client.post("upload") {
                setBody(MultiPartFormDataContent(formData {
                    append("image", image, fileFormHeaders())
                }))
                onUpload(progressContent.ktorListener)
            }.body<IMGBBUploadResult>()
            val resultData = result.data
            result.success.isFalse {
                return null
            }
            return resultData?.url
        }
        return null
    }
}