package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LemonadeDarkColorScheme = darkColorScheme(
    primary = LemonYellow,
    onPrimary = Color(0xFF1E1E1E),
    primaryContainer = Color(0xFF352B00),
    onPrimaryContainer = LemonYellow,
    secondary = NeonLime,
    onSecondary = Color(0xFF132000),
    secondaryContainer = Color(0xFF1F3500),
    onSecondaryContainer = NeonLime,
    tertiary = RobloxBlue,
    onTertiary = Color.White,
    background = StudioDarkBg,
    onBackground = TextPrimary,
    surface = StudioCardBg,
    onSurface = TextPrimary,
    surfaceVariant = StudioCardHover,
    onSurfaceVariant = TextSecondary,
    outline = StudioBorder,
    error = RobloxRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LemonadeDarkColorScheme,
        typography = Typography,
        content = content
    )
}
