package com.donut.mixmessage.util.image.apis.imgbb

import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.catchError
import com.donut.mixmessage.util.common.isFalse
import com.donut.mixmessage.util.image.ImageAPI
import com.donut.mixmessage.util.image.toMultiPart
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

var IMGBB_API_TOKEN by cachedMutableOf("c5519d0780770a7f1d2bec104240fecd", "imgbb_api_token")

object IMGBB : ImageAPI("https://api.imgbb.com/1/", "IMGBB") {

    interface ApiService {
        @POST("upload")
        @Multipart
        suspend fun upload(
            @Part file: MultipartBody.Part,
            @Part("key") token: RequestBody = IMGBB_API_TOKEN.trim().toRequestBody()
        ): IMGBBUploadResult
    }

    private val apiService: ApiService = retrofit.create(ApiService::class.java)
    override suspend fun uploadImage(image: ByteArray): String? {
        catchError {
            val result = apiService.upload(image.toMultiPart("image"))
            val resultData = result.data
            result.success.isFalse {
                return null
            }
            return resultData?.url
        }
        return null
    }
}