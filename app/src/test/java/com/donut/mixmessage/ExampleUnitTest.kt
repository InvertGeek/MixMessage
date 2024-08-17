package com.donut.mixmessage

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date


@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ExampleUnitTest {


    @Test
    fun test() {
        val time = System.currentTimeMillis() / 1000
        println(time / 1000000)
        println(Date(time / 1000000 * 1000000 * 1000))
        println(formatTime(10000000))
    }

    fun formatTime(time: Long): String {
        //返回格式: 天 小时 分钟,没有到1天或者1小时就不显示
        val day = time / (60 * 60 * 24)
        val hour = time % (60 * 60 * 24) / (60 * 60)
        val minute = time % (60 * 60 * 24) % (60 * 60) / 60
        if (day > 0) {
            return "${day}天${hour}小时${minute}分钟"
        } else if (hour > 0) {
            return "${hour}小时${minute}分钟"
        }
        return "${minute}分钟"
    }


}