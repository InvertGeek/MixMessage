package com.donut.mixmessage.util.encode.encoders.bean

import com.donut.mixmessage.util.common.truncate
import com.donut.mixmessage.util.encode.encoders.ShiftEncoder

data class CoderResult(
    var text: String,
    val password: String,
    val textCoder: TextCoder,
    var originText: String,
//    val isEncrypt: Boolean,
    val isFail: Boolean = false,
    val isStrict: Boolean = false,
    val prefix: String = textCoder.generatePrefix(),
) {

    companion object {
        val Failed = CoderResult("", "", ShiftEncoder, "", isFail = true)
    }

    fun getInfo() = """
                    使用的密钥: ${password.truncate(10)} 
                    加密方法: ${textCoder.name}
                    长度: ${text.length}
                    原始长度: ${if (this.isFail) 0 else originText.length}
                    ${if (isStrict) "严格编码" else ""}
                """.trimIndent().replace("\n", " ")

    fun textWithPrefix() = if (text.isEmpty()) text else "$prefix$text"

}