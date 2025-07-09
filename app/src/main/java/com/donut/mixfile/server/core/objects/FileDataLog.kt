package com.donut.mixfile.server.core.objects

import com.alibaba.fastjson2.toJSONString
import com.donut.mixfile.server.core.utils.compressGzip
import io.ktor.http.ContentType
import io.ktor.http.defaultForFilePath


data class FileDataLog(
    val shareInfoData: String,
    val name: String,
    val size: Long,
    val time: Long = System.currentTimeMillis(),
    private var category: String = "默认",
) {

    init {
        sanitizeCategory()
    }

    fun getCategory() = category

    fun setCategory(category: String) {
        this.category = category
        sanitizeCategory()
    }

    fun sanitizeCategory() {
        if (category.trim().isEmpty()) {
            category = "默认"
        }
        category = category.take(20)
    }

    fun isSimilar(other: FileDataLog): Boolean {
        return other.shareInfoData.contentEquals(shareInfoData)
    }


    override fun hashCode(): Int {
        var result = shareInfoData.hashCode()
        result = 31 * result + category.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (other !is FileDataLog) return false
        return isSimilar(other) && category.contentEquals(other.category)
    }
}

val FileDataLog.mimeType get() = ContentType.defaultForFilePath(name)

val FileDataLog.contentType get() = this.mimeType.contentType

val FileDataLog.contentSubType get() = this.mimeType.contentSubtype

val FileDataLog.isImage get() = this.contentType.contentEquals("image")

val FileDataLog.isVideo get() = this.contentType.contentEquals("video")

fun Collection<FileDataLog>.toByteArray(): ByteArray {
    val strData = this.toJSONString()
    val compressedData = compressGzip(strData)
    return compressedData
}