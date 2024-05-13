package com.donut.mixmessage.util.encode

import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.copyToClipboard
import com.donut.mixmessage.util.common.debug
import com.donut.mixmessage.util.common.readClipBoardText
import com.donut.mixmessage.util.encode.encoders.AlphaNumEncoder
import com.donut.mixmessage.util.encode.encoders.BuddhaEncoder
import com.donut.mixmessage.util.encode.encoders.EmojiEncoder
import com.donut.mixmessage.util.encode.encoders.SCVEncoder
import com.donut.mixmessage.util.encode.encoders.ShiftEncoder
import com.donut.mixmessage.util.encode.encoders.ZeroWidthEncoder
import com.donut.mixmessage.util.encode.encoders.bean.CoderResult

val ENCODERS = listOf(
    ShiftEncoder,
    ZeroWidthEncoder,
    BuddhaEncoder,
    SCVEncoder,
    EmojiEncoder,
    AlphaNumEncoder
)


var PASSWORDS by cachedMutableOf(setOf("123"), "encoder_passwords")

var DEFAULT_ENCODER by cachedMutableOf(ShiftEncoder.name, "default_encoder")


var DEFAULT_PASSWORD by cachedMutableOf("123", "default_password")

var USE_RANDOM_PASSWORD by cachedMutableOf(false, "use_random_password")

var USE_RANDOM_ENCODER by cachedMutableOf(false, "use_random_encoder")
var LAST_DECODE = System.currentTimeMillis()

var SUCCESS_DECODE_COUNT by cachedMutableOf(0L, "static_success_decode_count")

var ENCODE_COUNT by cachedMutableOf(0L, "static_encode_count")

fun increaseSuccessDecodeCount() {
    SUCCESS_DECODE_COUNT++
    LAST_DECODE = System.currentTimeMillis()
    debug("success decode count: $SUCCESS_DECODE_COUNT")
}

fun increaseEncodeCount() {
    ENCODE_COUNT++
    debug("encode count: $ENCODE_COUNT")
}

fun resetStaticCount() {
    SUCCESS_DECODE_COUNT = 0
    ENCODE_COUNT = 0
}


fun setUseRandomPassword(useRandomPassword: Boolean) {
    USE_RANDOM_PASSWORD = useRandomPassword
}

fun setUseRandomEncoder(useRandomEncoder: Boolean) {
    USE_RANDOM_ENCODER = useRandomEncoder
}

fun modifyPasswords(action: MutableSet<String>.() -> Unit) {
    PASSWORDS = PASSWORDS.toMutableSet().apply(action)
}

fun addPassword(password: String) {
    if (password.isEmpty()) return
    modifyPasswords {
        add(password)
    }
}

fun setDefaultPassword(password: String) {
    DEFAULT_PASSWORD = password
}

fun setDefaultEncoder(encoder: String) {
    DEFAULT_ENCODER = encoder
}

fun removePassword(password: String) {
    if (password == "123") {
        return
    }
//    PASSWORDS.remove(password)
    modifyPasswords {
        remove(password)
    }
}

fun exportAllPassword() {
    val clipBoardStr = PASSWORDS.joinToString("\n")
    clipBoardStr.copyToClipboard()
}

fun importPasswords(): Int {
    val clipBoard = readClipBoardText()
    val origSize = PASSWORDS.size
//    PASSWORDS.addAll(clipBoard.split("\n"))
    modifyPasswords {
        addAll(clipBoard.split("\n"))
    }
    return PASSWORDS.size - origSize
}

fun clearAllPassword() {
//    PASSWORDS.clear()
//    PASSWORDS.add("123")
    modifyPasswords {
        clear()
        add("123")
    }
    setDefaultPassword("123")
}

fun decodeText(text: String): CoderResult {
    var result: CoderResult = CoderResult.Failed
    if (text.trim().isEmpty()) {
        return result
    }

    ENCODERS.any { coder ->
        if (coder.isEnabled() && result.isFail) {
            return@any PASSWORDS.any {
                result = coder.decode(text, it)
                !result.isFail
            }
        }
        return@any false
    }
    if (!result.isFail) {
        increaseSuccessDecodeCount()
    }
    return result
}

fun encodeText(text: String): CoderResult {
    var encoder = ENCODERS.firstOrNull { it.name == DEFAULT_ENCODER } ?: ShiftEncoder
    if (USE_RANDOM_ENCODER) {
        encoder = ENCODERS.random()
    }
    val password = if (USE_RANDOM_PASSWORD) {
        PASSWORDS.random()
    } else {
        DEFAULT_PASSWORD
    }
    return encoder.encode(
        text.trim(), password
    ).also {
        if (!it.isFail) {
            increaseEncodeCount()
        }
    }
}