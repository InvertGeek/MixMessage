package com.donut.mixfile.server.core.utils.basen

class LoopBaseN(override val alphabet: Alphabet) : BaseN() {

    override fun repackDigits(
        digits: IntArray,
        direction: CodecDirection,
        leadingZeros: Int,
        out: IntArray,
    ): Int {
        var startAt = leadingZeros
        var j = out.size
        var firstNonZero = j
        while (startAt < digits.size && leadingZeros < j) {
            val mod = direction.divmod(digits, startAt)
            if (digits[startAt] == 0) startAt++
            out[--j] = mod
            if (mod != 0) firstNonZero = j
        }
        return firstNonZero
    }
}