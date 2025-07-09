package com.donut.mixfile.server.core.objects

import com.alibaba.fastjson2.annotation.JSONField
import com.alibaba.fastjson2.to
import com.alibaba.fastjson2.toJSONString
import com.donut.mixfile.server.core.Uploader
import com.donut.mixfile.server.core.aes.decryptAES
import com.donut.mixfile.server.core.aes.encryptAES
import com.donut.mixfile.server.core.utils.basen.Alphabet
import com.donut.mixfile.server.core.utils.basen.BigIntBaseN
import com.donut.mixfile.server.core.utils.extensions.mb
import com.donut.mixfile.server.core.utils.hashMD5
import com.donut.mixfile.server.core.utils.parseFileMimeType
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.Url
import io.ktor.http.contentLength
import io.ktor.utils.io.discard


data class MixShareInfo(
    @JSONField(name = "f") val fileName: String,
    @JSONField(name = "s") val fileSize: Long,
    @JSONField(name = "h") val headSize: Int,
    @JSONField(name = "u") val url: String,
    @JSONField(name = "k") val key: String,
    @JSONField(name = "r") val referer: String,
) {

    @JSONField(serialize = false)
    var cachedCode: String? = null

    companion object {

        val ENCODER =
            BigIntBaseN(Alphabet.fromString("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"))

        fun fromString(string: String) = fromJson(dec(string))

        fun tryFromString(string: String) = try {
            fromString(string).also {
                it.cachedCode = string
            }
        } catch (e: Exception) {
            null
        }

        private fun fromJson(json: String): MixShareInfo =
            json.to()

        private fun enc(input: String): String {
            val bytes = input.encodeToByteArray()
            val result = encryptAES(bytes, "123".hashMD5())
            return ENCODER.encode(result)
        }

        private fun dec(input: String): String {
            val bytes = ENCODER.decode(input)
            val result = decryptAES(bytes, "123".hashMD5())
            return result!!.decodeToString()
        }

    }

    fun shareCode() = enc(toJson()).also { cachedCode = it }


    override fun toString(): String {
        return cachedCode ?: shareCode()
    }

    fun toDataLog(): FileDataLog {
        return FileDataLog(
            shareInfoData = this.toString(),
            name = this.fileName,
            size = this.fileSize
        )
    }


    private fun toJson(): String = this.toJSONString()

    suspend fun fetchFile(
        url: String,
        client: HttpClient,
        referer: String = this.referer,
        limit: Int = 20.mb
    ): ByteArray {
        val transformedUrl = Uploader.transformUrl(url)
        val transformedReferer = Uploader.transformReferer(url, referer)
        val result: ByteArray = client.config {
            install(HttpRequestRetry) {
                maxRetries = 3
                retryOnException(retryOnTimeout = true)
                retryOnServerErrors()
                delayMillis { retry ->
                    retry * 100L
                }
            }
        }.prepareGet(transformedUrl) {
            if (transformedReferer.isNotEmpty()) {
                header("Referer", transformedReferer)
            }
        }.execute {
            val contentLength = it.contentLength() ?: 0
            // iv + ghash 各96位,12字节,共24字节
            if (contentLength > (limit + headSize + 24)) {
                throw Exception("分片文件过大")
            }
            val channel = it.bodyAsChannel()
            channel.discard(headSize.toLong())
            decryptAES(channel, ENCODER.decode(key), limit)
        }
        val hash = Url(url).fragment.trim()
        if (hash.isNotEmpty()) {
            val currentHash = result.hashMixSHA256()
            if (!currentHash.contentEquals(hash)) {
                throw Exception("文件遭到篡改")
            }
        }
        return result
    }

    override fun hashCode(): Int {
        return url.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other is MixShareInfo) {
            return url == other.url
        }
        return false
    }

    fun contentType() = fileName.parseFileMimeType()

    suspend fun fetchMixFile(client: HttpClient, referer: String = this.referer): MixFile {
        val decryptedBytes = fetchFile(url, client = client, referer = referer)
        return MixFile.fromBytes(decryptedBytes)
    }

}
