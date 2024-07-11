package com.teamb.calculator.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun CalculatorButton(
    symbol: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    ElevatedButton(
        modifier = modifier,
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        shape = CircleShape
    ) {
        Text(text = symbol, style = MaterialTheme.typography.displaySmall)
    }
}

