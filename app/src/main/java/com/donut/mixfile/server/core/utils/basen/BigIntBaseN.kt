package com.donut.mixfile.server.core.utils.basen

import java.math.BigInteger

class BigIntBaseN(override val alphabet: Alphabet) : BaseN() {

    override fun repackDigits(
        digits: IntArray, direction: CodecDirection, leadingZeros: Int,
        out: IntArray,
    ): Int {
        val fromBase = BigInteger.valueOf(direction.fromBase.toLong())
        val toBase = BigInteger.valueOf(direction.toBase.toLong())
        var acc = digits.slice(leadingZeros until digits.size)
            .fold(BigInteger.ZERO) { p, n -> p * fromBase + BigInteger.valueOf(n.toLong()) }
        var j = out.size
        var firstNonZero = j
        while (acc > BigInteger.ZERO) {
            val (newAcc, bigMod) = acc.divideAndRemainder(toBase)
            val mod = bigMod.toInt()
            acc = newAcc
            out[--j] = mod
            if (mod != 0) firstNonZero = j
        }
        return firstNonZero
    }
}