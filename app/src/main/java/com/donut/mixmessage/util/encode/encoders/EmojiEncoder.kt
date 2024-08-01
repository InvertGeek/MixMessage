package com.donut.mixmessage.util.encode.encoders

import com.donut.mixmessage.util.encode.encoders.bean.ComplexAlphabetEncoder

private val invalid = listOf(
    0x1f322,
    0x1f323,
    0x1f394,
    0x1f395,
    0x1f398,
    0x1f39c,
    0x1f39d,
    0x1f3f1,
    0x1f3f2,
    0x1f3f6,
    0x1f3fb,
    0x1f3fc,
    0x1f3fd,
    0x1f3fe,
    0x1f3ff,
    0x1f53e..0x1f548,
    0x1f54f,
    0x1f568..0x1f56e,
    0x1f571,
    0x1f572,
    0x1f57b..0x1f586,
    0x1f588,
    0x1f589,
    0x1f58e,
    0x1f58f,
    0x1f591..0x1f594,
    0x1f597..0x1f5a3,
    0x1f5a6..0x1f5a7,
    0x1f5a9..0x1f5b0,
    0x1f5b3..0x1f5bb,
    0x1f5bd..0x1f5c1,
    0x1f5c5..0x1f5d0,
    0x1f5d4..0x1f5db,
    0x1f5df..0x1f5e0,
    0x1f5e2,
    0x1f5e4..0x1f5e7,
    0x1f5e9..0x1f5ee,
    0x1f5f0..0x1f5f2,
    0x1f5f4..0x1f5f9,
).map {
    if (it is IntRange){
        return@map (it.first..it.last).toList()
    }
    return@map listOf(it)
}.flatten().toHashSet()

object EmojiEncoder : ComplexAlphabetEncoder(
    (0x1f300..0x1f64f).filter { !invalid.contains(it) }
        .map { Character.toChars(it).joinToString("") }.toList()
) {
    override val name: String
        get() = "表情编码"

}