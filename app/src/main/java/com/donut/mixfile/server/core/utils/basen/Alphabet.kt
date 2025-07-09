package com.donut.mixfile.server.core.utils.basen

class Alphabet private constructor(val key: String) {


    companion object Factory {
        private val cache = mutableMapOf<String, Alphabet>()

        fun predefined(id: Int): Alphabet {
            return fromString(ALPHABETS[id]!!)
        }


        fun fromString(key: String): Alphabet {
            val filtered = key.groupingBy { it }.eachCount()
                .filter {
                    it.value == 1 && !it.key.isWhitespace()
                }.keys.joinToString("") {
                    it.toString()
                }
            return cache[filtered] ?: Alphabet(filtered).also { cache[filtered] = it }
        }


        fun fromCharList(listOf: List<Char>): Alphabet {
            val dupes = listOf.groupingBy { it }.eachCount().filter { it.value > 1 }
            if (dupes.isNotEmpty()) {
                throw IllegalArgumentException(
                    "Duplicate characters in alphabet: ${
                        dupes.run {
                            this.entries.joinToString(",") {
                                String.format("u+%04x", it.key.code).uppercase()
                            }
                        }
                    }"
                )
            }
            return fromString(listOf.joinToString(""))
        }

    }

    private val inverse: Map<Char, Int> =
        mapOf(*key.mapIndexed { i: Int, ch: Char -> ch to i }.toTypedArray())
    val encoder = CodecDirection(256, key.length)
    val decoder = CodecDirection(key.length, 256)

    fun toDigits(chars: String): IntArray = IntArray(chars.length) { i ->
        inverse[chars[i]]
            ?: throw java.lang.IndexOutOfBoundsException("'${chars[i]}' not in alphabet")
    }

    fun toChars(digits: IntArray): String = String(CharArray(digits.size) { i ->
        digits[i].let {
            if (it < key.length)
                key[it]
            else
                throw java.lang.IndexOutOfBoundsException("digit:${it} not in alphabet")
        }
    })

}