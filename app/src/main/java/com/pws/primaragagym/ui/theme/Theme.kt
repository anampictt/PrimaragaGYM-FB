package com.pws.primaragagym.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = GreenSurface,
    onPrimaryContainer = TextOnPrimary,
    secondary = GreenPrimaryLight,
    onSecondary = TextOnPrimary,
    secondaryContainer = GreenPrimaryDark,
    onSecondaryContainer = TextOnPrimary,
    tertiary = GreenPrimaryLight,
    onTertiary = TextOnPrimary,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = Error,
    onError = TextOnPrimary,
    errorContainer = ErrorDark,
    onErrorContainer = TextOnPrimary,
    outline = BorderLight,
    outlineVariant = DividerLight,
    scrim = DarkBackground
)

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GreenPrimaryLight.copy(alpha = 0.1f),
    onPrimaryContainer = GreenPrimaryDark,
    secondary = GreenPrimaryLight,
    onSecondary = Color.White,
    secondaryContainer = GreenPrimaryLight.copy(alpha = 0.15f),
    onSecondaryContainer = GreenPrimaryDark,
    tertiary = GreenPrimaryLight,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = TextPrimaryDark,
    surface = LightSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = LightSurface,
    onSurfaceVariant = TextSecondaryDark,
    error = Error,
    onError = Color.White,
    errorContainer = Error.copy(alpha = 0.1f),
    onErrorContainer = ErrorDark,
    outline = BorderDark,
    outlineVariant = DividerDark,
    scrim = Color.Black.copy(alpha = 0.32f)
)

@Composable
fun PrimaragagymTheme(
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
