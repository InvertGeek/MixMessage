package com.donut.mixmessage.util.image.apis.bfs

import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.catchError
import com.donut.mixmessage.util.common.isNull
import com.donut.mixmessage.util.image.ImageAPI
import com.donut.mixmessage.util.image.toMultiPart
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

var BFS_COOKIE by cachedMutableOf(
    "",
    "bfs_cookie"
)

object BFS : ImageAPI("https://api.bilibili.com/x/dynamic/feed/draw/", "BILIBILI") {
    interface ApiService {
        @POST("upload_bfs")
        @Multipart
        suspend fun upload(
            @Part file: MultipartBody.Part,
            @Part("biz") biz: RequestBody = "article".toRequestBody(),
            @Part("csrf") csrf: RequestBody = (getCookies()["bili_jct"] ?: "").toRequestBody(),
            @Header("cookie") cookie: String = BFS_COOKIE.trim()
        ): BFSResponse
    }

    fun getCookies() = BFS_COOKIE.split("; ").associate { cookie ->
        val (key, value) = cookie.split("=")
        key to value
    }

    private val apiService: ApiService = retrofit.create(ApiService::class.java)
    override suspend fun uploadImage(image: ByteArray): String? {
        catchError {
            val result = apiService.upload(image.toMultiPart("file_up"))
            val resultData = result.data
            result.data.isNull {
                return null
            }
            return resultData?.imageUrl
        }
        return null
    }
}