package com.donut.mixmessage.util.common

inline fun <T> T?.isNull(block: UnitBlock = {}): Boolean {
    if (this == null) {
        block()
    }
    return this == null
}

inline fun <T> T?.isNotNull(block: (T) -> Unit = {}): Boolean {
    if (this != null) {
        block(this)
    }
    return this != null
}

inline fun <T> T?.isNotNullAnd(condition: Boolean, block: (T) -> Unit = {}): Boolean {
    if (condition && this != null) {
        block(this)
    }
    return condition && this != null
}

inline fun <T> T.isEqual(other: Any?, block: (T) -> Unit = {}): Boolean {
    if (this == other) {
        block(this)
    }
    return this == other
}

inline fun Boolean?.isTrue(block: UnitBlock = {}): Boolean {
    if (this == true) {
        block()
    }
    return this == true
}

inline fun Boolean?.isTrueAnd(condition: Boolean, block: UnitBlock = {}): Boolean {
    if (condition && this == true) {
        block()
    }
    return condition && this == true
}

inline fun Boolean?.isFalse(block: UnitBlock = {}): Boolean {
    if (this == false) {
        block()
    }
    return this == false
}