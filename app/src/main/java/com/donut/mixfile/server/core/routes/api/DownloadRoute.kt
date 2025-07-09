package com.donut.mixfile.server.core.routes.api

import com.donut.mixfile.server.core.MixFileServer
import com.donut.mixfile.server.core.objects.MixFile
import com.donut.mixfile.server.core.objects.MixShareInfo
import com.donut.mixfile.server.core.utils.SortedTask
import com.donut.mixfile.server.core.utils.encodeURL
import com.donut.mixfile.server.core.utils.extensions.ifNullOrBlank
import com.donut.mixfile.server.core.utils.extensions.mb
import com.donut.mixfile.server.core.utils.parseFileMimeType
import com.donut.mixfile.server.core.utils.resolveMixShareInfo
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.ranges
import io.ktor.server.response.contentRange
import io.ktor.server.response.header
import io.ktor.server.response.respondBytesWriter
import io.ktor.server.response.respondText
import io.ktor.server.routing.RoutingHandler
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.close
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.nio.ByteBuffer


fun MixFileServer.getDownloadRoute(): RoutingHandler {
    return route@{
        val param = call.parameters
        val shareInfoData = param["s"]
        if (shareInfoData == null) {
            call.respondText("分享信息为空", status = HttpStatusCode.InternalServerError)
            return@route
        }
        val shareInfo = resolveMixShareInfo(shareInfoData)
        if (shareInfo == null) {
            call.respondText("解析文件失败", status = HttpStatusCode.InternalServerError)
            return@route
        }
        respondMixFile(call, shareInfo)
    }
}

suspend fun MixFileServer.respondMixFile(call: ApplicationCall, shareInfo: MixShareInfo) {
    val param = call.parameters

    val referer = param["referer"].ifNullOrBlank { shareInfo.referer }

    val name = param["name"].ifNullOrBlank { shareInfo.fileName }

    val mixFile = try {
        shareInfo.fetchMixFile(httpClient, referer)
    } catch (e: Exception) {
        call.respondText(
            "解析文件索引失败: ${e.stackTraceToString()}",
            status = HttpStatusCode.InternalServerError
        )
        return
    }

    var contentLength = shareInfo.fileSize
    val range: LongRange? = call.request.ranges()?.mergeToSingle(contentLength)
    call.response.apply {
        header(
            "Content-Disposition",
            "inline; filename=\"${name.encodeURL()}\""
        )
        header("x-mix-code", shareInfo.toString())
    }
    var fileList = mixFile.fileList.map { it to 0 }
    if (range != null) {
        fileList = mixFile.getFileListByStartRange(range.first)
        call.response.apply {
            header("Accept-Ranges", "bytes")
            status(HttpStatusCode.PartialContent)
            contentRange(range, mixFile.fileSize)
        }
        contentLength = mixFile.fileSize - range.first
    }
    call.respondBytesWriter(
        contentType = name.parseFileMimeType(),
        contentLength = contentLength
    ) {
        writeMixFileToByteChannel(
            shareInfo = shareInfo,
            mixFile = mixFile,
            fileList = fileList,
            referer = referer,
            channel = this
        )
    }

}

suspend fun MixFileServer.writeMixFileToByteChannel(
    shareInfo: MixShareInfo,
    mixFile: MixFile,
    fileList: List<Pair<String, Int>> = mixFile.fileList.map { it to 0 },
    referer: String = shareInfo.referer,
    channel: ByteWriteChannel,
) {
    coroutineScope {
        val chunkSizeMB = mixFile.chunkSize / 1.mb
        val taskCount = (downloadTaskCount / chunkSizeMB.coerceAtLeast(1)).coerceAtLeast(1)
        val fileListToWrite = fileList.toMutableList()
        val sortedTask = SortedTask(taskCount)
        val tasks = mutableListOf<Deferred<Unit>>()
        while (!channel.isClosedForWrite && fileListToWrite.isNotEmpty()) {
            val currentFile = fileListToWrite.removeAt(0)
            val taskOrder = -fileListToWrite.size
            sortedTask.prepareTask(taskOrder)
            tasks.add(async {
                val (url, range) = currentFile
                val dataBytes = try {
                    shareInfo.fetchFile(url, httpClient, referer)
                } catch (e: Exception) {
                    channel.close(e)
                    throw e
                }
                sortedTask.addTask(taskOrder) {
                    val buffer = ByteBuffer.wrap(dataBytes)

                    when {
                        range < 0 -> buffer.limit(dataBytes.size + range) //一般无 < 0 的情况
                        range > 0 -> buffer.position(range)
                    }

                    try {
                        channel.writeFully(buffer)
                        onDownloadData(dataBytes)
                    } catch (e: Exception) {
                        channel.close(e)
                        throw e
                    }
                }
                sortedTask.execute()
            })
        }
        tasks.awaitAll()
    }
}