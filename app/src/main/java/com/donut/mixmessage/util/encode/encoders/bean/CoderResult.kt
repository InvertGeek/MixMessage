package com.donut.mixmessage.util.encode.encoders.bean

import com.donut.mixmessage.util.common.ignoreError
import com.donut.mixmessage.util.common.isNotNull
import com.donut.mixmessage.util.common.isTrue
import com.donut.mixmessage.util.encode.RSAUtil
import com.donut.mixmessage.util.encode.RoundKey
import com.donut.mixmessage.util.encode.getDefaultEncoder
import com.donut.mixmessage.util.encode.getPasswordIndex
import java.security.PublicKey

data class CoderResult(
    var text: String,
    val password: String,
    val textCoder: TextCoder,
    var originText: String,
    var roundKey: RoundKey? = null,
    var isEncrypt: Boolean = false,
    val isFail: Boolean = false,
    val isSimple: Boolean = false,
    var isTimeLock: Boolean = false,
    val prefix: String = textCoder.generatePrefix(),
    var textWithPrefix: String = text
) {

    companion object {

        val Failed = CoderResult("", "", getDefaultEncoder(), "", isFail = true, prefix = "")

        const val PUBLIC_KEY_IDENTIFIER = "__public_key:"
        const val PRIVATE_MESSAGE_IDENTIFIER = "__private_message:"

        fun failed(text: String) = Failed.copy(originText = text)
    }

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

    private fun Boolean.trueText(trueText: String, falseText: String = "") =
        if (this) trueText else falseText

    fun getInfo(full: Boolean = false): String {
        val indexPass = roundKey?.key?.value ?: password
        return """
                    ${full.trueText("使用的密钥: $password")}
                    ${(!full).trueText("密钥编号: #${getPasswordIndex(indexPass, isTimeLock)}")}
                    加密方法: ${textCoder.name}
                    长度: ${textWithPrefix.length}
                    原始长度: ${originText.length}
                    ${isSimple.trueText("精简模式")}
                    ${isTimeLock.trueText("时间锁")}
                    ${roundKey.isNotNull().trueText("轮换: ${roundKey?.name}")}
                """.trimIndent().replace("\n", " ")
    }


    fun textWithPrefix(prefixText: String = prefix): String {
        return textCoder.textWithPrefix(prefixText, text)
    }

}