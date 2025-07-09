package com.donut.mixfile.server.core.utils

import com.donut.mixfile.server.core.objects.MixShareInfo
import java.security.MessageDigest

tailrec fun String.hashToMD5String(round: Int = 1): String {
    val digest = hashMD5()
    if (round > 1) {
        return digest.toHex().hashToMD5String(round - 1)
    }
    return digest.toHex()
}

fun ByteArray.toHex(): String {
    val sb = StringBuilder()
    for (b in this) {
        sb.append(String.format("%02x", b))
    }
    return sb.toString()
}

fun String.hashMD5() = hashToHexString("MD5")

fun String.hashSHA256() = hashToHexString("SHA-256")

fun String.hashToHexString(algorithm: String): ByteArray {
    val md = MessageDigest.getInstance(algorithm)
    md.update(this.toByteArray())
    return md.digest()
}

fun ByteArray.hashToHexString(algorithm: String): String {
    return calcHash(algorithm).toHex()
}

fun ByteArray.calcHash(algorithm: String): ByteArray {
    val md = MessageDigest.getInstance(algorithm)
    md.update(this)
    return md.digest()
}

fun ByteArray.hashSHA256() = calcHash("SHA-256")

fun ByteArray.hashSHA256String() = hashToHexString("SHA-256")

fun resolveMixShareInfo(value: String): MixShareInfo? {
    return parseShareCode(value)
}

private val encodeMap = run {
    val map = mutableMapOf<String, String>()
    for (value in 0xfe00..0xfe0f) {
        val key = (value - 0xfe00).toString(16)
        map[key] = "${value.toChar()}"
    }
    map
}

fun MixShareInfo.shareCode(useShortCode: Boolean): String {
    if (useShortCode) {
        return "mf://${encodeHex(this.toString())}${
            this.toString().take(8)
        }"
    }
    return "mf://$this"
}

fun parseShareCode(code: String): MixShareInfo? {
    val mf = code.substringAfter("mf://")
    val decoded = decodeHex(mf)
    val parsed = MixShareInfo.tryFromString(decoded) ?: MixShareInfo.tryFromString(mf)
    return parsed
}

fun encodeHex(data: String): String {
    val sb = StringBuilder()
    for (element in data.toByteArray().toHex()) {
        if (encodeMap.containsKey(element.toString())) {
            sb.append(encodeMap[element.toString()])
        }
    }
    return sb.toString()
}

fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }

    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

fun decodeHex(data: String): String {
    val sb = StringBuilder()
    for (element in data) {
        if (encodeMap.containsValue(element.toString())) {
            sb.append(encodeMap.filterValues { it == element.toString() }.keys.first())
        }
    }
    return sb.toString().decodeHex().decodeToString()
}