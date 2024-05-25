package com.donut.mixmessage.util.encode

import com.donut.mixmessage.util.common.decodeBase64
import com.donut.mixmessage.util.common.encodeToBase64
import com.donut.mixmessage.util.common.hashSHA256
import com.donut.mixmessage.util.common.ignoreError
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

fun encryptAES(data: ByteArray, key: ByteArray, iv: ByteArray = key): ByteArray {
    ignoreError {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = SecretKeySpec(key, "AES")
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
        return cipher.doFinal(data)
    }
    return byteArrayOf()
}

fun decryptAES(data: ByteArray, key: ByteArray, iv: ByteArray = key): ByteArray {
    ignoreError {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = SecretKeySpec(key, "AES")
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
        return cipher.doFinal(data)
    }
    return byteArrayOf()
}

fun encryptAES(data: ByteArray, key: String): ByteArray {
    return encryptAES(data, key.hashSHA256())
}

fun decryptAES(data: ByteArray, key: String): ByteArray {
    return decryptAES(data, key.hashSHA256())
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