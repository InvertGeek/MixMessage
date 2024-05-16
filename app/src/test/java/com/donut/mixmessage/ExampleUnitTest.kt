package com.donut.mixmessage

import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        // 获取当前日期

        for (symbol in 0x13000..0x1342f) {
//            println(symbol)
            //转换unicode
            println(Character.toChars(symbol).joinToString(""))
        }

    }
}