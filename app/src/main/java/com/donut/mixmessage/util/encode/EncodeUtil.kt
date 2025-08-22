package com.donut.mixmessage.util.encode

import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.default
import com.donut.mixmessage.util.common.isFalse
import com.donut.mixmessage.util.common.isTrue
import com.donut.mixmessage.util.common.isTrueAnd
import com.donut.mixmessage.util.encode.encoders.AlphaNumEncoder
import com.donut.mixmessage.util.encode.encoders.BuddhaEncoder
import com.donut.mixmessage.util.encode.encoders.ChineseEncoder
import com.donut.mixmessage.util.encode.encoders.CuneiEncoder
import com.donut.mixmessage.util.encode.encoders.EgyptEncoder
import com.donut.mixmessage.util.encode.encoders.EmojiEncoder
import com.donut.mixmessage.util.encode.encoders.HangulEncoder
import com.donut.mixmessage.util.encode.encoders.SCVEncoder
import com.donut.mixmessage.util.encode.encoders.YiEncoder
import com.donut.mixmessage.util.encode.encoders.ZeroWidthEncoder
import com.donut.mixmessage.util.encode.encoders.bean.CoderResult

val ENCODERS = listOf(
    ZeroWidthEncoder,
    BuddhaEncoder,
    SCVEncoder,
    EmojiEncoder,
    AlphaNumEncoder,
    EgyptEncoder,
    ChineseEncoder,
    YiEncoder,
    HangulEncoder,
    CuneiEncoder,
)


var DEFAULT_ENCODER by cachedMutableOf(ZeroWidthEncoder.name, "default_encoder")

var DEFAULT_PASSWORD by cachedMutableOf("123", "default_password")

var LAST_DECODE = System.currentTimeMillis()

var SUCCESS_DECODE_COUNT by cachedMutableOf(0L, "static_success_decode_count")

var ENCODE_COUNT by cachedMutableOf(0L, "static_encode_count")

var USE_TIME_LOCK by cachedMutableOf(false, "use_time_lock_encode")

var TIME_LOCK_REVERSE by cachedMutableOf(0, "time_lock_reverse")

fun getPassStringList() = PASSWORDS.map { it.value }


fun getDefaultEncoder() =
    ENCODERS.firstOrNull { it.name.contentEquals(DEFAULT_ENCODER) } default ZeroWidthEncoder

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


fun decodeText(text: String): CoderResult {
    var result: CoderResult = CoderResult.Failed
    if (text.trim().isEmpty()) {
        return result
    }

    val passwordInfoList = PASSWORDS.toMutableList().map {
        KeyInfo(it.value, false)
    }.toMutableList()

    val passwordList = mutableListOf<KeyInfo>()

    passwordInfoList.forEach {
        if (it.isRoundKey()) {
            passwordList.add(KeyInfo(it.roundKey?.lastKey ?: it.pass, false, it.roundKey))
            passwordList.add(KeyInfo(it.roundKey?.getNextKeyHash() ?: it.pass, false, it.roundKey))
        }
        passwordList.add(it.getUsingPass())
        passwordList.addAll(it.getTimeLockPass())
    }

    ENCODERS.any { coder ->
        coder.isEnabled().isTrueAnd(result.isFail) {
            return@any passwordList.any {
                result = coder.decode(text, it.pass)
                result.isTimeLock = it.isTimeLock
                result.roundKey = it.roundKey
                val isSuccess = !result.isFail
                isSuccess.apply {
                    isTrue {
                        increaseSuccessDecodeCount()
                    }
                    isFalse {
                        result.isTimeLock = false
                    }
                }
            }
        }
    }
    return result
}

fun encodeText(text: String, password: String = getCurrentPassword()): CoderResult {
    if (text.trim().isEmpty()) {
        return CoderResult.Failed
    }
    if (PASSWORDS.firstOrNull { it.value.contentEquals(DEFAULT_PASSWORD) } == null) {
        setDefaultPassword(PASSWORDS.firstOrNull()?.value ?: "123")
    }
    val encoder = getDefaultEncoder()
    val keyInfo = KeyInfo(password)
    return encoder.encode(
        text.trim(), keyInfo.getUsingPass().pass
    ).also {
        if (!it.isFail) {
            increaseEncodeCount()
        }
    }.apply {
        isTimeLock = if (keyInfo.isRoundKey()) false else USE_TIME_LOCK
        isEncrypt = true
        roundKey = keyInfo.roundKey
        textWithPrefix = textCoder.textWithPrefix(prefix, this.text)
    }
}