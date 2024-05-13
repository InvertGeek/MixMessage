package com.donut.mixmessage.util.encode.encoders;

import com.donut.mixmessage.util.encode.encoders.bean.AlphabetCoder

object AlphaNumEncoder : AlphabetCoder((0..9).map { it.toString()[0] } + ('a'..'z') + ('A'..'Z')) {
    override val name = "英文数字"
}