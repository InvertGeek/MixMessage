package com.donut.mixfile.server.core.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentSkipListMap

class SortedTask(limit: Int) {
    private val taskMap = ConcurrentSkipListMap<Int, (suspend () -> Unit)>()
    private val semaphore = Semaphore(limit.coerceAtLeast(1))
    private val placeholder = suspend {}
    private val lock = Mutex()

    suspend fun prepareTask(order: Int) {
        semaphore.acquire()
        taskMap[order] = placeholder
    }

    fun addTask(order: Int, task: suspend () -> Unit) {
        taskMap[order] = task
    }

    suspend fun execute() {
        lock.withLock {
            for (task in taskMap) {
                val block = task.value
                if (block === placeholder) {
                    return
                }
                taskMap.remove(task.key)
                try {
                    block()
                } finally {
                    semaphore.release()
                }
            }
        }
    }
}