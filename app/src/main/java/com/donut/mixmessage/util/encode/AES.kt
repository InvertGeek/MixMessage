package com.donut.mixmessage.util.encode

import com.donut.mixmessage.util.common.decodeBase64
import com.donut.mixmessage.util.common.encodeToBase64
import com.donut.mixmessage.util.common.hashSHA256
import com.donut.mixmessage.util.common.ignoreError
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

fun generateRandomByteArray(size: Int): ByteArray {
    val byteArray = ByteArray(size)
    SecureRandom().nextBytes(byteArray)
    return byteArray
}


fun transformNumber(radix: Int, number: Long, maxCount: Int): List<Long> {
    val result = mutableListOf<Long>()
    var n = number
    for (i in 1..maxCount) {
        result.add(n % radix)
        n /= radix
    }
    return result
}

fun reverseTransformNumber(radix: Int, numbers: List<Int>): Long {
    var result = 0L
    var r = 1L
    for (i in numbers) {
        result += i * r
        r *= radix
    }
    return result
}


fun encryptAES(
    data: ByteArray,
    key: ByteArray,
    iv: ByteArray = generateRandomByteArray(16)
): ByteArray {
    ignoreError {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = SecretKeySpec(key, "AES")
        val gcmParameterSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec)
        return iv + cipher.doFinal(data)
    }
    return byteArrayOf()
}

fun decryptAES(data: ByteArray, key: ByteArray): ByteArray {
    ignoreError {
        if (data.size <= 16) {
            return@ignoreError
        }
        val iv = data.copyOf(16)
        val encryptedData = data.copyOfRange(16, data.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = SecretKeySpec(key, "AES")
        val gcmParameterSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)
        return cipher.doFinal(encryptedData)
    }
    return byteArrayOf()
}

fun encryptAES(data: ByteArray, key: String): ByteArray {
    return encryptAES(data, key.hashSHA256())
}

fun decryptAES(data: ByteArray, key: String): ByteArray {
    return decryptAES(data, key.hashSHA256())
}

fun encryptAES(data: String, key: String): ByteArray {
    return encryptAES(data.toByteArray(), key.hashSHA256())
}

fun decryptAES(data: String, key: String): ByteArray {
    return decryptAES(data.toByteArray(), key.hashSHA256())
}


fun encryptAESBase64(text: String, key: String): String {
    return encryptAES(text.toByteArray(), key.hashSHA256()).encodeToBase64()
}

fun decryptAESBase64(encryptedText: String, key: String): String {
    return decryptAES(
        encryptedText.decodeBase64(),
        key.hashSHA256()
    ).decodeToString()
}