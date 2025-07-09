package com.donut.mixfile.server.core.objects


import com.alibaba.fastjson2.annotation.JSONField
import com.alibaba.fastjson2.to
import com.alibaba.fastjson2.toJSONString
import com.donut.mixfile.server.core.utils.compressGzip
import com.donut.mixfile.server.core.utils.decompressGzip
import com.donut.mixfile.server.core.utils.hashSHA256


fun ByteArray.hashMixSHA256() = MixShareInfo.ENCODER.encode(hashSHA256())


data class MixFile(
    @JSONField(name = "chunk_size") val chunkSize: Int,
    @JSONField(name = "file_size") val fileSize: Long,
    @JSONField(name = "version") val version: Long,
    @JSONField(name = "file_list") val fileList: List<String>,
) {

    companion object {
        fun fromBytes(data: ByteArray): MixFile =
            decompressGzip(data).to()
    }

    fun getFileListByStartRange(startRange: Long): List<Pair<String, Int>> {
        val startIndex = (startRange / chunkSize).toInt()
        val startOffset = (startRange % chunkSize).toInt()
        return fileList.subList(startIndex, fileList.size)
            .mapIndexed { index, file ->
                val offset = if (index == 0) startOffset else 0
                file to offset
            }
    }


    fun toBytes() = compressGzip(this.toJSONString())

}