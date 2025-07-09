package com.donut.mixfile.server.core.aes

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.readByteArray
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

fun generateRandomByteArray(size: Int): ByteArray {
    val byteArray = ByteArray(size)
    SecureRandom().nextBytes(byteArray)
    return byteArray
}

fun encryptAES(
    data: ByteArray,
    key: ByteArray,
    iv: ByteArray = generateRandomByteArray(12),
): ByteArray {
    val cipher = getCipher(Cipher.ENCRYPT_MODE, key, iv)
    return iv + cipher.doFinal(data)
}

fun decryptAES(data: ByteArray, key: ByteArray): ByteArray? {
    if (data.size <= 12) {
        return null
    }
    val iv = data.copyOf(12)
    val encryptedData = data.copyOfRange(12, data.size)
    val cipher = getCipher(Cipher.DECRYPT_MODE, key, iv)
    return cipher.doFinal(encryptedData)
}

fun getCipher(mode: Int, key: ByteArray, iv: ByteArray): Cipher {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    val secretKey = SecretKeySpec(key, "AES")
    val gcmParameterSpec = GCMParameterSpec(96, iv)
    cipher.init(mode, secretKey, gcmParameterSpec)
    return cipher
}


suspend fun decryptAES(
    data: ByteReadChannel,
    key: ByteArray,
    limit: Int
): ByteArray {
    val iv = data.readRemaining(12).readByteArray()
    val cipher = getCipher(Cipher.DECRYPT_MODE, key, iv)
    val result = ByteArrayOutputStream()
    var size = 0
    withContext(Dispatchers.IO) {
        while (!data.isClosedForRead) {
            // +12字节ghash 大小,iv已读取
            if (size >= limit + 12) {
                throw Exception("分片文件过大")
            }
            val buffer = data.readRemaining(1024 * 64).readByteArray()
            size += buffer.size
            result.write(cipher.update(buffer))
        }
        result.write(cipher.doFinal())
    }
    return result.toByteArray()
}
