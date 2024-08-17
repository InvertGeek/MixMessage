package com.donut.mixmessage.util.common

import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.donut.mixmessage.kv
import kotlinx.parcelize.Parcelize

fun <T> constructCachedMutableValue(
    value: T,
    key: String,
    setVal: (value: T) -> Unit,
    getVal: () -> T,
) =
    object : CachedMutableValue<T>(value, key) {
        override fun readCachedValue(): T {
            return getVal()
        }

        override fun writeCachedValue(value: T) {
            setVal(value)
        }
    }


fun cachedMutableOf(value: String, key: String) =
    constructCachedMutableValue(
        value,
        key,
        { kv.encode(key, it) },
        { kv.decodeString(key, value)!! })

fun cachedMutableOf(value: Boolean, key: String) =
    constructCachedMutableValue(value, key, { kv.encode(key, it) }, { kv.decodeBool(key, value) })

fun cachedMutableOf(value: Long, key: String) =
    constructCachedMutableValue(value, key, { kv.encode(key, it) }, { kv.decodeLong(key, value) })


@Parcelize
data class ParcelableItemList<T : Parcelable>(
    val items: List<T>
) : Parcelable

inline fun < reified T : Parcelable> cachedMutableOf(value: List<T>, key: String) =
    constructCachedMutableValue(
        value,
        key,
        { kv.encode(key, ParcelableItemList(it)) },
        getter@{
            val data = kv.decodeParcelable(key,ParcelableItemList::class.java) ?: return@getter value
            @Suppress("UNCHECKED_CAST")
            return@getter data.items as List<T>
        }
    )

fun cachedMutableOf(value: Parcelable, key: String) =
    constructCachedMutableValue(
        value,
        key,
        { kv.encode(key, it) },
        { kv.decodeParcelable(key, value.javaClass) })


fun cachedMutableOf(value: Set<String>, key: String) =
    constructCachedMutableValue(
        value,
        key,
        { kv.encode(key, it) },
        { kv.decodeStringSet(key, value)!! },
    )


abstract class CachedMutableValue<T>(
    value: T,
    private val key: String,
) {
    var value by mutableStateOf(value)
    private var loaded = false
    abstract fun readCachedValue(): T

    abstract fun writeCachedValue(value: T)

    operator fun getValue(thisRef: Any?, property: Any?): T {
        if (!loaded) {
            value = readCachedValue()
        }
        loaded = true
        return value
    }

    operator fun setValue(thisRef: Any?, property: Any?, value: T) {
        this.value = value
        writeCachedValue(value)
    }
}