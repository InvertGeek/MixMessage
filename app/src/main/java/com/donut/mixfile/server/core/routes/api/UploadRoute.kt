package com.donut.mixfile.server.core.routes.api

import com.donut.mixfile.server.core.MixFileServer
import com.donut.mixfile.server.core.Uploader
import com.donut.mixfile.server.core.aes.generateRandomByteArray
import com.donut.mixfile.server.core.objects.MixFile
import com.donut.mixfile.server.core.objects.MixShareInfo
import com.donut.mixfile.server.core.utils.MixUploadTask
import com.donut.mixfile.server.core.utils.extensions.mb
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.contentLength
import io.ktor.server.request.receiveChannel
import io.ktor.server.response.respondText
import io.ktor.server.routing.RoutingHandler
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.cancel
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.sync.Semaphore
import kotlinx.io.readByteArray
import kotlin.math.ceil
import kotlin.math.min


fun MixFileServer.getUploadRoute(): RoutingHandler {
    return route@{
        val name = call.parameters["name"]
        val add = call.parameters["add"] ?: "true"
        if (name.isNullOrEmpty()) {
            call.respondText("需要文件名称", status = HttpStatusCode.InternalServerError)
            return@route
        }

        val size = call.request.contentLength() ?: 0

        call.respondText(uploadFile(call.receiveChannel(), name, size, add.toBoolean()) {
            call.respondText("上传已取消", status = HttpStatusCode.InternalServerError)
        }.first)
    }
}

suspend fun MixFileServer.uploadFile(
    channel: ByteReadChannel,
    name: String,
    size: Long,
    add: Boolean = true,
    key: ByteArray = generateRandomByteArray(32),
    onStop: suspend () -> Unit = {}
): Pair<String, Long> {

    val uploadTask = getUploadTask(name, size, add)

    uploadTask.stopFunc.add(onStop)

    currentCoroutineContext().job.invokeOnCompletion {
        uploadTask.stop(it)
    }

    val uploader = getUploader()

    val head = uploader.genHead(httpClient) ?: genDefaultImage()

    val (mixUrl, fileSize) =
        doUploadFile(channel, head, uploader, key, fileSize = size, uploadTask)

    val mixShareInfo =
        MixShareInfo(
            fileName = name,
            fileSize = fileSize,
            headSize = head.size,
            url = mixUrl,
            key = MixShareInfo.ENCODER.encode(key),
            referer = uploader.referer
        )
    uploadTask.complete(mixShareInfo)
    return mixShareInfo.toString() to fileSize
}

private suspend fun MixFileServer.doUploadFile(
    channel: ByteReadChannel,
    head: ByteArray,
    uploader: Uploader,
    secret: ByteArray,
    fileSize: Long,
    uploadTask: MixUploadTask,
): Pair<String, Long> {

    val chunkSizeMB = chunkSize / 1.mb

    val semaphore = Semaphore((uploadTaskCount / chunkSizeMB.coerceAtLeast(1)).coerceAtLeast(1))

    return coroutineScope {

        uploadTask.stopFunc.add(0) {
            channel.cancel()
        }

        val fixedChunkSize = min(20.mb, chunkSize)

        val chunkCount = ceil(fileSize / fixedChunkSize.toDouble()).toInt()
        var uploadedChunkCount = 0
        val chunkList = mutableListOf<String>()

        var chunkIndex = 0

        var totalChunkSize = 0L

        val tasks = mutableListOf<Deferred<Unit>>()


        while (!channel.isClosedForRead) {
            semaphore.acquire()
            val chunkData = channel.readRemaining(fixedChunkSize.toLong()).readByteArray()
            val currentChunkSize = chunkData.size
            totalChunkSize += currentChunkSize
            val currentIndex = chunkIndex
            chunkList.add("")
            chunkIndex++
            tasks.add(async {
                try {
                    val url = uploader.upload(head, chunkData, secret, this@doUploadFile)
                    chunkList[currentIndex] = url
                    uploadedChunkCount++
                    uploadTask.updateProgress(currentChunkSize.toLong(), fileSize)
                } finally {
                    semaphore.release()
                }
            })
        }

        tasks.awaitAll()

        if (uploadedChunkCount < chunkCount) {
            throw Exception("上传失败")
        }

        val mixFile =
            MixFile(
                chunkSize = fixedChunkSize,
                version = 0,
                fileList = chunkList,
                fileSize = totalChunkSize
            )

        val mixFileData = mixFile.toBytes()
        val mixFileUrl =
            uploader.upload(head, mixFileData, secret, this@doUploadFile)

        return@coroutineScope mixFileUrl to totalChunkSize
    }
}