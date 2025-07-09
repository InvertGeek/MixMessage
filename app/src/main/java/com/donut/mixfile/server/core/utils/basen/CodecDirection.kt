package com.donut.mixfile.server.core.utils.basen

class CodecDirection(val fromBase: Int, val toBase: Int) {
    private val fromLog = logInt(fromBase)
    private val toLog = logInt(toBase)

    fun approximateSize(size: Int): Int = 1 + size * fromLog / toLog

    fun divmod(digits: IntArray, startAt: Int): Int {
        var remaining = 0
        for (i in startAt until digits.size) {
            val num = fromBase * remaining + digits[i]
            digits[i] = num / toBase
            remaining = num % toBase
        }
        return remaining
    }
}