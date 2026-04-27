package com.loki.deni.presentation.ui.theme

import android.app.Activity
import android.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorPalette = darkColorScheme(
    primary = DeniPrimary,
    onPrimary = DeniOnPrimary,
    secondary = DeniAccent,
    background = DeniDarkBackground,
    surface = DeniDarkSurface,
    onBackground = DeniTextLight,
    onSurface = DeniTextLight,
    error = DeniError,
)

private val LightColorPalette = lightColorScheme(
    primary = DeniPrimary,
    onPrimary = DeniOnPrimary,
    secondary = DeniAccent,
    background = DeniLightBackground,
    surface = DeniLightSurface,
    onBackground = DeniTextDark,
    onSurface = DeniTextDark,
    error = DeniError,
)

@Composable
fun DeniTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = colors.surface.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
