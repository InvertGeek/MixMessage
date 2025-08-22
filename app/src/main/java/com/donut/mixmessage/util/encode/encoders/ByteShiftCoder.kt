package com.donut.mixmessage.util.encode.encoders

import com.donut.mixmessage.util.common.negativeIf
import com.donut.mixmessage.util.encode.reverseTransformNumber
import com.donut.mixmessage.util.encode.transformNumber
import kotlin.math.abs
import kotlin.random.Random

@Deprecated("deprecated")
object ByteShiftEncoder {

    private val PREFIX = byteArrayOf(*((1..8).map { (0).toByte() }.toByteArray()))

    private fun moveByteEnc(
        source: ByteArray,
        iv: Long = 1,
        password: String,
        reverse: Boolean = false,
    ): ByteArray {

        val seed = abs(iv) % Int.MAX_VALUE

        val passRandom = EncRandom(password, seed)

        val seedTransformed = transformNumber(256, seed, 4)

        return source.mapIndexed { index, c ->
            val passSeed = passRandom.nextInt()
            val incShiftValue = (index / 4).negativeIf(reverse) * passSeed

            val transformValue = seedTransformed.getOrElse(index) { 0 }.negativeIf(reverse)

            val shiftedByte = c + incShiftValue + transformValue
            shiftedByte.toByte()
        }.toByteArray()
    }

    fun moveEncByte(data: ByteArray, password: String): ByteArray {
        return moveByteEnc(
            source = PREFIX + data,
            password = password,
            iv = Random.nextLong()
        )
    }

    fun moveDecByte(data: ByteArray, password: String): ByteArray {
        if (data.size <= PREFIX.size) {
            return byteArrayOf()
        }

        val indexTransform =
            (0..<4).map { data[it].toUByte().toInt() }
        val index = reverseTransformNumber(256, indexTransform)
        val decryptedPrefix =
            moveByteEnc(
                data.copyOfRange(0, PREFIX.size),
                index,
                reverse = true,
                password = password
            )

        if (!decryptedPrefix.contentEquals(PREFIX)) {
            return byteArrayOf()
        }
        return moveByteEnc(
            iv = index,
            source = data,
            password = password,
            reverse = true
        ).copyOfRange(PREFIX.size, data.size)
    }

}