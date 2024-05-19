package com.donut.mixmessage

import android.app.Activity
import android.app.Application
import android.os.Looper
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.util.common.copyToClipboard
import com.donut.mixmessage.util.common.showError
import com.donut.mixmessage.util.objects.MixActivity
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.MainScope


val appScope by lazy { MainScope() }

lateinit var kv: MMKV

private lateinit var innerApp: Application


val currentActivity: Activity
    get() {
        return MixActivity.firstActiveActivity()!!
    }

val app: Application
    get() = innerApp

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            showError(e)
            if (Looper.myLooper() == null) {
                return@setDefaultUncaughtExceptionHandler
            }

            MixDialogBuilder("发生错误").apply {
                setContent {
                    Column(
                        modifier = Modifier
                            .heightIn(0.dp, 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = e.message ?: "未知错误",
                            color = Color.Red,
                            fontSize = 20.sp
                        )
                        Text(text = e.stackTraceToString())
                    }
                }
                setPositiveButton("复制错误信息") {
                    e.stackTraceToString().copyToClipboard()
                }
                setNegativeButton("关闭") {
                    closeDialog()
                }
                show()
            }
        }
        innerApp = this
        MMKV.initialize(this)
        kv = MMKV.defaultMMKV()
    }
}