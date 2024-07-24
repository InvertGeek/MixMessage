package com.donut.mixmessage.util.image.apis

import com.donut.mixmessage.decode.image.ProgressContent
import com.donut.mixmessage.util.common.catchError
import com.donut.mixmessage.util.common.isFalse
import com.donut.mixmessage.util.common.isNull
import com.donut.mixmessage.util.image.ImageAPI
import com.donut.mixmessage.util.image.apis.bb.sCode
import com.donut.mixmessage.util.image.fileFormHeaders
import com.google.gson.JsonObject
import io.ktor.client.call.body
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody

object ChatBot : ImageAPI(
    "迧峷籡魜刞蜯當屭毸://從夲鲥椶筪捜浲.澬質謉蛉掂暏.鈰譛.騟饄麈/繜珹魐綖飧坉扐睧/懷騼萸趩殲諃/鉒婗嫤厅庆匉茘腞襋蔥侟饫劲宽暏鵏吝禧璉蘼鉖鴕霽汋谡桃楄遍鏏术/垟鋛娜/".sCode,
    "极速2(推荐)"
) {

    override suspend fun uploadImage(image: ByteArray, progressContent: ProgressContent): String? {
        catchError {
            val result = client.post("upload") {
                setBody(MultiPartFormDataContent(formData {
                    append("media", image, fileFormHeaders())
                }))
                onUpload(progressContent.ktorListener)
            }.body<JsonObject>()
            result.isNull {
                return null
            }
            result.has("url").isFalse {
                return null
            }
            return result.get("url").asString
        }
        return null
    }

}