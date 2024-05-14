package com.donut.mixmessage

import com.donut.mixmessage.util.common.calculateMD5
import org.junit.Test
import kotlin.time.measureTime

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
       measureTime {
           "".calculateMD5(10000)
       }.also {
           println(it)
       }
    }
}