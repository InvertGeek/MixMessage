package com.donut.mixmessage.util.common

import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donut.mixmessage.appScope
import com.donut.mixmessage.currentActivity
import com.donut.mixmessage.decode.LocalParentScroll
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.ui.theme.MixMessageTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

fun addContentView(view: View): () -> Unit {
    currentActivity.addContentView(
        view,
        ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    )
    return {
        appScope.launch(Dispatchers.Main) {
            view.removeView()
        }
    }
}

fun View.removeView() {
    this.parent.isNotNull {
        (it as ViewGroup).removeView(this)
    }
}

@Composable
fun OnDispose(block: () -> Unit) {
    DisposableEffect(Unit) {
        onDispose {
            block()
        }
    }
}

fun addComposeView(content: @Composable (removeView: () -> Unit) -> Unit): () -> Unit {
    return addContentView(
        ComposeView(currentActivity).apply {
            setContent {
                MixMessageTheme {
                    content {
                        this.removeView()
                    }
                }
            }
        }
    )
}

@Composable
fun <T> ProvidableCompositionLocal<T>.Provide(value: T, content: @Composable () -> Unit) {
    CompositionLocalProvider(this provides value) {
        content()
    }
}

@Composable
fun ZoomableView(content: @Composable () -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var size by remember { mutableStateOf(IntSize(0, 0)) }
    val scope = rememberCoroutineScope()
    val scrollState = LocalParentScroll.current

    Box(modifier = Modifier
        .fillMaxSize()
        .onGloballyPositioned { coordinates ->
            size = coordinates.size
        }
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                val prevY = offsetY
                scale *= zoom
                offsetX += pan.x
                offsetY += pan.y
                scale = scale
                    .coerceAtLeast(1f)
                    .coerceAtMost(10f)
                val maxX = (size.width * (scale - 1)) / 2
                val maxY = (size.height * (scale - 1)) / 2
                offsetX = offsetX
                    .coerceAtMost(maxX)
                    .coerceAtLeast(-maxX)
                offsetY = offsetY
                    .coerceAtMost(maxY)
                    .coerceAtLeast(-maxY)
                if (zoom == 1.0f && (prevY - offsetY).absoluteValue == 0f) {
                    scope.launch {
                        scrollState.scrollTo((scrollState.value - pan.y).roundToInt())
                    }
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .scale(scale)
        ) {
            content()
        }
    }
}

var lastHapticFeedBackTime = 0L

var ENABLE_HAPTIC_FEEDBACK by cachedMutableOf(true, "enable_haptic_feedback")
fun performHapticFeedBack(cd: Long = 400L) {
    if (ENABLE_HAPTIC_FEEDBACK.isFalse()) {
        return
    }
    if (System.currentTimeMillis() - lastHapticFeedBackTime < cd) {
        return
    }

    lastHapticFeedBackTime = System.currentTimeMillis()

    val view = ComposeView(currentActivity)
    addContentView(view).also {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        it()
    }

}

@Composable
fun TipText(content: String, onClick: () -> Unit) {
    Text(
        text = content,
        color = Color.Gray,
        style = TextStyle(
            fontSize = 10.sp,
            lineHeight = 12.sp
        ),
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(10.dp),
    )
}

@Composable
fun UrlContent(url: String) {
    TipText("文件地址: $url") {
        MixDialogBuilder("复制地址到剪贴板?").apply {
            setDefaultNegative()
            setPositiveButton("确定") {
                url.copyToClipboard()
                closeDialog()
            }
            show()
        }
    }
}

