package com.donut.mixmessage.util.image.apis

import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.catchError
import com.donut.mixmessage.util.common.isFalse
import com.donut.mixmessage.util.common.isNull
import com.donut.mixmessage.util.image.ImageAPI
import com.donut.mixmessage.util.image.toMultiPart
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

var FREEIMAGEHOST_KEY by cachedMutableOf(
    "6d207e02198a847aa98d0a2a901485a5",
    "freeimagehost_api_key"
)

object FreeImageHost : ImageAPI("https://freeimage.host/api/1/", "FreeImageHost") {

    interface ApiService {
        @POST("upload")
        @Multipart
        suspend fun upload(
            @Part file: MultipartBody.Part,
            @Part("key") requestType: RequestBody = FREEIMAGEHOST_KEY.trim()
                .toRequestBody(),
        ): Response<ResponseBody>
    }

    private val apiService: ApiService = retrofit.create(ApiService::class.java)
    override suspend fun uploadImage(image: ByteArray): String? {
        catchError {
            val result = apiService.upload(image.toMultiPart("source")).body()?.string()
            result.isNull {
                return null
            }
            val jsonData = Gson().fromJson(result, JsonObject::class.java)
            jsonData.has("image").isFalse {
                return null
            }
            return jsonData.get("image").asJsonObject.get("url").asString
        }
        return null
    }
}