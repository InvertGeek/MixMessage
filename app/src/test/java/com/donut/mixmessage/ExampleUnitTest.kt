package com.donut.mixmessage

import com.donut.mixmessage.util.encode.decryptAES
import com.donut.mixmessage.util.encode.encryptAES
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random


@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ExampleUnitTest {

    private fun generateRandomByteArray(size: Int): ByteArray {
        val byteArray = ByteArray(size)
        Random.nextBytes(byteArray)
        return byteArray
    }


    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun addition_isCorrect() {
//        val enc = ShiftEncoder.encode("哈哈哈哈哈哈哈哈哈哈哈哈").text
//        println(enc)
//        val dec = ShiftEncoder.decode(enc).text
//        println(dec)
//        repeat(100000000) {
//            val random = (1..10).map { ShiftEncoder.Alphabet.getRandomChar() }.joinToString("")
//            val result = ShiftEncoder.decode(random).text
//            if (result.isNotEmpty()) {
//                println(result)
//            }
//        }


//        val bytes = generateRandomByteArray(4)
//        println(bytes.toList())
//        val enc = ByteShiftEncoder.moveEncByte(bytes, "123")
//        println(enc.toList())
//        println(ByteShiftEncoder.moveDecByte(enc, "123").toList())

        val bytes = generateRandomByteArray(4)
        println(bytes.toList())
        val enc = encryptAES(bytes,"123")
        println(enc.toList())
        println(decryptAES(enc,"123").toList())


    }
}