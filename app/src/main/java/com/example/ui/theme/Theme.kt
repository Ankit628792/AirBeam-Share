package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DarkCyberIndigo,
    onPrimary = Color.White,
    primaryContainer = DarkIndigoContainer,
    onPrimaryContainer = DarkOnIndigoContainer,
    secondary = DarkNeonCyan,
    onSecondary = Color.Black,
    secondaryContainer = DarkNeonCyanContainer,
    onSecondaryContainer = DarkNeonCyan,
    tertiary = DarkEmeraldGreen,
    onTertiary = Color.Black,
    background = DarkObsidianBackground,
    onBackground = DarkTextPrimary,
    surface = DarkObsidianSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkObsidianVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkVectorBorder,
    error = DarkRose,
    onError = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = LightCyberIndigo,
    onPrimary = Color.White,
    primaryContainer = LightIndigoContainer,
    onPrimaryContainer = LightOnIndigoContainer,
    secondary = LightNeonCyan,
    onSecondary = Color.White,
    secondaryContainer = LightNeonCyanContainer,
    onSecondaryContainer = LightNeonCyan,
    tertiary = LightEmeraldGreen,
    onTertiary = Color.White,
    background = LightSoftBackground,
    onBackground = LightTextPrimary,
    surface = LightSoftSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSoftVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightVectorBorder,
    error = LightRose,
    onError = Color.White
)

@Composable
fun AirBeamTheme(
    darkTheme: Boolean = true, // Default to dark theme as requested
    content: @Composable () -> Unit
) {
    val airBeamColors = if (darkTheme) DarkAirBeamColors else LightAirBeamColors
    val materialColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalAirBeamColors provides airBeamColors) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = Typography,
            content = content
        )
    }
}
