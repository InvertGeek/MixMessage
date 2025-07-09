package com.donut.mixmessage.util.encode.encoders

import com.donut.mixmessage.decode.lastDecodeResult
import com.donut.mixmessage.ui.routes.settings.routes.ADD_ZERO_WIDTH_PREFIX
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.codePointsString
import com.donut.mixmessage.util.common.isTrue
import com.donut.mixmessage.util.common.subAsString
import com.donut.mixmessage.util.encode.encoders.bean.AlphabetCoder
import com.donut.mixmessage.util.encode.prefix.IdiomPrefix
import com.donut.mixmessage.util.encode.prefix.PoemPrefix
import kotlin.random.Random


object ZeroWidthEncoder : AlphabetCoder(
    (0xfe00..0xfe0f).map { it.toChar() }
) {

    override val name = "空位编码"

    var encodePrefix by cachedMutableOf("x%r%r%r", "zero_width_encode_result_prefix")

    var usePoemPrefix by cachedMutableOf(false, "zero_width_use_poem_prefix")

    var useIdiomPrefix by cachedMutableOf(false, "zero_width_use_idiom_prefix")


    fun removeInvisibleChars(text: String) =
        text.replace(Regex("[\\s\\uFE00-\\uFE0f\\u200b\\u200D]"), "")


    override fun textWithPrefix(prefixText: String, text: String): String {
        text.isEmpty().isTrue {
            return text
        }
        val codePoints = prefixText.codePointsString()
        if (codePoints.size > 1) {
            return codePoints[0] + getZeroWidthCharPrefix() + text + codePoints.subAsString(1)
        }
        return "$prefixText${getZeroWidthCharPrefix()}$text"
    }

    private fun getZeroWidthCharPrefix(): String {
        if (ADD_ZERO_WIDTH_PREFIX) {
            return "\u200d"
        }
        return ""
    }


    override fun generatePrefix(): String {
        val lastText = lastDecodeResult.originText
        useIdiomPrefix.isTrue {
            return IdiomPrefix.getIdiomByPrefix(lastText)
        }
        usePoemPrefix.isTrue {
            return PoemPrefix.getPoemPrefix(lastText)
        }
        return encodePrefix.replace(Regex("%r")) {
            Random.nextInt(10).toString()
        }.replace(Regex("%e")) {
            EmojiEncoder.replaceMap.keys.random()
        }
    }


}
