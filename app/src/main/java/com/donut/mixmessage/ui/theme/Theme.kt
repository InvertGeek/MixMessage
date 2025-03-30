package com.donut.mixmessage.ui.theme

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.donut.mixmessage.util.common.cachedMutableOf


val LightColorScheme = lightColorScheme(
    primary = Color(0xFF24a0ed),
    secondary = Color(0xFF625b71),
    tertiary = Color(0xFF7D5260),
//    tertiaryContainer = Color(0xFFF0004E),
//    primaryContainer = Color(0xFFF0004E),
    secondaryContainer = Color(0x3662B5E8),
    background = Color(0xFFE6DFEB),
    surface = Color(0xFFE6DFEB),
    outline = Color(0xA624A0ED),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
)
val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF24a0ed),
)

var colorScheme by mutableStateOf(LightColorScheme)
var enableAutoDarkMode by mutableStateOf(true)
var currentTheme by cachedMutableOf(Theme.DEFAULT.name, "app_theme")

enum class Theme(
    val light: (context: Context) -> ColorScheme,
    val night: (context: Context) -> ColorScheme,
    val label: String,
) {
    DEFAULT({ LightColorScheme }, { DarkColorScheme }, "默认"),
    LEGACY({ lightColorScheme() }, { darkColorScheme() }, "原生"),

    DYNAMIC(
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dynamicLightColorScheme(it)
            } else {
                DEFAULT.light(it)
            }
        },
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dynamicDarkColorScheme(it)
            } else {
                DEFAULT.night(it)
            }
        },
        "跟随壁纸(需要安卓13+)"
    )
}

@Composable
fun MixMessageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val theme = Theme.valueOf(currentTheme)
    colorScheme =
        if (enableAutoDarkMode && darkTheme) theme.night(context) else theme.light(
            context
        )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            if (view.context !is Activity) {
                return@SideEffect
            }
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightNavigationBars = true
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}