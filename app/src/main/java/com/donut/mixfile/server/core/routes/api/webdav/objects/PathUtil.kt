package com.donut.mixfile.server.core.routes.api.webdav.objects

import com.donut.mixfile.server.core.utils.sanitizeFileName

fun String?.normalPath() = normalizePath(this ?: "")


fun normalizePath(path: String): String {
    if (path.isBlank()) return ""
    return path.trim('/').replace(Regex("/+"), "/")
}

fun String?.parentPath() = this.normalPath().substringBeforeLast("/", "")

fun String?.pathFileName() = this.normalPath().substringAfterLast("/").sanitizeFileName()