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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import com.donut.mixmessage.util.common.performHapticFeedBack
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.objects.MixActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import java.lang.ref.WeakReference


class MainActivity : MixActivity(MAIN_ID) {

    override fun onResume() {
        checkOverlayPermission()
//        checkAccessibilityPermission()
        super.onResume()
    }

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


    // 处理权限请求结果

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                                if (zoom > 1 && !scaled && START_BLANK_SCREEN) {
                                    scaled = true
                                    performHapticFeedBack()
                                    showToast("解锁成功")
                                }
                            }
                        },

                    ) {
                    AnimatedVisibility(
                        visible = !START_BLANK_SCREEN || scaled,
                        enter = slideInVertically(),
                        exit = slideOutVertically()
                    ) {
                        MainPage()
                    }
                }
            }
        }

    }
}