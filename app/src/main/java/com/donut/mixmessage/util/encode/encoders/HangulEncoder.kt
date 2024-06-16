package com.donut.mixmessage.util.encode.encoders

import com.donut.mixmessage.util.encode.encoders.bean.AlphabetCoder

object HangulEncoder : AlphabetCoder(
    (0xac00..0xd7af).map { it.toChar() }
) {
    override val name: String = "韩文字符"
}