package com.donut.mixmessage.util.image

import com.donut.mixmessage.app
import com.donut.mixmessage.decode.image.ProgressInterceptor
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.catchError
import com.donut.mixmessage.util.common.toInt
import com.donut.mixmessage.util.encode.encryptAES
import com.donut.mixmessage.util.encode.getCurrentPassword
import com.donut.mixmessage.util.image.apis.FreeImageHost
import com.donut.mixmessage.util.image.apis.bfs.BFS
import com.donut.mixmessage.util.image.apis.imgbb.IMGBB
import com.donut.mixmessage.util.image.apis.smms.SMMS
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url
import java.io.File
import java.util.concurrent.TimeUnit


var CURRENT_IMAGE_API by cachedMutableOf(SMMS.name, "current_image_api")

val IMAGE_APIS = listOf(
    SMMS,
    IMGBB,
    BFS,
    FreeImageHost
)

suspend fun startUploadImage(
    fileData: ByteArray,
    password: String = getCurrentPassword(),
): String? {
    val encryptedFileData = encryptAES(fileData, password)
    val blankImage = createBlankBitmap(50, 50)
    val useGif = CURRENT_IMAGE_API.contentEquals(BFS.name)
    val blankImageData = blankImage.compressToByteArray(webpQuality = 0, gifHeader = useGif)
    val byteArray = combineArray(blankImageData, encryptedFileData)
    return IMAGE_APIS.maxByOrNull {
        (CURRENT_IMAGE_API == it.name).toInt()
    }!!.uploadImage(byteArray)
}


abstract class ImageAPI(baseUrl: String, val name: String) {

    var retrofit: Retrofit = createRetrofit(baseUrl)

    interface DownloadService {
        @GET
        suspend fun download(
            @Url url: String
        ): Response<ResponseBody>
    }

    companion object {
        fun createRetrofit(
            baseUrl: String,
            client: OkHttpClient.Builder.() -> Unit = {}
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(OkHttpClient.Builder()
                    .apply(client)
                    .writeTimeout(120, TimeUnit.SECONDS)
                    .addNetworkInterceptor { chain ->
                        val requestBuilder = chain.request().newBuilder().apply {
                            header(
                                "User-Agent",
                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
                            )
                        }
                        chain.proceed(requestBuilder.build())
                    }
                    .build())
                .build()
        }

        suspend fun downloadEncryptedData(
            url: String,
            password: String,
            progressListener: ProgressInterceptor
        ): ByteArray? {
            catchError {
                val cacheSize = 100 * 1024 * 1024 // 100 MiB
                val cacheDirectory = File(app.cacheDir, "http-cache")
                val cache = Cache(cacheDirectory, cacheSize.toLong())
                return createRetrofit("http://127.0.0.1") {
                    addNetworkInterceptor(forceCacheInterceptor)
                    addNetworkInterceptor(genDecodeInterceptor(password))
                    cache(cache)
                    addNetworkInterceptor(progressListener)
                }.create(DownloadService::class.java).download(url).body()?.bytes()
            }
            return null
        }

    }

    abstract suspend fun uploadImage(image: ByteArray): String?
}