package com.beautymirror.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Pink40,
    onPrimary = ColorWhite,
    secondary = Peach40,
    onSecondary = ColorWhite,
    tertiary = Peach80,
    onTertiary = ColorWhite,
    background = ColorWhite,
    surface = ColorWhite,
    onSurface = ColorDark,
)

private val DarkColorScheme = darkColorScheme(
    primary = Pink80,
    onPrimary = ColorDark,
    secondary = Peach80,
    onSecondary = ColorDark,
    tertiary = Pink40,
    onTertiary = ColorWhite,
    background = ColorDark,
    surface = ColorDark,
    onSurface = ColorWhite,
)

@Composable
fun BeautyMirrorTheme(
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
