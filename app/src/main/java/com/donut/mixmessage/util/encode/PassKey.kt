package com.donut.mixmessage.util.encode

import android.os.Parcelable
import com.donut.mixmessage.util.common.cachedMutableOf
import com.donut.mixmessage.util.common.copyToClipboard
import com.donut.mixmessage.util.common.getCurrentDate
import com.donut.mixmessage.util.common.isNotNull
import com.donut.mixmessage.util.common.isNull
import com.donut.mixmessage.util.common.isTrue
import com.donut.mixmessage.util.common.isTrueAnd
import com.donut.mixmessage.util.common.readClipBoardText
import com.donut.mixmessage.util.common.showToast
import kotlinx.parcelize.Parcelize

var PASSWORDS by cachedMutableOf(listOf(PassKey("123")), "encoder_passwords_v2")
var PASSWORDS_OLD by cachedMutableOf(setOf(), "encoder_passwords").also {
    val value = it.getValue(null, null)
    if (value.isEmpty()) {
        return@also
    }
    value.forEach { oldPass ->
        addPassword(oldPass)
    }
    it.setValue(null, null, setOf())
}

@Parcelize
data class PassKey(
    private var passValue: String,
) : Parcelable {

    val value: String
        get() = passValue


    @Synchronized
    fun updateValue(newValue: String) {
        modifyPasswords {
            val index = indexOfFirst { it == this@PassKey }
            if (index == -1) {
                return@modifyPasswords
            }
            set(index, PassKey(newValue))
        }
        val isDefault = DEFAULT_PASSWORD.contentEquals(passValue)
        if (isDefault) {
            setDefaultPassword(newValue)
        }
        passValue = newValue
        fixDefaultPassword()
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PassKey

        return value == other.value
    }

    fun isRoundKey(): Boolean {
        if (!value.startsWith("_r:")) {
            return false
        }
        val splits = value.split(":")
        if (splits.size != 7) {
            return false
        }
        val date = RoundKey.checkDate(splits[4])
        date.isNull {
            return false
        }
        return true
    }

}

fun exportAllPassword() {
    val clipBoardStr = getPassStringList().joinToString("\n")
    clipBoardStr.copyToClipboard()
}

fun getCurrentPass(password: String): String {
    var currentRoundKey: String? = null
    val key = PassKey(password)
    if (key.isRoundKey()) {
        val name = RoundKey(key).name
        currentRoundKey = getRoundKey(name)?.value
    }
    return currentRoundKey ?: password
}

fun addPassword(password: String) {
    modifyPasswords {
        add(PassKey(password))
    }
}

fun getPasswordIndex(password: String, isTimeLock: Boolean = false): Int {
    password.isEmpty().isTrue {
        return -1
    }
    isTimeLock.isTrueAnd(getCurrentDate().length < password.length) {
        return getPassStringList().indexOf(
            password.substring(
                0,
                password.length - getCurrentDate().length
            )
        )
    }
    return getPassStringList().indexOf(password)
}

fun setDefaultPassword(password: String) {
    DEFAULT_PASSWORD = password
}

fun setDefaultEncoder(encoder: String) {
    DEFAULT_ENCODER = encoder
}

fun clearAllPassword() {
    modifyPasswords {
        clear()
        add(PassKey("123"))
    }
    setDefaultPassword("123")
}

fun modifyPasswords(action: MutableList<PassKey>.() -> Unit) {
    PASSWORDS = PASSWORDS.toMutableList().apply(action).distinct()
}

fun manualAddPassword(password: String): Boolean {
    if (getPassStringList().contains(password)) {
        showToast("密钥已存在")
        return false
    }
    if (password.isBlank()) {
        showToast("密钥不能为空")
        return false
    }
    val key = PassKey(password)
    if (key.isRoundKey()) {
        val name = RoundKey(PassKey(password)).name
        if (getRoundKey(name).isNotNull()) {
            showToast("相同名称轮换密钥已存在")
            return false
        }
    }
    addPassword(password)
    showToast("添加成功")
    return true
}


fun removePassword(password: String) {
    if (password == "123") {
        return
    }
    val currentPassword = getCurrentPass(password)
    if (currentPassword.contentEquals(DEFAULT_PASSWORD)) {
        setDefaultPassword("123")
    }
    modifyPasswords {
        val value = firstOrNull { it.value == currentPassword }
        remove(value)
    }
    fixDefaultPassword()
}

fun fixDefaultPassword() {
    if (PASSWORDS.firstOrNull { it.value.contentEquals(DEFAULT_PASSWORD) } == null) {
        setDefaultPassword(PASSWORDS.firstOrNull()?.value ?: "123")
    }
}

fun importPasswords(): Int {
    val clipBoard = readClipBoardText()
    val origSize = PASSWORDS.size
    modifyPasswords {
        addAll(clipBoard.split("\n").map { PassKey(it) })
    }
    return PASSWORDS.size - origSize
}

data class KeyInfo(
    val pass: String,
    val isTimeLock: Boolean = false,
    var roundKey: RoundKey? = null,
) {

    fun isRoundKey() = roundKey.isNotNull()

    fun getUsingPass(): KeyInfo {
        if (isRoundKey()) {
            return KeyInfo(roundKey?.currentKey ?: "", false, roundKey)
        }
        return KeyInfo(pass)
    }

    fun getTimeLockPass(): MutableList<KeyInfo> {
        val timeLockReversePass = mutableListOf<KeyInfo>()
        (0..TIME_LOCK_REVERSE).forEach { num ->
            timeLockReversePass.add(
                KeyInfo(
                    getUsingPass().pass + getCurrentDate(num),
                    true
                )
            )
        }
        return timeLockReversePass
    }

    init {
        val key = PassKey(pass)
        if (key.isRoundKey()) {
            roundKey = RoundKey(key)
            roundKey?.updateKey()
        }
    }
}


fun getCurrentPassword(): String {
    val password = DEFAULT_PASSWORD
    val keyInfo = KeyInfo(password)
    if (keyInfo.isRoundKey()) {
        return password
    }
    if (USE_TIME_LOCK) {
        return password + getCurrentDate()
    }
    return password
}

fun getRoundKey(name: String) = PASSWORDS.firstOrNull {
    it.isRoundKey() && RoundKey(it).name.contentEquals(name)
}

fun updateRoundKeys() {
    PASSWORDS.forEach {
        if (it.isRoundKey()) {
            RoundKey(it).updateKey()
        }
    }
}