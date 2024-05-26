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


//var USE_STRICT_ENCODE by mutableStateOf(kv.decodeBool("use_strict_encode", false))
var USE_STRICT_ENCODE by cachedMutableOf(false, "use_strict_encode")

fun setUseStrictEncode(value: Boolean) {
    USE_STRICT_ENCODE = value
}

abstract class AlphabetCoder(charList: List<Char>) : TextCoder {
    private val alphabet: Alphabet = Alphabet.fromCharList(charList)

    private val baseN = BigIntBaseN(alphabet)

    override fun encode(input: String, password: String): CoderResult {
        input.trim().isEmpty().isTrue {
            return CoderResult.Failed
        }
        val encodeResultText: String = if (USE_STRICT_ENCODE) {
            baseN.encode(
                encryptAES(input, password)
            )
        } else {
            baseN.encode(
//                ShiftEncoder.encode(input, password)
//                    .text.toByteArray()
                ByteShiftEncoder.moveEncByte(input.toByteArray(), password)
            )
        }
        if (encodeResultText.trim().isEmpty()) {
            return CoderResult.Failed
        }
        return CoderResult(encodeResultText, password, this, input, isStrict = USE_STRICT_ENCODE)
    }

    override fun decode(input: String, password: String): CoderResult {
        //按照不在alphabet中的分割
        val filtered = input.split(Regex("[^${alphabet.key}]+")).filter { it.isNotEmpty() }

        if (filtered.isEmpty()) {
            return CoderResult.Failed
        }

        var isStrict: Boolean? = null

        fun decodeSecret(value: String): String {
            val bytes = baseN.decode(value)
//            val shiftDecodeResult = ShiftEncoder.decode(bytes.decodeToString(), password)
            val shiftDecodeResult = ByteShiftEncoder.moveDecByte(bytes, password)
            shiftDecodeResult.isEmpty().isTrueAnd(isStrict.isNotFalse()) {
                isStrict = true
                return decryptAES(bytes, password).decodeToString()
            }
            isStrict.isTrue {
                return ""
            }
            isStrict = false
            return shiftDecodeResult.decodeToString()
        }

        val decodeResultText = filtered.joinToString("\n") { decodeSecret(it) }.trim()

        if (decodeResultText.trim().isEmpty()) {
            return CoderResult.Failed
        }

        return CoderResult(decodeResultText, password, this, input, isStrict = isStrict ?: false)
    }

    override fun checkText(input: String) = input.any { it in alphabet.key }
}