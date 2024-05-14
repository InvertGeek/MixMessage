package com.donut.mixmessage.util.common

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import com.donut.mixmessage.currentActivity
import com.donut.mixmessage.ui.theme.MixMessageTheme

fun addContentView(view: ComposeView): () -> Unit {
    currentActivity.addContentView(
        view,
        ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    )
    return {
        view.removeView()
    }
}

fun ComposeView.removeView() {
    (this.parent as ViewGroup).removeView(this)
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
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val view = ComposeView(currentActivity)
        addContentView(view).also {
            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
            it()
        }
    }
}

