package com.donut.mixmessage

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.text.SimpleDateFormat
import java.util.Date
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

    fun getCurrentDate(): String {
        val date = Date()
        val format = SimpleDateFormat("yyyyMMdd")
        return format.format(date)
    }


    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun addition_isCorrect() {
        println(getCurrentDate())
    }
}