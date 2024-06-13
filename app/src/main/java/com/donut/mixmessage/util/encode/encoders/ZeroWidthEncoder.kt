package com.donut.mixmessage.util.encode.encoders;

import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.encode.encoders.bean.AlphabetCoder
import kotlin.random.Random


object ZeroWidthEncoder : AlphabetCoder(
    listOf(
        '\uFE00', //VARIATION SELECTOR-1
        '\uFE01', //VARIATION SELECTOR-2
        '\uFE02', //VARIATION SELECTOR-3
        '\uFE03', //VARIATION SELECTOR-4
        '\uFE04', //VARIATION SELECTOR-5
        '\uFE05', //VARIATION SELECTOR-6
        '\uFE06', //VARIATION SELECTOR-7
        '\uFE07', //VARIATION SELECTOR-8
        '\uFE08', //VARIATION SELECTOR-9
        '\uFE09', //VARIATION SELECTOR-10
        '\uFE0A', //VARIATION SELECTOR-11
        '\uFE0B', //VARIATION SELECTOR-12
        '\uFE0C', //VARIATION SELECTOR-13
        '\uFE0D', //VARIATION SELECTOR-14
        '\uFE0E', //VARIATION SELECTOR-15
        '\uFE0F', //VARIATION SELECTOR-16
    )
) {

    override val name = "空位编码"

    var encodePrefix by cachedMutableOf("x%r%r%r", "zero_width_encode_result_prefix")


    override fun generatePrefix(): String {
        return encodePrefix.replace(Regex("%r")) {
            Random.nextInt(10).toString()
        }.replace(Regex("%e")) {
            EmojiEncoder.replaceMap.keys.random()
        }
    }


}
