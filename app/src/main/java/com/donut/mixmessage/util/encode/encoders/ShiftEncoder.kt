package com.donut.mixmessage.util.encode.encoders

import com.donut.mixmessage.util.common.at
import com.donut.mixmessage.util.common.negative
import com.donut.mixmessage.util.encode.encoders.bean.CoderResult
import com.donut.mixmessage.util.encode.encoders.bean.TextCoder
import kotlin.math.abs
import kotlin.random.Random


class XorRandom(var seed: Int) {

    fun nextInt(max: Int = Int.MAX_VALUE): Int {
        seed = seed xor (seed shl 13)
        seed = seed xor (seed shr 17)
        seed = seed xor (seed shl 5)
        return abs(seed) % max
    }

}


class EncRandom(val password: String) {
    private val passSeedCache = genCache(genCache().last().nextInt())

    private fun genCache(start: Int = 0) = password.runningFold(XorRandom(start)) { acc, c ->
        XorRandom(XorRandom(acc.seed + c.code).nextInt())
    }

    private val passRandom = passSeedCache.last()

    fun nextInt(max: Int = Int.MAX_VALUE) =
        passSeedCache[passRandom.nextInt(passSeedCache.size)].nextInt(max)
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
            return "$ENC_CHAR|$ENC_CHAR"
        }

        fun getPrefixLength(): Int {
            return getPrefix().length
        }

        fun getMaxShift(): Int {
            return getCharLength() - getEncCharIndex()
        }

        fun getEncCharIndex(): Int {
            return getCharIndex(ENC_CHAR)
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
        count: Int = 1,
        password: String,
        reverse: Boolean = false,
    ): String {
        // 不能超过最大偏移,否则解密时无法利用第一个字符识别本次加密的偏移量
        val seed = abs(count) % Alphabet.getMaxShift()

        val fixedCount = if (reverse) -seed else seed

        val shiftRandom = XorRandom(seed)
        val passRandom = EncRandom(password)

        return source.mapIndexed { index, c ->
            val charIndex = Alphabet.getCharIndex(c)
            //编码
            if (charIndex == -1) {
                return@mapIndexed c.toString()
            }
            val passSeed = passRandom.nextInt()

            val incShiftValue =
                (if (reverse) index.negative() else (index)) * (shiftRandom.nextInt() + passSeed)

            val shiftedChar =
                Alphabet.getCharByIndex(
                    charIndex + fixedCount + incShiftValue
                )
            shiftedChar.toString()
        }.joinToString("")
    }

    private fun moveEncText(text: String, password: String): String {
        return moveStringEnc(
            Alphabet.getPrefix() + text,
            1,
            password
        )
    }

    private fun moveDecText(text: String, password: String): String {
        //check text length
        if (text.length < Alphabet.getPrefixLength() + 1) {
            return ""
        }

        //get random index
        val index = Alphabet.getCharIndex(text[0]) - Alphabet.getEncCharIndex()
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
        val encodeResultText = moveEncText(encodeText, password.ifEmpty { "123" });
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
            return CoderResult.Failed
        }

        return CoderResult(
            decodeResultText,
            password,
            this,
            input
        )
    }

}