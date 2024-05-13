package com.donut.mixmessage.util.encode.encoders

import com.donut.mixmessage.util.encode.encoders.bean.CoderResult
import com.donut.mixmessage.util.encode.encoders.bean.TextCoder
import kotlin.math.abs
import kotlin.random.Random

object ShiftEncoder : TextCoder {
    object Alphabet {
        private val legalChars = getAllLegalChar()
        private const val encChar = '\u4E00'
        private val charToIndexMap = mutableMapOf<Char, Int>().apply {
            putAll(legalChars.mapIndexed { index, c -> c to index })
        }

        @JvmStatic
        fun getCharLength(): Int {
            return legalChars.size
        }

        fun getPrefix(): String {
            return "$encChar|$encChar"
        }

        fun getPrefixLength(): Int {
            return getPrefix().length
        }

        fun getMaxShift(): Int {
            return getCharLength() - getEncCharIndex() - 1
        }

        fun getEncCharIndex(): Int {
            return getCharIndex(encChar)
        }

        fun getCharByIndex(index: Int): Char {
            return legalChars[index.run {
                val fixedIndex = this % (legalChars.size)
                if (fixedIndex < 0) {
                    return@run fixedIndex + (legalChars.size)
                }
                fixedIndex
            }]
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

    class XorRandom(seed: Int) {
        private var x = seed


        fun nextInt(max: Int = Int.MAX_VALUE): Int {
            x = x xor (x shl 13)
            x = x xor (x shr 17)
            x = x xor (x shl 5)
            return abs(x) % max
        }
    }


    private fun moveStringEnc(
        source: String,
        count: Int = 1,
        password: String,
        reverse: Boolean = false,
    ): String {

        val seed = abs(count).coerceAtMost(Alphabet.getMaxShift())

        val fixedCount = if (reverse) -seed else seed
        val passSeedCache = password.runningFold(0) { acc, c ->
            XorRandom(acc + c.code).nextInt()
        }
        val shiftRandom = XorRandom(seed)
        val passRandom = XorRandom(passSeedCache.last())
        return source.mapIndexed { index, c ->
            val charIndex = Alphabet.getCharIndex(c)
            //编码
            if (charIndex == -1) {
                return@mapIndexed c.toString()
            }
//            println("total $passTotalSeed")
            val passSeed = passSeedCache[passRandom.nextInt(passSeedCache.size)]
            val incShiftValue =
                ((if (reverse) -index else index)) * (shiftRandom.nextInt() + passSeed)
            val shiftedChar =
                Alphabet.getCharByIndex(charIndex + fixedCount + incShiftValue)
            shiftedChar.toString()
        }.joinToString("")
    }

    private fun moveEncText(text: String, password: String): String {
        return moveStringEnc(
            Alphabet.getPrefix() + text,
            Random.nextInt(Alphabet.getMaxShift()),
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
        val decrypted = moveStringEnc(text, index, password, true)
        if (!decrypted.startsWith(Alphabet.getPrefix())) {
            return ""
        }
        return decrypted.substring(Alphabet.getPrefixLength())
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