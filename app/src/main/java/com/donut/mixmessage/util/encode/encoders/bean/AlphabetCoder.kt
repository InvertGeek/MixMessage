package com.donut.mixmessage.util.encode.encoders.bean

import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.isNotFalse
import com.donut.mixmessage.util.common.isTrue
import com.donut.mixmessage.util.common.isTrueAnd
import com.donut.mixmessage.util.encode.basen.Alphabet
import com.donut.mixmessage.util.encode.basen.BigIntBaseN
import com.donut.mixmessage.util.encode.decryptAES
import com.donut.mixmessage.util.encode.encoders.ByteShiftEncoder
import com.donut.mixmessage.util.encode.encryptAES


var USE_SIMPLE_MODE by cachedMutableOf(false, "use_simple_mode")

fun setUseStrictEncode(value: Boolean) {
    USE_SIMPLE_MODE = value
}

abstract class AlphabetCoder(charList: List<Char>) : TextCoder {
    private val alphabet: Alphabet = Alphabet.fromCharList(charList)

    private val baseN = BigIntBaseN(alphabet)

    fun encodeRaw(data: ByteArray) = baseN.encode(data)

    override fun encode(input: String, password: String): CoderResult {
        input.trim().isEmpty().isTrue {
            return CoderResult.Failed
        }
        val encodeResultText: String = if (!USE_SIMPLE_MODE) {
            baseN.encode(
                encryptAES(input, password)
            )
        } else {
            baseN.encode(
                ByteShiftEncoder.moveEncByte(input.toByteArray(), password)
            )
        }
        if (encodeResultText.trim().isEmpty()) {
            return CoderResult.Failed
        }
        return CoderResult(encodeResultText, password, this, input, isSimple = USE_SIMPLE_MODE)
    }

    private val regex by lazy {
        Regex(
            "[^${
                alphabet.key.toCharArray().joinToString("") { Regex.escape(it.toString()) }
            }]+"
        )
    }

    override fun decode(input: String, password: String): CoderResult {
        //按照不在alphabet中的分割
        val filtered = input.split(regex).filter { it.isNotEmpty() }

        if (filtered.isEmpty()) {
            return CoderResult.failed(input)
        }

        var isSimple: Boolean? = null

        fun decodeSecret(value: String): String {
            val bytes = baseN.decode(value)
            val shiftDecodeResult = ByteShiftEncoder.moveDecByte(bytes, password)
            shiftDecodeResult.isEmpty().isTrueAnd(isSimple.isNotFalse()) {
                isSimple = false
                return decryptAES(bytes, password).decodeToString()
            }
            isSimple.isTrue {
                return ""
            }
            isSimple = true
            return shiftDecodeResult.decodeToString()
        }

        val decodeResultText = filtered.joinToString("\n") { decodeSecret(it) }.trim()

        if (decodeResultText.trim().isEmpty()) {
            return CoderResult.failed(input)
        }

        return CoderResult(decodeResultText, password, this, input, isSimple = isSimple.isTrue())
    }

    override fun checkText(input: String) = input.any { it in alphabet.key }
}