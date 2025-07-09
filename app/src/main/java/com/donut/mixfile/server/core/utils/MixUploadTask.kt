package com.donut.mixfile.server.core.utils

import com.donut.mixfile.server.core.objects.MixShareInfo

interface MixUploadTask {
    var error: Throwable?
    var stopped: Boolean
    suspend fun complete(shareInfo: MixShareInfo)
    val stopFunc: MutableList<suspend () -> Unit>
    suspend fun updateProgress(size: Long, total: Long)
    fun stop(error: Throwable?) {
        stopped = true
        this.error = error
    }
}