package com.donut.mixmessage.util.image.apis.bb

import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.OSSClient
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import com.donut.mixmessage.app
import com.donut.mixmessage.decode.image.ProgressContent
import com.donut.mixmessage.util.common.catchError
import com.donut.mixmessage.util.common.genRandomString
import com.donut.mixmessage.util.common.hashToMD5String
import com.donut.mixmessage.util.encode.encoders.ShiftEncoder
import com.donut.mixmessage.util.image.ImageAPI
import com.google.gson.JsonObject
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


val String.sCode: String
    get() = ShiftEncoder.decode(this).text


object BB : ImageAPI("疴析鼞逴歷婩蕃礱隄://赛磀哹.瞔茓電乌悍.葕挱/".sCode, "极速(推荐)") {
    private suspend fun getToken(): String? {
        val response =
            client.get("榟晥髸鬄掸砳鄰翍/釣茄砷/嶈曋滦祕躂?诧昼煋鵆耵=礓睱內蒌芚叛&杈玺癄堣窾秱誴葞岁=纮让钆秤脅蟈杅蟎鍷橞穥誃鄜茋崴鏞煾笷刨颲肟犒鉃倕烝捇槽羦殿冘".sCode)
                .body<JsonObject>()
        return response.get("token")?.asString
    }

    private suspend fun getUploadToken() =
        client.post("${"徢溶決滭鍙讏濫碽/羬无柨/竲娹赦/嫰焌騋/璺媥涙玫朷?卤嫡楪覕嗤=".sCode}${getToken()}") {
            contentType(ContentType.Application.Json)
            setBody(JsonObject().apply {
                addProperty("appId", "抅縱胕寙伈孨歋蟼壎銛".sCode)
                addProperty("duration", "9000")
            }.toString())
        }.body<BBUploadBean>()

    override suspend fun uploadImage(image: ByteArray, progressContent: ProgressContent): String? {
        catchError {
            val token = getUploadToken()
            val (env, credential, resList) = token

            val credentialProvider: OSSCredentialProvider =
                OSSStsTokenCredentialProvider(
                    credential.accessKeyId,
                    credential.accessKeySecret,
                    credential.securityToken
                )

            val ossClient = OSSClient(app, env.publicEndpoint, credentialProvider)

            val key = resList[0] + genRandomString(32).hashToMD5String() + ".jpg"

            return suspendCancellableCoroutine { continuation ->
                var task: OSSAsyncTask<PutObjectResult>? = null
                val putRequest = PutObjectRequest(
                    env.bucket,
                    key,
                    image
                ).apply {
                    setProgressCallback { request, currentSize, totalSize ->
                        progressContent.updateProgress(currentSize, totalSize)
                    }
                }
                continuation.invokeOnCancellation {
                    task?.cancel()
                }
                task = ossClient.asyncPutObject(
                    putRequest, object : OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
                        override fun onSuccess(
                            request: PutObjectRequest?,
                            result: PutObjectResult?
                        ) {
                            continuation.resume("${"肞駌矨銍瘷昅鯚信夹://増擾挸礣.騝蒱厏菪灂萺萦齫鑸.南缞瀻/".sCode}$key")
                        }

                        override fun onFailure(
                            request: PutObjectRequest?,
                            clientException: ClientException?,
                            serviceException: ServiceException?
                        ) {
                            continuation.resume(null)
                        }
                    })
                task?.waitUntilFinished()
            }
        }
        return null
    }
}