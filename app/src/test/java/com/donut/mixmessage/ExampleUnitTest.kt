package com.donut.mixmessage

import org.junit.Test
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher
import kotlin.io.encoding.ExperimentalEncodingApi

fun generateKeyPair(): KeyPair {
    val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
    keyPairGenerator.initialize(2048)
    return keyPairGenerator.generateKeyPair()
}

fun encrypt(text: String, publicKey: PublicKey): ByteArray {
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    cipher.init(Cipher.ENCRYPT_MODE, publicKey)
    return cipher.doFinal(text.toByteArray())
}

fun decrypt(encryptedText: ByteArray, privateKey: PrivateKey): String {
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
    cipher.init(Cipher.DECRYPT_MODE, privateKey)
    val decryptedBytes = cipher.doFinal(encryptedText)
    return String(decryptedBytes)
}

class ExampleUnitTest {
    @OptIn(ExperimentalEncodingApi::class)
    @Test
    fun addition_isCorrect() {

    }
}