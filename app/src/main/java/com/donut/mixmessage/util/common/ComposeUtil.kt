package com.donut.mixmessage.util.common

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import com.donut.mixmessage.currentActivity
import com.donut.mixmessage.ui.theme.MixMessageTheme

fun addComposeView(content: @Composable () -> Unit) {
    currentActivity.addContentView(
        ComposeView(currentActivity).apply {
            setContent {
                MixMessageTheme {
                    content()
                }
            }
        },
        ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    )
}