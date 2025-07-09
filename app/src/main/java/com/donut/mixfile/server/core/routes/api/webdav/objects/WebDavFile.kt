package com.donut.mixfile.server.core.routes.api.webdav.objects

import com.alibaba.fastjson2.annotation.JSONField
import com.donut.mixfile.server.core.objects.FileDataLog
import com.donut.mixfile.server.core.utils.hashSHA256
import com.donut.mixfile.server.core.utils.parseFileMimeType
import com.donut.mixfile.server.core.utils.sanitizeFileName
import com.donut.mixfile.server.core.utils.toHex
import io.ktor.http.encodeURLParameter
import io.ktor.http.encodeURLPath
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set


fun WebDavFile.toDataLog() = FileDataLog(shareInfoData, getName(), size)

// WebDAV 文件类，包含额外属性
data class WebDavFile(
    private var name: String,
    val size: Long = 0,
    val shareInfoData: String = "",
    val isFolder: Boolean = false,
    val files: ConcurrentHashMap<String, WebDavFile> = ConcurrentHashMap(),
    var lastModified: Long = System.currentTimeMillis()
) {

    init {
        sanitizeName()
    }

    fun setName(name: String) {
        this.name = name
        sanitizeName()
    }

    fun getName() = name

    fun sanitizeName() {
        name = name.trim().sanitizeFileName()
    }

    fun clone(): WebDavFile {

        val newFiles = ConcurrentHashMap<String, WebDavFile>()

        files.forEach { (key, file) ->
            newFiles[key] = file.clone()
        }

        return copy(files = newFiles)
    }

    fun addFile(file: WebDavFile) {
        if (!this.isFolder) {
            return
        }
        val existingFile = files[file.name]
        if (existingFile != null && existingFile.isFolder && file.isFolder) {
            file.files.forEach { (name, subFile) ->
                if (subFile.isFolder) {
                    existingFile.addFile(subFile)
                    return@forEach
                }
                existingFile.files[name] = subFile.clone()
            }
            return
        }
        files[file.name] = file.clone()
    }

    fun listFiles() = files.values.toList()


    @JSONField(serialize = false)
    fun getLastModifiedFormatted(): String {
        val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        return sdf.format(Date(lastModified))
    }


    fun toXML(path: String, isRoot: Boolean = false): String {
        val pathName = name.takeIf { !isRoot } ?: ""
        if (isFolder) {
            return xml("D:response") {
                "D:href" {
                    -"/${"$path/${pathName}".normalPath()}/".encodeURLPath(encodeEncoded = true)
                }
                "D:propstat" {
                    "D:prop" {
                        "D:displayname" {
                            -name.encodeURLParameter()
                        }
                        "D:resourcetype" {
                            "D:collection" {
                                attribute("xmlns:D", "DAV:")
                            }
                        }

                        "D:getlastmodified" {
                            -getLastModifiedFormatted()
                        }
                    }
                    "D:status" {
                        -"HTTP/1.1 200 OK"
                    }
                }
            }.toString()
        }
        return xml("D:response") {
            "D:href" {
                -"/${"$path/$name".normalPath()}".encodeURLPath(encodeEncoded = true)
            }
            "D:propstat" {
                "D:prop" {
                    "D:displayname" {
                        -name.encodeURLParameter()
                    }
                    "D:resourcetype" {

                    }
                    "D:getcontenttype" {
                        -name.parseFileMimeType().toString()
                    }
                    "D:getcontentlength" {
                        -size.toString()
                    }
                    "D:getetag" {
                        -shareInfoData.hashSHA256().toHex()
                    }
                    "D:getlastmodified" {
                        -getLastModifiedFormatted()
                    }
                }
                "D:status" {
                    -"HTTP/1.1 200 OK"
                }
            }
        }.toString()
    }

}