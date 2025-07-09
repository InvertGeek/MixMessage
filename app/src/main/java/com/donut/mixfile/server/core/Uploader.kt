package com.donut.mixfile.server.core

import com.donut.mixfile.server.core.aes.encryptAES
import com.donut.mixfile.server.core.objects.hashMixSHA256
import com.donut.mixfile.server.core.utils.isValidURL
import com.donut.mixfile.server.core.utils.retry
import io.ktor.client.HttpClient
import io.ktor.http.URLBuilder
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

abstract class Uploader(val name: String) {

    open val referer = ""

    abstract suspend fun doUpload(fileData: ByteArray, client: HttpClient): String

    companion object {
        val urlTransforms = mutableMapOf<String, (String) -> String>()
        val refererTransforms = mutableMapOf<String, (url: String, referer: String) -> String>()

        fun transformUrl(url: String): String {
            return urlTransforms.entries.fold(url) { acc, (_, transform) ->
                transform(acc)
            }.trim()
        }

        fun transformReferer(url: String, referer: String): String {
            return refererTransforms.entries.fold(referer) { acc, (_, transform) ->
                transform(url, acc)
            }.trim()
        }

        fun registerUrlTransform(name: String, transform: (String) -> String) {
            urlTransforms[name] = transform
        }

        fun registerRefererTransform(
            name: String,
            transform: (url: String, referer: String) -> String,
        ) {
            refererTransforms[name] = transform
        }
    }

    suspend fun upload(
        head: ByteArray,
        fileData: ByteArray,
        key: ByteArray,
        mixFileServer: MixFileServer
    ): String {

        return retry(times = mixFileServer.uploadRetryCount, delay = 100) {
            val encryptedData = encryptBytes(head, fileData, key)
            try {
                val url = doUpload(
                    encryptedData,
                    mixFileServer.httpClient
                )
                if (!isValidURL(url)) {
                    throw Exception("url格式错误")
                }
                URLBuilder(url).apply {
                    fragment = fileData.hashMixSHA256()
                }.buildString()
            } finally {
                mixFileServer.onUploadData(encryptedData)
            }
        }
    }

    open suspend fun genHead(client: HttpClient): ByteArray? = null

    private fun encryptBytes(head: ByteArray, fileData: ByteArray, key: ByteArray): ByteArray {
        return head + (encryptAES(fileData, key))
    }

}