package com.donut.mixmessage.util.encode.encoders.bean

import com.donut.mixmessage.util.common.codePointsString
import com.donut.mixmessage.util.common.decodeBase64
import com.donut.mixmessage.util.common.encodeToBase64
import com.donut.mixmessage.util.common.ignoreError
import com.donut.mixmessage.util.common.isTrue
import com.donut.mixmessage.util.common.subAsString
import com.donut.mixmessage.util.common.truncate
import com.donut.mixmessage.util.encode.RSAUtil
import com.donut.mixmessage.util.encode.getDefaultEncoder
import java.security.PublicKey

data class CoderResult(
    var text: String,
    val password: String,
    val textCoder: TextCoder,
    var originText: String,
//    val isEncrypt: Boolean,
    val isFail: Boolean = false,
    val isSimple: Boolean = false,
    var isTimeLock: Boolean = false,
    val prefix: String = textCoder.generatePrefix(),
) {

    companion object {
        fun media(url: String, fileName: String, identifier: String = IMAGE_IDENTIFIER): String {
            return "$identifier${url.encodeToBase64()}|${fileName.encodeToBase64()}"
        }

        val Failed = CoderResult("", "", getDefaultEncoder(), "", isFail = true, prefix = "")
        const val IMAGE_IDENTIFIER = "__image:"
        const val VIDEO_IDENTIFIER = "__video:"
        const val FILE_IDENTIFIER = "__file:"
        const val PUBLIC_KEY_IDENTIFIER = "__public_key:"
        const val PRIVATE_MESSAGE_IDENTIFIER = "__private_message:"

        fun failed(text: String) = Failed.copy(text = text)
    }


    inline fun isMedia(
        identifier: String,
        block: (url: String, filename: String) -> Unit = { _, _ -> }
    ): Boolean {
        val result = text.startsWith(identifier) && text.contains("|")

        return result.isTrue {
            ignoreError {
                val (url, fileName) = text.substring(identifier.length).split("|")
                block(url.decodeBase64().decodeToString(), fileName.decodeBase64().decodeToString())
            }
        }
    }

    inline fun isImage(block: (url: String, fileName: String) -> Unit = { _, _ -> }) =
        isMedia(IMAGE_IDENTIFIER, block)

    inline fun isVideo(block: (url: String, fileName: String) -> Unit = { _, _ -> }) =
        isMedia(VIDEO_IDENTIFIER, block)

    inline fun isFile(block: (url: String, fileName: String) -> Unit = { _, _ -> }) =
        isMedia(FILE_IDENTIFIER, block)

    inline fun isPublicKey(block: (publicKey: PublicKey) -> Unit = { _ -> }): Boolean {
        val isPublicKey = text.startsWith(PUBLIC_KEY_IDENTIFIER)
        isPublicKey.isTrue {
            ignoreError {
                val keyStr = text.substring(PUBLIC_KEY_IDENTIFIER.length)
                val publicKey = RSAUtil.publicKeyFromString(keyStr)
                block(publicKey)
            }
        }
        return isPublicKey
    }

    inline fun isPrivateMessage(block: () -> Unit = { }): Boolean {
        val isPrivateMessage = text.startsWith(PRIVATE_MESSAGE_IDENTIFIER)
        isPrivateMessage.isTrue {
            ignoreError {
                block()
            }
        }
        return isPrivateMessage
    }

    fun getInfo(full: Boolean = false, prefixLength: Int = 0) = """
                    使用的密钥: ${if (full) password else password.truncate(10)} 
                    加密方法: ${textCoder.name}
                    长度: ${text.length + prefixLength}
                    原始长度: ${originText.length}
                    ${if (isSimple) "精简模式" else ""}
                    ${if (isTimeLock) "时间锁" else ""}
                """.trimIndent().replace("\n", " ")

    fun textWithPrefix(prefixText: String = prefix): String {
        text.isEmpty().isTrue {
            return text
        }
        val codePoints = prefixText.codePointsString()
        if (codePoints.size > 1) {
            return codePoints[0] + text + codePoints.subAsString(1)
        }
        return "$prefixText$text"
    }

}