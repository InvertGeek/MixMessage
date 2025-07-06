package com.donut.mixmessage.util.common

import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import com.donut.mixmessage.appScope
import com.donut.mixmessage.kv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.parcelize.Parcelize

fun <T> constructCachedMutableValue(
    value: T,
    setVal: (value: T) -> Unit,
    getVal: () -> T,
) =
    object : CachedMutableValue<T>(value) {
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
        { kv.encode(key, it) },
        { kv.decodeString(key, value)!! })

fun cachedMutableOf(value: Boolean, key: String) =
    constructCachedMutableValue(value, { kv.encode(key, it) }, { kv.decodeBool(key, value) })

fun cachedMutableOf(value: Long, key: String) =
    constructCachedMutableValue(value, { kv.encode(key, it) }, { kv.decodeLong(key, value) })


@Parcelize
data class ParcelableItemList<T : Parcelable>(
    val items: List<T>
) : Parcelable

inline fun <reified T : Parcelable> cachedMutableOf(value: List<T>, key: String) =
    constructCachedMutableValue(
        value,
        { kv.encode(key, ParcelableItemList(it)) },
        getter@{
            val data =
                kv.decodeParcelable(key, ParcelableItemList::class.java) ?: return@getter value
            @Suppress("UNCHECKED_CAST")
            return@getter data.items as List<T>
        }
    )

fun cachedMutableOf(value: Parcelable, key: String) =
    constructCachedMutableValue(
        value,
        { kv.encode(key, it) },
        { kv.decodeParcelable(key, value.javaClass) })


fun cachedMutableOf(value: Set<String>, key: String) =
    constructCachedMutableValue(
        value,
        { kv.encode(key, it) },
        { kv.decodeStringSet(key, value)!! },
    )


abstract class CachedMutableValue<T>(
    @Volatile
    private var value: T,
) {
    private var loaded = false
    private val mutex = Mutex()
    private var stateValue by mutableLongStateOf(0)

    abstract fun readCachedValue(): T

    abstract fun writeCachedValue(value: T)

    operator fun getValue(thisRef: Any?, property: Any?): T {
        synchronized(this) {
            if (!loaded) {
                value = readCachedValue()
                loaded = true
            }
            stateValue
            return value
        }
    }


    operator fun setValue(thisRef: Any?, property: Any?, value: T) {
        synchronized(this) {
            if (this.value == value) {
                return
            }
            stateValue++
            this.value = value
            appScope.launch(Dispatchers.IO) {
                mutex.withLock {
                    writeCachedValue(this@CachedMutableValue.value)
                }
            }
        }
    }
}