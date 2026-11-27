package com.smaran.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// High-contrast palette: body text meets WCAG AAA (7:1) on its background in both themes.
// Dynamic (wallpaper) colour is deliberately not used, so contrast stays predictable.

internal val LightColors = lightColorScheme(
    primary = Color(0xFF0B4F9C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E6FF),
    onPrimaryContainer = Color(0xFF00214A),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF111111),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = Color(0xFF333333),
    outline = Color(0xFF5A5A5A),
    error = Color(0xFFA4000F),
    onError = Color(0xFFFFFFFF),
)

internal val DarkColors = darkColorScheme(
    primary = Color(0xFF9CC7FF),
    onPrimary = Color(0xFF002B5C),
    primaryContainer = Color(0xFF0B4F9C),
    onPrimaryContainer = Color(0xFFFFFFFF),
    background = Color(0xFF121212),
    onBackground = Color(0xFFF2F2F2),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFF2F2F2),
    surfaceVariant = Color(0xFF2A2C30),
    onSurfaceVariant = Color(0xFFE0E0E0),
    outline = Color(0xFFB0B0B0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)
