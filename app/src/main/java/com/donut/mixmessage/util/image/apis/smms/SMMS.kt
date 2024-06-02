package com.donut.mixmessage.util.image.apis.smms

import com.donut.mixmessage.decode.image.ProgressContent
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.catchError
import com.donut.mixmessage.util.image.ImageAPI
import com.donut.mixmessage.util.image.fileFormHeaders
import io.ktor.client.call.body
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody


var SMMS_API_TOKEN by cachedMutableOf("3rKeiI3eIBYqFm7umT5RLKndPcXkCjKE", "smms_api_token")

object SMMS : ImageAPI("https://smms.app/api/v2/", "SM.MS") {

    override suspend fun uploadImage(image: ByteArray, progressContent: ProgressContent): String? {
        catchError {
            val smResult = client.post("upload"){
                setBody(MultiPartFormDataContent(formData{
                    append("smfile",image, fileFormHeaders())
                }))
                header("Authorization", SMMS_API_TOKEN.trim())
                onUpload(progressContent.ktorListener)
            }.body<SMMSUploadResult>()
            val resultData = smResult.data
            if (smResult.code.contentEquals("image_repeated")) {
                return smResult.images
            }
            return resultData?.url
        }
        return null
    }
}


