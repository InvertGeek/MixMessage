package com.donut.mixmessage.util.encode.encoders.bean

import com.donut.mixmessage.util.encode.encoders.ShiftEncoder


abstract class ComplexAlphabetEncoder(
    stringList: List<String>,
    val replaceMap: Map<String, Char> =
        stringList.distinct()
            .mapIndexed { index, s -> s to ShiftEncoder.Alphabet.getCharByIndex(index) }
            .toMap()
) : AlphabetCoder(replaceMap.values.toList()) {
    override fun decode(input: String, password: String): CoderResult {
        val superResult = super.decode(input.run {
            replaceMap.entries.fold(this) { acc, entry ->
                acc.replace(entry.key, entry.value.toString())
            }
        }, password)
        superResult.originText = input
        return superResult
    }

    override fun encode(input: String, password: String): CoderResult {
        val superResult = super.encode(input, password)
        superResult.text = superResult.text.run {
            replaceMap.entries.fold(this) { acc, entry ->
                acc.replace(entry.value.toString(), entry.key)
            }
        }
        return superResult
    }
}

