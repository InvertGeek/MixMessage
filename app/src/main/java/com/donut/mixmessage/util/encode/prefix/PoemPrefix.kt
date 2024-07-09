package com.donut.mixmessage.util.encode.prefix

import com.donut.mixmessage.R
import com.donut.mixmessage.util.common.readRawText
import com.donut.mixmessage.util.encode.encoders.ZeroWidthEncoder

object PoemPrefix {
    private val POEMS = readRawText(R.raw.poem).trimIndent().split("|").run {
        val data = this
        HashMap<String, List<String>>().apply {
            data.forEach { poem ->
                val sentences = poem.trim().split("\n").filter { it.isNotBlank() }
                sentences.forEach {
                    put(it, sentences)
                }
            }
        }
    }

    private fun getRandomSentence() = POEMS.values.random().first()


    fun getPoemPrefix(str: String): String {
        //去除空白字符\uFE00-\uFE0f
        val parsedStr = ZeroWidthEncoder.removeInvisibleChars(str)
        //检测
        val poem = POEMS[parsedStr] ?: return getRandomSentence()
        val index = poem.indexOf(parsedStr)
        return poem.getOrElse(index + 1) { getRandomSentence() }
    }

}
