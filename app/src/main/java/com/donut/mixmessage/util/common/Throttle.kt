package com.donut.mixmessage.util.common

import com.donut.mixmessage.appScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


object Throttler {
    private val lastExecutionTimeMap = mutableMapOf<String, Long>()

    fun throttle(key: String, wait: Long, block: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        val lastTime = lastExecutionTimeMap[key] ?: 0L

        if (currentTime - lastTime >= wait) {
            block()
            lastExecutionTimeMap[key] = currentTime
        }
    }
}

object Debouncer {
    private val jobMap = mutableMapOf<String, Job>()

    fun cancel(key: String) {
        jobMap[key]?.cancel()
    }

    fun debounce(key: String, wait: Long, block: () -> Unit) {
        cancel(key)
        val job = appScope.launch {
            delay(wait)
            block()
        }
        jobMap[key] = job
    }
}