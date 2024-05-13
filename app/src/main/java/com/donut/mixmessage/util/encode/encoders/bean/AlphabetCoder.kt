package com.donut.mixmessage.util.encode.encoders.bean

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.donut.mixmessage.kv
import com.donut.mixmessage.util.encode.basen.Alphabet
import com.donut.mixmessage.util.encode.basen.BigIntBaseN
import com.donut.mixmessage.util.encode.encoders.ShiftEncoder
import com.donut.mixmessage.util.encode.xxtea.XXTEA


var USE_STRICT_ENCODE by mutableStateOf(kv.decodeBool("use_strict_encode", false))

fun setUseStrictEncode(value: Boolean) {
    USE_STRICT_ENCODE = value
    kv.encode("use_strict_encode", value)
}

abstract class AlphabetCoder(charList: List<Char>) : TextCoder {
    val alphabet: Alphabet = Alphabet.fromCharList(charList)

    val baseN = BigIntBaseN(alphabet)

    override fun encode(input: String, password: String): CoderResult {
        val encodeResultText: String = if (USE_STRICT_ENCODE) {
            baseN.encode(
                input.run { XXTEA.encrypt(this, password) }
            )
        } else {
            baseN.encode(
                input.run { ShiftEncoder.encode(this, password) }
                    .text.toByteArray(Charsets.UTF_8)
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
            val shiftDecodeResult = ShiftEncoder.decode(bytes.toString(Charsets.UTF_8), password)
            if (shiftDecodeResult.isFail && isStrict != false) {
                isStrict = true
                return XXTEA.decryptToString(bytes, password) ?: ""
            }
            if (isStrict == true) {
                return ""
            }
            isStrict = false
            return shiftDecodeResult.text
        }

//        val decodeResultText = filtered.fold("") { a, b -> a + decodeSecret(b) }
        val decodeResultText = filtered.joinToString("\n") { decodeSecret(it) }.trim()

        if (decodeResultText.trim().isEmpty()) {
            return CoderResult.Failed
        }

        return CoderResult(decodeResultText, password, this, input, isStrict = isStrict ?: false)
    }

    override fun checkText(input: String) = input.any { it in alphabet.key }
}