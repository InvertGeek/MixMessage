package com.donut.mixmessage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.donut.mixmessage.ui.component.common.CommonColumn
import com.donut.mixmessage.ui.component.nav.NavComponent
import com.donut.mixmessage.ui.component.routes.password.LOCK_CACHE
import com.donut.mixmessage.ui.component.routes.password.Unlock
import com.donut.mixmessage.ui.component.routes.settings.CALCULATOR_LOCK
import com.donut.mixmessage.ui.component.routes.settings.START_BLANK_SCREEN
import com.donut.mixmessage.ui.theme.MixMessageTheme
import com.donut.mixmessage.util.common.Provide
import com.donut.mixmessage.util.common.performHapticFeedBack
import com.donut.mixmessage.util.common.showToast
import com.teamb.calculator.CalculatorContent


val LocalLockVisible = compositionLocalOf { mutableStateOf(false) }
val LocalDestination = compositionLocalOf { mutableStateOf("") }

@Composable
fun MainContent() {
    val visible = remember {
        mutableStateOf(false)
    }
    val destination = remember {
        mutableStateOf("")
    }
    LocalDestination.Provide(value = destination) {
        LocalLockVisible.Provide(value = visible) {
            MixMessageTheme {
                Surface(
                    modifier = Modifier
                        .systemBarsPadding()
                        .fillMaxSize()
                ) {

                    AnimatedVisibility(
                        visible = !visible.value,
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
                        visible = (!START_BLANK_SCREEN && !CALCULATOR_LOCK) || visible.value,
                        enter = slideInVertically { -it },
                        exit = slideOutVertically { -it }
                    ) {
                        MainPage()
                        return@AnimatedVisibility
                    }
                }
            }
        }
    }
}

@Composable
fun ScaleLock() {
    val visible = LocalLockVisible.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    if (zoom > 1 && !visible.value) {
                        visible.value = true
                        performHapticFeedBack()
                        showToast("解锁成功")
                    }
                }
            }
    )
}

@Composable
fun CalculatorLock() {
    val visible = LocalLockVisible.current
    CalculatorContent {
        if (it.contentEquals("66/66")) {
            showToast("解锁成功")
            visible.value = true
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