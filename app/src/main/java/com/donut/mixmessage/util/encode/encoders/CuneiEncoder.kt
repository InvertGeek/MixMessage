package com.donut.mixmessage.util.encode.encoders

import com.donut.mixmessage.util.encode.encoders.bean.ComplexAlphabetEncoder

object CuneiEncoder : ComplexAlphabetEncoder(
    (0x12000..0x1254f).map { Character.toChars(it).joinToString("") }.toList()
) {

    override val name: String = "楔形文字"

}