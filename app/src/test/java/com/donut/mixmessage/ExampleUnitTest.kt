package com.donut.mixmessage

import com.donut.mixmessage.util.common.calculateMD5
import com.donut.mixmessage.util.common.genRandomString
import com.donut.mixmessage.util.common.getCurrentDate
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.time.measureTime

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        // 获取当前日期

        println("当前日期: ${getCurrentDate()}")
    }
}