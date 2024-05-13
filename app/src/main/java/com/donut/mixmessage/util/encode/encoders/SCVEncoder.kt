package com.donut.mixmessage.util.encode.encoders

import com.donut.mixmessage.util.encode.encoders.bean.ComplexAlphabetEncoder


object SCVEncoder : ComplexAlphabetEncoder(
    listOf(
        "富强",
        "民主",
        "文明",
        "和谐",
        "自由",
        "平等",
        "公正",
        "法治",
        "爱国",
        "敬业",
        "诚信",
        "友善"
    )
) {
    override val name = "核心价值观"
}