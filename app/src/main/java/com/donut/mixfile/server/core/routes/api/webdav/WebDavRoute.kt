package com.donut.mixfile.server.core.routes.api.webdav

import com.alibaba.fastjson2.into
import com.donut.mixfile.server.core.MixFileServer
import com.donut.mixfile.server.core.interceptCall
import com.donut.mixfile.server.core.objects.FileDataLog
import com.donut.mixfile.server.core.objects.MixShareInfo
import com.donut.mixfile.server.core.routes.api.respondMixFile
import com.donut.mixfile.server.core.routes.api.uploadFile
import com.donut.mixfile.server.core.routes.api.webdav.objects.WebDavFile
import com.donut.mixfile.server.core.routes.api.webdav.objects.WebDavManager
import com.donut.mixfile.server.core.routes.api.webdav.objects.normalPath
import com.donut.mixfile.server.core.routes.api.webdav.objects.parentPath
import com.donut.mixfile.server.core.routes.api.webdav.objects.pathFileName
import com.donut.mixfile.server.core.utils.decompressGzip
import com.donut.mixfile.server.core.utils.extensions.decodedPath
import com.donut.mixfile.server.core.utils.extensions.mb
import com.donut.mixfile.server.core.utils.extensions.paramPath
import com.donut.mixfile.server.core.utils.extensions.routePrefix
import com.donut.mixfile.server.core.utils.getHeader
import com.donut.mixfile.server.core.utils.resolveMixShareInfo
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.decodeURLQueryComponent
import io.ktor.http.encodeURLParameter
import io.ktor.http.withCharset
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.contentLength
import io.ktor.server.request.receiveChannel
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.RoutingHandler
import io.ktor.server.routing.method
import io.ktor.server.routing.route
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray


suspend fun RoutingContext.receiveBytes(limit: Int) =
    call.receiveChannel().readRemaining(limit.toLong()).readByteArray()


val RoutingContext.davPath: String
    get() = paramPath

val RoutingContext.davParentPath: String
    get() = davPath.parentPath()

val RoutingContext.davFileName: String
    get() = davPath.pathFileName()

val RoutingContext.davShareInfo: MixShareInfo?
    get() = resolveMixShareInfo(
        davPath.substringAfterLast(
            "/"
        )
    )


suspend fun RoutingContext.handleCopy(keep: Boolean, webDavManager: WebDavManager) {
    val overwrite = getHeader("overwrite").contentEquals("T")
    val destination = getHeader("destination")?.decodeURLQueryComponent().let {
        it?.substringAfter(routePrefix).normalPath()
    }
    if (destination.isBlank()) {
        call.respond(HttpStatusCode.BadRequest)
        return
    }
    val moved = webDavManager.copyFile(davPath, destination, overwrite, keep)
    if (!moved) {
        call.respond(HttpStatusCode.PreconditionFailed)
        return
    }
    call.respond(HttpStatusCode.Created)
    webDavManager.saveData()
}

fun MixFileServer.getWebDAVRoute(): Route.() -> Unit {
    return {
        interceptCall({
            if (!webDav.loaded) {
                it.respond(HttpStatusCode.ServiceUnavailable, "WebDav is Loading")
            }
        })
        webdav("OPTIONS") {
            call.response.apply {
                header("Allow", "OPTIONS, DELETE, COPY, MOVE, PROPFIND")
                header("Dav", "1")
                header("Ms-Author-Via", "DAV")
            }
            call.respond(HttpStatusCode.OK)
        }
        webdav("GET") {
            if (davFileName.contentEquals("当前目录存档.mix_dav")) {
                val file = webDav.getFile(davParentPath)
                if (file == null) {
                    call.respond(HttpStatusCode.NotFound)
                    return@webdav
                }
                val data = webDav.dataToBytes(file.copy(name = "root"))
                val fileName = "${davParentPath.ifEmpty { "root" }}.mix_dav".encodeURLParameter()
                call.response.apply {
                    header("Cache-Control", "no-cache, no-store, must-revalidate")
                    header("Pragma", "no-cache")
                    header("Expires", "0")
                    header(
                        "Content-Disposition",
                        "attachment;filename=\"$fileName\""
                    )
                }
                call.respondBytes(data, ContentType.Application.OctetStream)
                return@webdav
            }
            val fileNode = webDav.getFile(davPath)
            if (fileNode == null) {
                call.respond(HttpStatusCode.NotFound)
                return@webdav
            }
            val shareInfo = resolveMixShareInfo(fileNode.shareInfoData)
            if (shareInfo == null) {
                call.respond(HttpStatusCode.Conflict)
                return@webdav
            }
            respondMixFile(call, shareInfo)
        }
        webdav("MOVE") {
            handleCopy(false, webDav)
        }
        webdav("COPY") {
            handleCopy(true, webDav)
        }
        webdav("PUT") {
            val fileSize = call.request.contentLength() ?: 0
            if (fileSize > 0 && fileSize < 50.mb) {
                if (davFileName.endsWith(".mix_dav")) {
                    val davData =
                        webDav.parseDataFromBytes(receiveBytes(50.mb))
                    val parentFile = webDav.getFile(davParentPath)
                    if (parentFile == null || !parentFile.isFolder) {
                        call.respond(HttpStatusCode.Conflict)
                        return@webdav
                    }
                    davData.files.forEach {
                        parentFile.addFile(it.value)
                    }
                    call.respond(HttpStatusCode.Created)
                    webDav.saveData()
                    return@webdav
                }
                if (davFileName.endsWith(".mix_list")) {
                    val dataLogList = decompressGzip(
                        receiveBytes(50.mb)
                    ).into<List<FileDataLog>>()
                    webDav.importMixList(dataLogList, davParentPath)
                    call.respond(HttpStatusCode.Created)
                    webDav.saveData()
                    return@webdav
                }
            }

            val fileList = webDav.listFiles(davParentPath)
            if (fileList == null) {
                call.respond(HttpStatusCode.Conflict)
                return@webdav
            }
            val (shareInfo, finalSize) = uploadFile(
                call.receiveChannel(),
                davFileName,
                fileSize,
                add = false
            )
            val fileNode =
                WebDavFile(size = finalSize, shareInfoData = shareInfo, name = davFileName)
            webDav.addFileNode(davParentPath, fileNode)
            call.respond(HttpStatusCode.Created)
            webDav.saveData()
        }
        webdav("DELETE") {
            webDav.removeFileNode(davPath)
            call.respond(HttpStatusCode.NoContent)
            webDav.saveData()
        }
        webdav("MKCOL") {
            if (davPath.isEmpty()) {
                call.respond(HttpStatusCode.Created)
                return@webdav
            }
            val fileList = webDav.listFiles(davParentPath)
            if (fileList == null) {
                call.respond(HttpStatusCode.Conflict)
                return@webdav
            }
            val shareInfo = davShareInfo
            if (shareInfo != null) {
                val node = WebDavFile(
                    name = shareInfo.fileName,
                    size = shareInfo.fileSize,
                    shareInfoData = shareInfo.toString()
                )
                webDav.addFileNode(davParentPath, node)
                call.respond(HttpStatusCode.Created)
                webDav.saveData()
                return@webdav
            }
            val node = WebDavFile(isFolder = true, name = davFileName)
            webDav.addFileNode(davParentPath, node)
            call.respond(HttpStatusCode.Created)
            webDav.saveData()
        }
        propfind {
            val depth = getHeader("depth")?.toInt() ?: 0
            val file = webDav.getFile(davPath)
            if (depth == 0) {
                respondRootFile(file)
                return@propfind
            }
            val fileList = webDav.listFiles(davPath)
            if (fileList == null) {
                respondRootFile(file)
                return@propfind
            }
            val xmlFileList = fileList.toMutableList().apply {
                if (isNotEmpty()) {
                    add(WebDavFile("当前目录存档.mix_dav", isFolder = false))
                }
            }.joinToString(separator = "") {
                it.toXML(decodedPath)
            }
            val rootFile = webDav.getFile(davPath) ?: WebDavFile("root", isFolder = true)
            val text = """
                <D:multistatus xmlns:D="DAV:">
                ${rootFile.toXML(decodedPath, true)}
                $xmlFileList
                </D:multistatus>
                """
            call.respondXml(text)
        }
    }
}

suspend fun ApplicationCall.respondXml(xml: String) {
    respondText(
        contentType = ContentType.Text.Xml.withCharset(Charsets.UTF_8),
        status = HttpStatusCode.MultiStatus,
        text = compressXml(
            """<?xml version="1.0" encoding="UTF-8"?>$xml"""
        )
    )
}

suspend fun RoutingContext.respondRootFile(file: WebDavFile?) {
    if (file == null) {
        call.respond(HttpStatusCode.NotFound)
        return
    }
    val text = """
                <D:multistatus xmlns:D="DAV:">
                ${file.toXML(decodedPath, true)}
                </D:multistatus>
                """
    call.respondXml(text)
}

fun compressXml(xmlString: String): String {
    // 1. 移除换行符和多余的空白字符
    var compressed = xmlString.replace("\\s+".toRegex(), " ")

    // 2. 去除标签之间的多余空格
    compressed = compressed.replace("> <", "><").trim()

    return compressed
}

fun Route.webdav(method: String, handler: RoutingHandler) {
    route("/{path...}") {
        method(HttpMethod(method)) {
            handle(handler)
        }
    }
}

fun Route.propfind(handler: RoutingHandler) = webdav("PROPFIND", handler)

