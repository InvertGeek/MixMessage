package com.donut.mixmessage.util.encode.encoders

import com.donut.mixmessage.util.common.at
import com.donut.mixmessage.util.common.negativeIf
import com.donut.mixmessage.util.common.pow
import com.donut.mixmessage.util.encode.encoders.bean.CoderResult
import com.donut.mixmessage.util.encode.encoders.bean.TextCoder
import com.donut.mixmessage.util.encode.reverseTransformNumber
import com.donut.mixmessage.util.encode.transformNumber
import kotlin.math.abs
import kotlin.random.Random


class XorRandom(var seed: Long) {

    fun nextLong(max: Long = Long.MAX_VALUE): Long {
        seed = seed xor (seed shl 13)
        seed = seed xor (seed shr 17)
        seed = seed xor (seed shl 5)
        return abs(seed) % max
    }

}


class EncRandom(val password: String, seed: Long) {
    private val passSeedCache = genCache(seed)

    private fun genCache(start: Long = 0) = password.runningFold(XorRandom(start)) { acc, c ->
        XorRandom(XorRandom(acc.seed + c.code).nextLong())
    }

    private val passRandom = passSeedCache.last()

    fun nextInt(max: Long = Long.MAX_VALUE) =
        passSeedCache[passRandom.nextLong(passSeedCache.size.toLong()).toInt()].nextLong(max)
}

object ShiftEncoder : TextCoder {
    object Alphabet {
        private val legalChars = getAllLegalChar()
        private const val ENC_CHAR = '\u4E00'
        private val charToIndexMap = mutableMapOf<Char, Int>().apply {
            putAll(legalChars.mapIndexed { index, c -> c to index })
        }

        @JvmStatic
        fun getCharLength(): Int {
            return legalChars.size
        }

        fun getPrefix(): String {
            return "$ENC_CHAR$ENC_CHAR$ENC_CHAR$ENC_CHAR"
        }

        fun getPrefixLength(): Int {
            return getPrefix().length
        }

        fun getMaxShift(count: Int = 1): Long {
            return (getCharLength() - getEncCharIndex()).toLong().pow(count)
        }

        private fun getEncCharIndex(): Int {
            return getCharIndex(ENC_CHAR)
        }

        fun getCharByIndex(index: Long): Char {
            return legalChars.at(index)
        }

        fun getCharByIndex(index: Int): Char {
            return legalChars.at(index)
        }

        fun getCharIndex(c: Char): Int {
            return charToIndexMap[c] ?: -1
        }

        fun isLegalChar(c: Char): Boolean {
            return getCharIndex(c) != -1
        }

        private fun getAllLegalChar(): List<Char> {
            val list = mutableListOf<Char>()
            for (i in 0x4E00..0x9FA5) {
                list.add(i.toChar())
            }
            for (i in 'a'..'z') {
                list.add(i)
            }
            for (i in 'A'..'Z') {
                list.add(i)
            }
            for (i in '0'..'9') {
                list.add(i)
            }
//            list.addAll("|`~·!！@#$￥%^……&*()（）-=_+——[]【】\\|;:；：'\"“,<《.>》/?？、。".toList())
            list.add('|')
            return list.distinct()
        }

        fun getRandomChar(): Char {
            return legalChars[Random.nextInt(legalChars.size)]
        }
    }


    private fun moveStringEnc(
        source: String,
        iv: Long = 1,
        password: String,
        reverse: Boolean = false,
    ): String {

        val maxSeed = Alphabet.getMaxShift(2)

        val seed = abs(iv) % maxSeed

        val passRandom = EncRandom(password, seed)

        val seedTransformed =
            transformNumber(Alphabet.getCharLength(), seed, 2)

        return source.mapIndexed { index, c ->
            val charIndex = Alphabet.getCharIndex(c)
            //编码
            if (charIndex == -1) {
                return@mapIndexed c.toString()
            }
            val passSeed = passRandom.nextInt()

            val incShiftValue = (index / 2).negativeIf(reverse) * passSeed

            val transformValue = seedTransformed.getOrElse(index) { 0 }.negativeIf(reverse)

            val shiftedChar =
                Alphabet.getCharByIndex(
                    charIndex + incShiftValue + transformValue
                )
            shiftedChar
        }.joinToString("")
    }

    private fun moveEncText(text: String, password: String): String {
        return moveStringEnc(
            Alphabet.getPrefix() + text,
            Random.nextLong(),
            password
        )
    }

    private fun moveDecText(text: String, password: String): String {
        //check text length
        if (text.length <= Alphabet.getPrefixLength()) {
            return ""
        }

        //get random index
        val indexTransform =
            (0..<Alphabet.getPrefixLength()).map { Alphabet.getCharIndex(text[it]) }
        val index = reverseTransformNumber(Alphabet.getCharLength(), indexTransform)

        val decryptedPrefix =
            moveStringEnc(text.substring(0, Alphabet.getPrefixLength()), index, password, true)
        if (!decryptedPrefix.contentEquals(Alphabet.getPrefix())) {
            return ""
        }
        return moveStringEnc(text, index, password, true).substring(Alphabet.getPrefixLength())
    }

    override val name = "移位编码"

    private val replaceMap = mapOf(
        ' ' to '\u200b',
        '\n' to '\u200C',
        '\t' to '\u200D',
        '\r' to '\uFEFF',
    )

    override fun encode(input: String, password: String): CoderResult {
        if (input.isEmpty()) {
            return CoderResult.Failed
        }

        val encodeText = replaceMap.entries.fold(input) { acc, entry ->
            acc.replace(entry.key, entry.value)
        }
        val encodeResultText = moveEncText(encodeText, password.ifEmpty { "123" })
        return CoderResult(
            encodeResultText,
            password,
            this,
            input
        )
    }


    override fun decode(input: String, password: String): CoderResult {
        val decodeResultText = splitText(input).joinToString("\n") {
            val result = moveDecText(it, password.ifEmpty { "123" })

            replaceMap.entries.fold(result) { acc, entry ->
                acc.replace(entry.value, entry.key)
            }
        }.trim()

        if (decodeResultText.isEmpty()) {
            return CoderResult.failed(input)
        }

        return CoderResult(
            decodeResultText,
            password,
            this,
            input
        )
    }

}