package com.donut.mixmessage.util.encode.encoders.bean

import com.donut.mixmessage.util.common.isTrue
import com.donut.mixmessage.util.encode.basen.Alphabet
import com.donut.mixmessage.util.encode.basen.BigIntBaseN
import com.donut.mixmessage.util.encode.decryptAES
import com.donut.mixmessage.util.encode.encryptAES


abstract class AlphabetCoder(charList: List<Char>) : TextCoder {
    private val alphabet: Alphabet = Alphabet.fromCharList(charList)

    private val baseN = BigIntBaseN(alphabet)

    fun encodeRaw(data: ByteArray) = baseN.encode(data)

    fun decodeRaw(data: String) = baseN.decode(data)

    override fun encode(input: String, password: String): CoderResult {
        input.trim().isEmpty().isTrue {
            return CoderResult.Failed
        }
        val encodeResultText: String = baseN.encode(
            encryptAES(input, password)
        )
        if (encodeResultText.trim().isEmpty()) {
            return CoderResult.Failed
        }
        return CoderResult(encodeResultText, password, this, input)
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

        val decodeResultText = filtered.joinToString("\n") {
            val bytes = baseN.decode(it)
            decryptAES(bytes, password).decodeToString()
        }.trim()

        if (decodeResultText.trim().isEmpty()) {
            return CoderResult.failed(input)
        }

        return CoderResult(decodeResultText, password, this, input)
    }

    override fun checkText(input: String) = input.any { it in alphabet.key }
}