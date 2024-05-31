package com.donut.mixmessage.util.encode

import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.copyToClipboard
import com.donut.mixmessage.util.common.getCurrentDate
import com.donut.mixmessage.util.common.isFalse
import com.donut.mixmessage.util.common.isTrue
import com.donut.mixmessage.util.common.isTrueAnd
import com.donut.mixmessage.util.common.readClipBoardText
import com.donut.mixmessage.util.encode.encoders.AlphaNumEncoder
import com.donut.mixmessage.util.encode.encoders.BuddhaEncoder
import com.donut.mixmessage.util.encode.encoders.ChineseEncoder
import com.donut.mixmessage.util.encode.encoders.EgyptEncoder
import com.donut.mixmessage.util.encode.encoders.EmojiEncoder
import com.donut.mixmessage.util.encode.encoders.SCVEncoder
import com.donut.mixmessage.util.encode.encoders.ShiftEncoder
import com.donut.mixmessage.util.encode.encoders.ZeroWidthEncoder
import com.donut.mixmessage.util.encode.encoders.bean.CoderResult
import java.util.Date

val ENCODERS = listOf(
    ShiftEncoder,
    ZeroWidthEncoder,
    BuddhaEncoder,
    SCVEncoder,
    EmojiEncoder,
    AlphaNumEncoder,
    EgyptEncoder,
    ChineseEncoder
)


var PASSWORDS by cachedMutableOf(setOf("123"), "encoder_passwords")

var DEFAULT_ENCODER by cachedMutableOf(ShiftEncoder.name, "default_encoder")

var DEFAULT_PASSWORD by cachedMutableOf("123", "default_password")

var USE_RANDOM_PASSWORD by cachedMutableOf(false, "use_random_password")

var USE_RANDOM_ENCODER by cachedMutableOf(false, "use_random_encoder")
var LAST_DECODE = System.currentTimeMillis()

var SUCCESS_DECODE_COUNT by cachedMutableOf(0L, "static_success_decode_count")

var ENCODE_COUNT by cachedMutableOf(0L, "static_encode_count")

var USE_TIME_LOCK by cachedMutableOf(false, "use_time_lock_encode")

var TIME_LOCK_REVERSE by cachedMutableOf(0, "time_lock_reverse")

fun increaseSuccessDecodeCount() {
    SUCCESS_DECODE_COUNT++
    LAST_DECODE = System.currentTimeMillis()
//    debug("success decode count: $SUCCESS_DECODE_COUNT")
}

fun increaseEncodeCount() {
    ENCODE_COUNT++
//    debug("encode count: $ENCODE_COUNT")
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
        coder.isEnabled().isTrueAnd(result.isFail) {
            return@any PASSWORDS.any { password ->
                (0..TIME_LOCK_REVERSE).map { num -> password + getCurrentDate(Date(System.currentTimeMillis() - (num * 86400 * 1000))) }
                    .toMutableList().apply {
                        add(0, password)
                    }.any {
                        result = coder.decode(text, it)
                        result.isFail.isFalse {
                            increaseSuccessDecodeCount()
                        }
                    }
            }
        }
        return@any false
    }
    return result
}

fun getCurrentPassword(): String {
    var password = if (USE_RANDOM_PASSWORD) {
        PASSWORDS.filter { !it.contentEquals("123") }.randomOrNull() ?: "123"
    } else {
        DEFAULT_PASSWORD
    }
    if (USE_TIME_LOCK) password += getCurrentDate()
    return password
}

fun encodeText(text: String, password: String = getCurrentPassword()): CoderResult {
    if (text.trim().isEmpty()) {
        return CoderResult.Failed
    }
    var encoder = ENCODERS.firstOrNull { it.name == DEFAULT_ENCODER } ?: ShiftEncoder
    USE_RANDOM_ENCODER.isTrue { encoder = ENCODERS.random() }
    return encoder.encode(
        text.trim(), password
    ).also {
        if (!it.isFail) {
            increaseEncodeCount()
        }
    }
}