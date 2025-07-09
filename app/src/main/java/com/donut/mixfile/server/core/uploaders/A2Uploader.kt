package com.donut.mixfile.server.core.uploaders

import com.alibaba.fastjson2.to
import com.donut.mixfile.server.core.Uploader
import com.donut.mixfile.server.core.utils.add
import com.donut.mixfile.server.core.utils.decodeHex
import com.donut.mixfile.server.core.utils.fileFormHeaders
import com.donut.mixfile.server.core.utils.genRandomString
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


val String.sCode: String
    get() = decodeHex(this)


object A2Uploader : Uploader("线路A2") {

    private val domain =
        "d︆︈︇︄︇︄︇︀︇︃︃︊︂️︂️︇︇︇︇︇︇︂︎︇︇︆︊︇︈︂︎︆︃︆︎︂️wa".sCode

    init {
        //旧文件兼容
        registerUrlTransform("A2") {
            it.replace(
                "CO︆︈︇︄︇︄︇︀︇︃︃︊︂️︂️︇︀︆︌︆︁︇︄︂︍︇︃︆︈︂︍︆︃︆️︆︍︆︍︇︅︆︎︆︉︇︄︇︉︂︍︇︀︇︂︆️︆︄︂︍︇︅︇︀︆︌︆️︆︁︆︄︂︍︇︅︆︇︆︃︂︎︆️︇︃︇︃︂︍︆︃︆︎︂︍︇︃︆︈︆︁︆︎︆︇︆︈︆︁︆︉︂︎︆︁︆︌︆︉︇︉︇︅︆︎︆︃︇︃︂︎︆︃︆️︆︍︂️gP".sCode,
                "d5︆︈︇︄︇︄︇︀︇︃︃︊︂️︂️︇︅︇︀︆︌︆️︆︁︆︄︂︍︆︂︆︂︇︃︂︎︆︍︆︉︇︉︆️︇︅︇︃︆︈︆︅︂︎︆︃︆️︆︍︂️w3".sCode
            )
        }
    }

    data class Token(
        val host: String,
        val accessid: String,
        val policy: String,
        val signature: String,
        val dir: String,
    )


    override val referer: String
        get() = domain

    var tokenCache: Token? = null
    var tokenCacheTime: Long = 0
    val tokenLock = Mutex()

    private suspend fun getToken(client: HttpClient): Token {
        tokenLock.withLock {
            val cached = tokenCache
            if (cached != null && System.currentTimeMillis() - tokenCacheTime < 1000 * 60) {
                return cached
            }
            val response =
                client.get("${domain}handler/getoss.ashx")
            if (!response.status.isSuccess()) {
                throw Exception("上传失败")
            }
            val data: Token = response.body<String>().to()
            tokenCache = data
            tokenCacheTime = System.currentTimeMillis()
            return data
        }
    }

    override suspend fun doUpload(fileData: ByteArray, client: HttpClient): String {
        val token = getToken(client)

        val key = "${token.dir}${genRandomString()}"

        val response = client.submitFormWithBinaryData(token.host, formData {
            add("policy", token.policy)
            add("OSSAccessKeyId", token.accessid)
            add("Signature", token.signature)
            add("key", key)
            add("content-type", "image/gif")
            add("file", fileData, fileFormHeaders())
        })

        if (!response.status.isSuccess()) {
            throw Exception("上传失败")
        }

        return "${"a︆︈︇︄︇︄︇︀︇︃︃︊︂️︂️︇︀︇︅︆︂︆︎︆︅︇︇︆︆︇︂︂︎︇︀︆︁︇︀︆︅︇︂︆️︆︌︂︎︆︃︆︎︂️wa".sCode}/${key}"
    }
}