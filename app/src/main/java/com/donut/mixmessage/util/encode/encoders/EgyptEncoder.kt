package com.donut.mixmessage.util.encode.encoders

import com.donut.mixmessage.util.encode.encoders.bean.ComplexAlphabetEncoder

object EgyptEncoder : ComplexAlphabetEncoder(
    (0x13000..0x1342f).map { Character.toChars(it).joinToString("") }.toList()
) {

    override val name: String = "埃及字符"

}