package com.donut.mixmessage

import com.donut.mixmessage.util.common.toHex
import com.donut.mixmessage.util.encode.basen.Alphabet
import com.donut.mixmessage.util.encode.basen.BigIntBaseN
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config


@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ExampleUnitTest {

    private val encodeMap = run {
        val map = mutableMapOf<String, String>()
        for (value in 0xfe00..0xfe0f) {
            val key = (value - 0xfe00).toString(16)
            map[key] = "${value.toChar()}"
        }
        map
    }

    fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }

        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    fun encodeHex(data: String): String {
        val sb = StringBuilder()
        for (element in data.toByteArray().toHex()) {
            if (encodeMap.containsKey(element.toString())) {
                sb.append(encodeMap[element.toString()])
            }
        }
        return sb.toString()
    }

    fun encodeHex(data: ByteArray): String {
        val sb = StringBuilder()
        for (element in data.toHex()) {
            if (encodeMap.containsKey(element.toString())) {
                sb.append(encodeMap[element.toString()])
            }
        }
        return sb.toString()
    }

    fun decodeHex(data: String): String {
        val sb = StringBuilder()
        for (element in data) {
            if (encodeMap.containsValue(element.toString())) {
                sb.append(encodeMap.filterValues { it == element.toString() }.keys.first())
            }
        }
        return sb.toString()
    }


    @Test
    fun test() {
        val alphabet: Alphabet = Alphabet.fromCharList((0xfe00..0xfe0f).map { it.toChar() })
        val baseN = BigIntBaseN(alphabet)
        val alphabet2: Alphabet = Alphabet.fromCharList(('0'..'9') + ('a'..'f'))
        val baseN2 = BigIntBaseN(alphabet2)
        println(baseN.decode(encodeHex("0009".decodeHex())).toHex())
        println(
            baseN.decode("︀︀︀︉︌︀︁︁︀︁︌︀︅︊️︄︆︋︇︎︃︂︀︉︄︆︁︂︉︌︃︍︉︅︋︀︉︌︁️︆︍︀︂︌︉︀︉︆︂︈︃︅︋︌︉︁️︂︇︅︄︎︋︋︁︍︀︉︉︂︄︍︃︉︆︊︎︍︊︁︆︀︇︎︋︆︈︇︃︆︉︇︎︅︆︊︃︉︌︉︄︀︁︀︅️︂")
                .toHex()
        )
        println(baseN2.encode("000009".decodeHex()))
    }


}