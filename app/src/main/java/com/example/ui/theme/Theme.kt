package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = ElectricViolet,
    onPrimaryContainer = Color.White,
    secondary = NeonPurple,
    onSecondary = Color.White,
    tertiary = CyberPink,
    background = DarkBackground,
    onBackground = Color.White,
    surface = DarkCardSurface,
    onSurface = Color.White,
    surfaceVariant = DarkCardSurface,
    onSurfaceVariant = Color(0xFFB0B3C6),
    outline = DarkSurfaceGlassBorder
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricViolet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8DEF8),
    onPrimaryContainer = Color(0xFF1D192B),
    secondary = NeonBlue,
    onSecondary = Color.White,
    tertiary = CyberPink,
    background = LightBackground,
    onBackground = Color(0xFF1A1C2E),
    surface = LightCardSurface,
    onSurface = Color(0xFF1A1C2E),
    surfaceVariant = Color(0xFFE5E7F2),
    onSurfaceVariant = Color(0xFF44475A),
    outline = LightSurfaceGlassBorder
)

@Composable
fun NovaXAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

