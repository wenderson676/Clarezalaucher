package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
    SYSTEM, LIGHT, DARK, OLED
}

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF031A15),
    primaryContainer = Color(0xFF07302A),
    onPrimaryContainer = NeonCyanLight,
    secondary = DarkTextSecondary,
    onSecondary = CyberNavyBackground,
    background = CyberVoidBackground,
    onBackground = DarkTextPrimary,
    surface = CyberSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = CyberBorderSubtle,
    outlineVariant = Color(0xFF1E2D3D)
)

private val OledColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF031A15),
    primaryContainer = Color(0xFF07302A),
    onPrimaryContainer = NeonCyanLight,
    secondary = DarkTextSecondary,
    onSecondary = NeutralBlack,
    background = NeutralBlack,
    onBackground = DarkTextPrimary,
    surface = Color(0xFF0A0E14),
    onSurface = DarkTextPrimary,
    surfaceVariant = Color(0xFF101620),
    onSurfaceVariant = DarkTextSecondary,
    outline = Color(0xFF1B2836),
    outlineVariant = Color(0xFF141F2C)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00897B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2F1),
    onPrimaryContainer = Color(0xFF004D40),
    secondary = LightTextSecondary,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorderTech,
    outlineVariant = LightDivider
)

@Composable
fun VidaSimplesTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val colorScheme = when (themeMode) {
        AppThemeMode.SYSTEM -> if (isSystemDark) DarkColorScheme else LightColorScheme
        AppThemeMode.LIGHT -> LightColorScheme
        AppThemeMode.DARK -> DarkColorScheme
        AppThemeMode.OLED -> OledColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Alias for compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    VidaSimplesTheme(
        themeMode = if (darkTheme) AppThemeMode.DARK else AppThemeMode.LIGHT,
        content = content
    )
}

