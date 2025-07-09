package com.donut.mixfile.server.core.routes.api.webdav.objects

import com.alibaba.fastjson2.into
import com.alibaba.fastjson2.toJSONString
import com.donut.mixfile.server.core.objects.FileDataLog
import com.donut.mixfile.server.core.utils.compressGzip
import com.donut.mixfile.server.core.utils.decompressGzip
import com.donut.mixfile.server.core.utils.resolveMixShareInfo
import java.util.concurrent.ConcurrentHashMap


open class WebDavManager {

    companion object {
        const val VERSION_PREFIX = "V2_:\n"
    }

    var WEBDAV_DATA = WebDavFile("root", isFolder = true)
    var loaded = true

    fun dataToBytes(data: WebDavFile = WEBDAV_DATA) =
        compressGzip(VERSION_PREFIX + data.toJSONString())

    fun loadDataFromBytes(data: ByteArray) {
        WEBDAV_DATA = parseDataFromBytes(data)
    }

    fun parseDataFromBytes(data: ByteArray): WebDavFile {
        val dataStr = decompressGzip(data)
        if (dataStr.startsWith(VERSION_PREFIX)) {
            return dataStr.substring(VERSION_PREFIX.length).into()
        }
        return loadLegacyData(dataStr)
    }

    fun importMixList(list: List<FileDataLog>, path: String = "") {
        list.forEach {
            addFileNode(path, WebDavFile(it.getCategory(), isFolder = true))
            addFileNode(
                "${path}/${it.getCategory()}".normalPath(),
                WebDavFile(
                    name = it.name,
                    shareInfoData = it.shareInfoData,
                    size = it.size
                )
            )
        }
    }


    private fun loadLegacyData(data: String): WebDavFile {
        val davData: ConcurrentHashMap<String, MutableSet<WebDavFile>> = data.into()
        val rootFile = WebDavFile("root", isFolder = true)
        davData.forEach { (path, fileList) ->
            if (path.isBlank()) return@forEach
            val pathSegments = path.split("/").filter { it.isNotEmpty() }
            var segmentFile = rootFile

            // 构建文件夹结构
            pathSegments.forEach { segment ->
                segmentFile = segmentFile.files.getOrPut(segment) {
                    WebDavFile(name = segment, isFolder = true)
                }
            }

            fileList.forEach {
                // 非文件夹则添加，添加文件夹可能覆盖已经有子files的文件夹
                if (!it.isFolder) {
                    segmentFile.files[it.getName()] = it
                }
            }

        }
        return rootFile
    }

    suspend fun saveData() {
        saveWebDavData(dataToBytes())
    }

    open suspend fun saveWebDavData(data: ByteArray) {}

    // 添加文件或目录到指定路径
    open fun addFileNode(path: String, file: WebDavFile): Boolean {
        val folder = getFile(path) ?: return false
        if (!folder.isFolder) {
            return false
        }
        folder.addFile(file)
        return true
    }

    open fun copyFile(
        path: String,
        dest: String,
        overwrite: Boolean,
        keep: Boolean = true
    ): Boolean {
        val srcFile = getFile(path) ?: return false
        val destFile = getFile(dest)
        if (!overwrite && destFile != null) {
            return false
        }

        val destName = dest.pathFileName()

        addFileNode(
            dest.parentPath(),
            srcFile.copy(
                name = destName,
                shareInfoData = srcFile.shareInfoData.let {
                    val shareInfo = resolveMixShareInfo(srcFile.shareInfoData)
                    shareInfo?.copy(fileName = destName)?.toString() ?: it
                })
        )
        if (!keep) {
            removeFileNode(path)
        }
        return true
    }

    // 删除指定路径的文件或目录
    open fun removeFileNode(path: String): WebDavFile? {
        val normalizedPath = normalizePath(path)
        val parentPath = normalizedPath.parentPath()
        val name = normalizedPath.pathFileName()
        val parentFolder = getFile(parentPath) ?: return null
        return parentFolder.files.remove(name)
    }


    open fun getFile(path: String): WebDavFile? {
        val normalizedPath = normalizePath(path)
        val pathSegments = normalizedPath.split("/").filter { it.isNotEmpty() }
        var file = WEBDAV_DATA
        for (segment in pathSegments) {
            val pathFile = file.files[segment] ?: return null
            file = pathFile
        }
        return file
    }

    // 列出指定路径下的文件和目录
    open fun listFiles(path: String): List<WebDavFile>? {
        val folder = getFile(path) ?: return null
        return folder.listFiles()
    }


}



