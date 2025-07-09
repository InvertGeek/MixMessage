package com.donut.mixfile.server.core.utils.basen

import java.security.MessageDigest
import kotlin.math.ln


val ALPHABETS = mapOf(
    2 to "01",
    8 to "01234567",
    11 to "0123456789a",
    16 to "0123456789abcdef",
    32 to "0123456789ABCDEFGHJKMNPQRSTVWXYZ",
    36 to "0123456789abcdefghijklmnopqrstuvwxyz",
    58 to "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz",
    62 to "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ",
    64 to "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/",
    67 to "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.!~"
)

private fun sha256x2(input: ByteArray): ByteArray = sha256(sha256(input))

private fun sha256(input: ByteArray) = MessageDigest.getInstance("SHA-256").digest(input)

fun logInt(v: Int): Int = (ln(v.toDouble()) * 10000).toInt()

fun genChineseAlphabet(): Alphabet {
    val list = mutableListOf<Char>()
    for (i in 0x4E00..0x9FA5) {
        list.add(i.toChar())
    }
    return Alphabet.fromCharList(list)
}


tailrec fun countLeadingZeros(digits: IntArray, z: Int = 0): Int {
    return if (z < digits.size && digits[z] == 0) countLeadingZeros(digits, z + 1) else z
}

abstract class BaseN {
    abstract val alphabet: Alphabet

    fun encode(bytes: ByteArray): String = alphabet.toChars(
        codeDigits(IntArray(bytes.size) { i -> bytes[i].toInt() and 0xFF }, alphabet.encoder)
    )

    fun decode(text: String): ByteArray =
        codeDigits(alphabet.toDigits(text), alphabet.decoder).let {
            ByteArray(it.size) { i -> it[i].toByte() }
        }

    private fun codeDigits(digits: IntArray, direction: CodecDirection): IntArray {
        if (digits.isEmpty()) return IntArray(0)
        val leadingZeros = countLeadingZeros(digits)
        val codeSize = direction.approximateSize(digits.size - leadingZeros)
        val out = IntArray(leadingZeros + codeSize)
        val firstNonZero = repackDigits(digits, direction, leadingZeros, out)
        return if (firstNonZero == leadingZeros) out else out.copyOfRange(
            firstNonZero - leadingZeros,
            out.size
        )
    }

    abstract fun repackDigits(
        digits: IntArray,
        direction: CodecDirection,
        leadingZeros: Int,
        out: IntArray,
    ): Int

    fun encodeCheck(bytes: ByteArray): String {
        val buffer = ByteArray(bytes.size + 4)
        bytes.copyInto(buffer)
        val checksum = sha256x2(bytes)
        checksum.copyInto(buffer, bytes.size, endIndex = 4)
        return encode(buffer)
    }

    fun decodeCheck(code: String): ByteArray {
        val bytes = decode(code)
        val payloadEnd = bytes.size - 4
        val buffer = ByteArray(payloadEnd) { i -> bytes[i] }
        val checksum = sha256x2(buffer).slice(0..3)
        if (checksum != bytes.slice(payloadEnd until bytes.size)) {
            throw IllegalStateException("Checksum does not match")
        }
        return buffer
    }

}


