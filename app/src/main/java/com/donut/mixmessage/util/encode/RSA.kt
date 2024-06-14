package com.donut.mixmessage.util.encode

import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.catchError
import com.donut.mixmessage.util.common.decodeBase64
import com.donut.mixmessage.util.common.encodeToBase64
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

@Suppress("MemberVisibilityCanBePrivate")
object RSAUtil {
    var publicKey by cachedMutableOf("", "rsa_public_key")

    var privateKey by cachedMutableOf("", "rsa_private_key")

    init {
        if (publicKey.isEmpty() || privateKey.isEmpty()) {
            regenerateKeyPair()
        }
    }

    fun getPublicKey() = publicKeyFromString(publicKey)

    fun getPrivateKey() = privateKeyFromString(privateKey)


    fun regenerateKeyPair() {
        val keyPair = generateKeyPair()
        publicKey = keyPair.public.encoded.encodeToBase64()
        privateKey = keyPair.private.encoded.encodeToBase64()
    }

    private val keyFactory = KeyFactory.getInstance("RSA")
    private fun String.publicKeySpec(): X509EncodedKeySpec {
        val keyBytes = this.decodeBase64()
        return X509EncodedKeySpec(keyBytes)
    }

    private fun String.privateKeySpec(): PKCS8EncodedKeySpec {
        val keyBytes = this.decodeBase64()
        return PKCS8EncodedKeySpec(keyBytes)
    }

    fun publicKeyFromString(string: String): PublicKey {
        return keyFactory.generatePublic(string.publicKeySpec())
    }

    fun privateKeyFromString(string: String): PrivateKey {
        return keyFactory.generatePrivate(string.privateKeySpec())
    }

    fun getCipher() = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")

    fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        return keyPairGenerator.genKeyPair()
    }

    fun encryptRSA(
        data: ByteArray,
        publicKey: PublicKey = getPublicKey()
    ): ByteArray {
        val cipher = getCipher()
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(data)
    }


    fun decryptRSA(
        data: ByteArray,
        privateKey: PrivateKey = getPrivateKey()
    ): ByteArray {
        val cipher = getCipher()
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        return cipher.doFinal(data)
    }

    fun encryptRSA(
        data: String,
        publicKey: PublicKey = getPublicKey()
    ): String {
        return encryptRSA(data.toByteArray(), publicKey).encodeToBase64()
    }

    fun decryptRSA(
        data: String,
        privateKey: PrivateKey = getPrivateKey()
    ): String {
        catchError {
            return decryptRSA(data.decodeBase64(), privateKey).decodeToString()
        }
        return ""
    }
}
