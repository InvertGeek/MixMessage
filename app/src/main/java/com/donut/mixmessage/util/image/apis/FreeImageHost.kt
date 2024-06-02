package com.donut.mixmessage.util.image.apis

import com.donut.mixmessage.decode.image.ProgressContent
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.catchError
import com.donut.mixmessage.util.common.isFalse
import com.donut.mixmessage.util.common.isNull
import com.donut.mixmessage.util.image.ImageAPI
import com.donut.mixmessage.util.image.fileFormHeaders
import com.google.gson.JsonObject
import io.ktor.client.call.body
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody

var FREEIMAGEHOST_KEY by cachedMutableOf(
    "6d207e02198a847aa98d0a2a901485a5",
    "freeimagehost_api_key"
)

object FreeImageHost : ImageAPI("https://freeimage.host/api/1/", "FreeImageHost") {

    override suspend fun uploadImage(image: ByteArray, progressContent: ProgressContent): String? {
        catchError {
            val result = client.post("upload") {
                setBody(MultiPartFormDataContent(formData {
                    append("key", FREEIMAGEHOST_KEY.trim())
                    append("source", image, fileFormHeaders())
                }))
                onUpload(progressContent.ktorListener)
            }.body<JsonObject>()
            result.isNull {
                return null
            }
            result.has("image").isFalse {
                return null
            }
            return result.get("image").asJsonObject.get("url").asString
        }
        return null
    }
}