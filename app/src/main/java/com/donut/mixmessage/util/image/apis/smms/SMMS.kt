package com.donut.mixmessage.util.image.apis.smms

import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.catchError
import com.donut.mixmessage.util.image.ImageAPI
import com.donut.mixmessage.util.image.toMultiPart
import okhttp3.MultipartBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


var SMMS_API_TOKEN by cachedMutableOf("3rKeiI3eIBYqFm7umT5RLKndPcXkCjKE", "smms_api_token")

object SMMS : ImageAPI("https://smms.app/api/v2/", "SM.MS") {

    interface ApiService {
        @POST("upload")
        @Multipart
        suspend fun upload(
            @Part file: MultipartBody.Part,
            @Header("Authorization") token: String = SMMS_API_TOKEN.trim()
        ): SMMSUploadResult
    }

    private val apiService: ApiService = retrofit.create(ApiService::class.java)
    override suspend fun uploadImage(image: ByteArray): String? {
        catchError {
            val smResult = apiService.upload(image.toMultiPart("smfile"))
            val resultData = smResult.data
            if (smResult.code.contentEquals("image_repeated")) {
                return smResult.images
            }
            return resultData?.url
        }
        return null
    }
}


