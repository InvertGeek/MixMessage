package com.donut.mixmessage.util.encode

import com.donut.mixmessage.util.common.encodeToBase64
import com.donut.mixmessage.util.common.hashSHA256
import okhttp3.internal.toLongOrDefault
import java.util.Date

class RoundKey(var key: PassKey) {

    var currentKey: String = ""
        private set
    var lastKey: String = ""
        private set
    var date: Date = Date()
        private set
    var name: String = ""
        private set
    var round: Long = 0
        private set
    var hour: Long = 1
        private set

    init {
        updateAttributes()
    }

    private fun updateAttributes() {
        val splits = key.value.split(":")
        val (_, name, last, current, date) = splits
        hour = splits[5].toLongOrDefault(hour).coerceAtLeast(1)
        round = splits[6].toLongOrDefault(0).coerceAtLeast(0)
        lastKey = last
        currentKey = current
        this.name = name
        this.date = checkDate(date)!!
    }

    fun getNextKeyHash(): String {
        return hashKey(lastKey + date.time)
    }

    fun getNextKeyStr(): String {
        round++
        return "_r:${name}:${currentKey}:${getNextKeyHash()}:${date.time / 1000}:$hour:${round}"
    }

    fun getSingleRoundTime() = hour * 1000 * 60 * 60

    fun getRoundTime() = round * getSingleRoundTime()

    fun getCurrentRoundTime() = Date(date.time + getRoundTime())

    fun getDecodeBeforeTime(): Date {
        return Date(getCurrentRoundTime().time - getSingleRoundTime() * 2)
    }

    fun getDecodeAfterTime(): Date {
        return Date(getCurrentRoundTime().time + getSingleRoundTime() * 2)
    }


    private fun performUpdateKey(round: Int = 0): Boolean {
        if (round >= 100) {
            return false
        }
        val diff = getTimeDiff()
        if (diff > 0) {
            key.updateValue(
                getNextKeyStr()
            )
            updateAttributes()
            performUpdateKey(round + 1)
            return true
        }
        return false
    }

    @Synchronized
    fun updateKey() {
        val passKey = getRoundKey(name) ?: return
        key.updateValue(passKey.value)
        updateAttributes()
        if (performUpdateKey()) {
            passKey.updateValue(key.value)
        }
    }

    fun getTimeDiff(): Long {
        val currentDate = Date()
        if (date.after(currentDate)) {
            return 0
        }
        val diff = (currentDate.time - date.time) / (1000 * 60 * 60 * hour)
        return diff - round
    }

    companion object {
        fun checkDate(date: String): Date? {
            if (date.toLongOrNull() == null) {
                return null
            }
            return Date(date.toLong() * 1000)
        }


        private fun hashKey(value: String) =
            value.hashSHA256().encodeToBase64()

    }
}