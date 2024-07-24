package com.donut.mixmessage.util.common

import android.annotation.SuppressLint
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.donut.mixmessage.appScope
import com.donut.mixmessage.currentActivity
import com.donut.mixmessage.ui.theme.MixMessageTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.toggleScale
import net.engawapg.lib.zoomable.zoomable

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZoomableView(
    content: @Composable () -> Unit
) {
    val zoomState = rememberZoomState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .heightIn(0.dp, 1000.dp)
            .zoomable(zoomState, onDoubleTap = {
                zoomState.toggleScale(1.0f, it)
            })
    ) {
        content()
    }
}

var ENABLE_HAPTIC_FEEDBACK by cachedMutableOf(true, "enable_haptic_feedback")
fun performHapticFeedBack(cd: Long = 400L) {
    ENABLE_HAPTIC_FEEDBACK.isFalse {
        return
    }
    withCd("haptic_feedback", cd) {
        val view = ComposeView(currentActivity)
        addContentView(view).also {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            it()
        }
    }
}

@Composable
fun TipText(content: String, onClick: () -> Unit = {}) {
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
fun PasswordContent(password: String) {
    TipText(
        "此文件使用密钥: $password 加密"
    ) {

    }
}

@Composable
@NonRestartableComposable
fun UseEffect(
    vararg keys: Any?,
    block: suspend CoroutineScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(*keys) {
        scope.launch(Dispatchers.IO, block = block)
    }
}

@Composable
@NonRestartableComposable
fun UseEffect(
    block: suspend CoroutineScope.() -> Unit
) {
    UseEffect(Unit, block = block)
}

@Composable
fun screenWidthInDp(): Dp {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    return screenWidthDp.dp
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewComponent(url: String) {
    GenWebViewClient {
        loadUrl(url)
    }
}


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GenWebViewClient(modifier: Modifier = Modifier, block: WebView.() -> Unit) =
    AndroidView(factory = { context ->
        WebView(context).apply {
            webViewClient = WebViewClient()
            settings.apply {
                domStorageEnabled = true
                javaScriptEnabled = true
                allowUniversalAccessFromFileURLs = true
                allowContentAccess = true
                allowFileAccessFromFileURLs = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            block()
        }
    }, modifier = modifier)

