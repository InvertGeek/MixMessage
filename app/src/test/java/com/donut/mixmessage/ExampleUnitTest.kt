package com.donut.mixmessage

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

    }
}