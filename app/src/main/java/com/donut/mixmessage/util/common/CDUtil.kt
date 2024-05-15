package com.donut.mixmessage.util.common

private val CD_CACHE = mutableMapOf<String, Long>()


fun checkCd(key: String, cd: Long): Boolean {
    val lastTime = CD_CACHE[key] ?: 0
    val currentTime = System.currentTimeMillis()
    if (currentTime - lastTime > cd) {
        CD_CACHE[key] = currentTime
        return true
    }
    return false
}

inline fun withCd(key: String, cd: Long, block: () -> Unit) {
    if (checkCd(key, cd)) {
        block()
    }
}