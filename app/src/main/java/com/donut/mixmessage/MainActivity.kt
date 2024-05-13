package com.donut.mixmessage

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cn.vove7.andro_accessibility_api.AccessibilityApi
import com.donut.mixmessage.service.MixAccessibilityService
import com.donut.mixmessage.ui.component.NavComponent
import com.donut.mixmessage.ui.component.common.CommonColumn
import com.donut.mixmessage.ui.component.routes.settings.START_BLANK_SCREEN
import com.donut.mixmessage.ui.theme.MixMessageTheme
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.objects.MixActivity
import kotlinx.coroutines.DelicateCoroutinesApi


class MainActivity : MixActivity() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Activity
    }

    // 检查悬浮窗权限
    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent =
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
        }
    }

    // 检查无障碍权限
    private fun checkAccessibilityPermission() {
        if (!isAccessibilityServiceEnabled()) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
    }

    // 检查无障碍服务是否已启用
    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityService = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return accessibilityService?.contains(packageName) == true
    }

    override fun onResume() {
        checkOverlayPermission()
//        checkAccessibilityPermission()
        super.onResume()
    }


    // 处理权限请求结果
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        AccessibilityApi.init(
            this,
            MixAccessibilityService::class.java
        )

        setContent {
            var scaled by remember {
                mutableStateOf(false)
            }
            MixMessageTheme {
                Surface(
                    modifier = Modifier
                        .systemBarsPadding()
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
//                                debug("pan: $pan, zoom: $zoom")
                                if (zoom > 1 && !scaled && START_BLANK_SCREEN) {
                                    scaled = true
                                    showToast("解锁成功")
                                }
                            }
                        },
//                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!START_BLANK_SCREEN || scaled) {
                        MainPage()
                    }
                }
            }
        }

    }
}

@Composable
fun LockScreenOrientation(orientation: Int) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context.findActivity() ?: return@DisposableEffect onDispose {}
        val originalOrientation = activity.requestedOrientation
        activity.requestedOrientation = orientation
        onDispose {
            // restore original orientation when view disappears
            activity.requestedOrientation = originalOrientation
        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(DelicateCoroutinesApi::class, ExperimentalLayoutApi::class)
@Composable
fun MainPage() {

    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

    CommonColumn(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        NavComponent()

    }
}