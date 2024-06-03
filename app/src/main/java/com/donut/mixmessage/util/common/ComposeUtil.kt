package com.donut.mixmessage.util.common

import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import com.donut.mixmessage.appScope
import com.donut.mixmessage.currentActivity
import com.donut.mixmessage.decode.LocalParentScroll
import com.donut.mixmessage.decode.image.CallBackListener
import com.donut.mixmessage.ui.component.common.MixDialogBuilder
import com.donut.mixmessage.ui.theme.MixMessageTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
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

suspend fun PointerInputScope.detectTransformGesturesEvent(
    panZoomLock: Boolean = false,
    onGesture: (centroid: Offset, pan: Offset, zoom: Float, rotation: Float) -> Boolean
) {
    awaitEachGesture {
        var rotation = 0f
        var zoom = 1f
        var pan = Offset.Zero
        var pastTouchSlop = false
        val touchSlop = viewConfiguration.touchSlop
        var lockedToPanZoom = false

        awaitFirstDown(requireUnconsumed = false)
        do {
            val event = awaitPointerEvent()
            val canceled = event.changes.fastAny { it.isConsumed }
            if (!canceled) {
                val zoomChange = event.calculateZoom()
                val rotationChange = event.calculateRotation()
                val panChange = event.calculatePan()

                if (!pastTouchSlop) {
                    zoom *= zoomChange
                    rotation += rotationChange
                    pan += panChange

                    val centroidSize = event.calculateCentroidSize(useCurrent = false)
                    val zoomMotion = abs(1 - zoom) * centroidSize
                    val rotationMotion = abs(rotation * PI.toFloat() * centroidSize / 180f)
                    val panMotion = pan.getDistance()

                    if (zoomMotion > touchSlop ||
                        rotationMotion > touchSlop ||
                        panMotion > touchSlop
                    ) {
                        pastTouchSlop = true
                        lockedToPanZoom = panZoomLock && rotationMotion < touchSlop
                    }
                }

                if (pastTouchSlop) {
                    var consume = false
                    val centroid = event.calculateCentroid(useCurrent = false)
                    val effectiveRotation = if (lockedToPanZoom) 0f else rotationChange
                    if (effectiveRotation != 0f ||
                        zoomChange != 1f ||
                        panChange != Offset.Zero
                    ) {
                        consume = onGesture(centroid, panChange, zoomChange, effectiveRotation)
                    }
                    event.changes.fastForEach {
                        if (consume && it.positionChanged()) {
                            it.consume()
                        }
                    }
                }
            }
        } while (!canceled && event.changes.fastAny { it.pressed })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZoomableView(
    callBackListener: CallBackListener,
    content: @Composable () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var size by remember { mutableStateOf(IntSize(0, 0)) }
    val scope = rememberCoroutineScope()
    val scrollState = LocalParentScroll.current

    callBackListener.callback = {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .onGloballyPositioned { coordinates ->
            size = coordinates.size
        }
        .pointerInput(Unit) {
            detectTransformGesturesEvent { _, pan, zoom, _ ->
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
                val moved = zoom != 1.0f || (prevY - offsetY).absoluteValue != 0f
                if (!moved) {
                    scope.launch {
                        scrollState.scrollBy(-pan.y)
                    }
                }
                moved
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .heightIn(0.dp, 1000.dp)
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
    TipText("文件地址: ${url.truncate(50)}") {
        MixDialogBuilder("复制地址到剪贴板?").apply {
            setDefaultNegative()
            setContent {
                Text(text = url)
            }
            setPositiveButton("确定") {
                url.copyToClipboard()
                closeDialog()
            }
            show()
        }
    }
}

