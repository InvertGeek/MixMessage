package com.donut.mixmessage.util.encode.encoders

import com.donut.mixmessage.util.encode.encoders.bean.AlphabetCoder

object YiEncoder : AlphabetCoder(
    (0x4dc0..0x4dff).map { it.toChar() }
) {
    override val name: String = "易经八卦"
}