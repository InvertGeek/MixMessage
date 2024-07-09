package com.donut.mixmessage.util.encode.prefix

import com.donut.mixmessage.R
import com.donut.mixmessage.util.common.readRawText
import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum
import com.github.houbb.pinyin.util.PinyinHelper

object IdiomPrefix {
    private val IDIOM_DATA =
        readRawText(R.raw.idiom).trimIndent().split("\n").filter { it.isNotBlank() }

    private val PREFIX_MAP = mutableMapOf<String, MutableList<String>>()

    init {
        IDIOM_DATA.forEach {
            val pinyinPrefix = getFirstPinyin(it)
            PREFIX_MAP.getOrPut(pinyinPrefix) { mutableListOf() }.add(it)
        }
    }

    private fun getPinyin(str: String) =
        PinyinHelper.toPinyin(str, PinyinStyleEnum.NORMAL).split(" ")

    private fun getFirstPinyin(str: String) = getPinyin(str).firstOrNull() ?: ""

    private fun getLastPinyin(str: String) = getPinyin(str).lastOrNull() ?: ""

    fun getIdiomByPrefix(str: String?): String {
        val defaultValue = IDIOM_DATA.random()
        if (str == null) {
            return defaultValue
        }
        val list = PREFIX_MAP[getLastPinyin(str)]
        return list?.random() ?: defaultValue
    }

}