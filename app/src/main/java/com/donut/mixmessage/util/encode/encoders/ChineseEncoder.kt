package com.donut.mixmessage.util.encode.encoders

import com.donut.mixmessage.util.encode.encoders.bean.AlphabetCoder

object ChineseEncoder : AlphabetCoder(
    (0x4E00..0x9FA5).map { it.toChar() }
) {

    override val name: String = "随机中文"
}