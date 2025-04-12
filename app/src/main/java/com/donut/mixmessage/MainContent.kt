package com.donut.mixmessage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.donut.mixmessage.service.IS_ACS_ENABLED
import com.donut.mixmessage.ui.component.common.CommonColumn
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.ui.component.nav.NavComponent
import com.donut.mixmessage.ui.component.routes.password.LOCK_CACHE
import com.donut.mixmessage.ui.component.routes.password.Unlock
import com.donut.mixmessage.ui.component.routes.settings.CALCULATOR_LOCK
import com.donut.mixmessage.ui.component.routes.settings.START_BLANK_SCREEN
import com.donut.mixmessage.ui.theme.MixMessageTheme
import com.donut.mixmessage.util.common.OnDispose
import com.donut.mixmessage.util.common.isFalseAnd
import com.donut.mixmessage.util.common.isTrue
import com.donut.mixmessage.util.common.showToast
import com.teamb.calculator.CalculatorContent


var visible by mutableStateOf(false)

var lastDestination by mutableStateOf("")

@Composable
fun MainContent() {

    val showContent = (!START_BLANK_SCREEN && !CALCULATOR_LOCK) || visible

    OnDispose {
        visible = false
        lastDestination = ""
    }

    LaunchedEffect(showContent) {
        IS_ACS_ENABLED.isFalseAnd(ACS_NOTIFY && showContent) {
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
                    currentActivity.checkAccessibilityPermission()
                    closeDialog()
                }
                show()
            }
        }
    }
    MixMessageTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
        ) {
            AnimatedVisibility(
                visible = !visible,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                if (CALCULATOR_LOCK) {
                    CalculatorLock()
                    return@AnimatedVisibility
                }
                if (START_BLANK_SCREEN) {
                    ScaleLock()
                    return@AnimatedVisibility
                }
            }
            AnimatedVisibility(
                visible = showContent,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it }
            ) {
                MainPage()
                return@AnimatedVisibility
            }
        }
    }

}

@Composable
fun ScaleLock() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    if (zoom > 1 && !visible) {
                        visible = true
                        showToast("解锁成功")
                    }
                }
            }
    )
}

@Composable
fun CalculatorLock() {
    CalculatorContent {
        if (it.contentEquals("66/66")) {
            showToast("解锁成功")
            visible = true
        }
    }
}

@Composable
fun MainPage() {
    CommonColumn(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (LOCK_CACHE.isNotEmpty()) {
            Unlock()
            return@CommonColumn
        }
        NavComponent()
    }
}