package com.donut.mixmessage

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.donut.mixmessage.service.IS_ACS_ENABLED
import com.donut.mixmessage.ui.component.common.CommonColumn
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.ui.component.nav.NavComponent
import com.donut.mixmessage.ui.component.routes.settings.START_BLANK_SCREEN
import com.donut.mixmessage.ui.theme.MixMessageTheme
import com.donut.mixmessage.util.common.isFalse
import com.donut.mixmessage.util.common.isTrue
import com.donut.mixmessage.util.common.performHapticFeedBack
import com.donut.mixmessage.util.common.showToast
import com.donut.mixmessage.util.objects.MixActivity
import com.donut.mixmessage.util.objects.MixFileSelector


class MainActivity : MixActivity(MAIN_ID) {

    override fun onResume() {
        checkOverlayPermission()
        super.onResume()
        IS_ACS_ENABLED.isFalse {
            MixDialogBuilder("提示").apply {
                setContent {
                    Text(text = "无障碍权限未开启,是否进入设置?")
                    LaunchedEffect(IS_ACS_ENABLED) {
                        IS_ACS_ENABLED.isTrue {
                            closeDialog()
                        }
                    }
                }
                setDefaultNegative()
                setPositiveButton("确定") {
                    checkAccessibilityPermission()
                    closeDialog()
                }
                show()
            }
        }
    }

    @Composable
    fun MainPage() {

        CommonColumn(
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            NavComponent()

        }
    }

    companion object {
        lateinit var mixFileSelector: MixFileSelector
    }

    override fun onDestroy() {
        super.onDestroy()
        mixFileSelector.unregister()
    }


    // 处理权限请求结果

    override fun onCreate(savedInstanceState: Bundle?) {
        mixFileSelector = MixFileSelector(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                        enter = slideInVertically { -it },
                        exit = slideOutVertically { -it }
                    ) {
                        MainPage()
                    }
                }
            }
        }

    }
}