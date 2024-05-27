package com.donut.mixmessage.util.encode.encoders

import com.donut.mixmessage.util.common.negative
import kotlin.math.abs
import kotlin.random.Random

object ByteShiftEncoder {

    private val PREFIX = byteArrayOf(0, 127, 0, 127, 0)

    private fun moveByteEnc(
        source: ByteArray,
        count: Int = 1,
        password: String,
        reverse: Boolean = false,
    ): ByteArray {

        val seed = abs(count) % 256

        val fixedCount = if (reverse) -seed else seed

        val shiftRandom = XorRandom(seed)

        val passRandom = EncRandom(password)

        return source.mapIndexed { index, c ->
            val passSeed = passRandom.nextInt()
            val incShiftValue =
                (if (reverse) index.negative() else index) * (shiftRandom.nextInt() + passSeed)
            val shiftedByte = c + fixedCount + incShiftValue
            shiftedByte.toByte()
        }.toByteArray()
    }

    fun moveEncByte(data: ByteArray, password: String): ByteArray {
        return moveByteEnc(
            source = PREFIX + data,
            password = password,
            count = Random.nextInt()
        )
    }

    fun moveDecByte(data: ByteArray, password: String): ByteArray {
        if (data.size < PREFIX.size + 1) {
            return byteArrayOf()
        }

        //get random index
        val index = data.first().toUByte().toInt()
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
            count = index,
            source = data,
            password = password,
            reverse = true
        ).copyOfRange(PREFIX.size, data.size)
    }

}