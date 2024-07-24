package com.donut.mixmessage.util.encode.encoders

import com.donut.mixmessage.util.encode.encoders.bean.ComplexAlphabetEncoder


object EmojiEncoder : ComplexAlphabetEncoder(
    (0x1f300..0x1f64f).map { Character.toChars(it).joinToString("") }.toList()
) {
    override val name: String
        get() = "表情编码"

}