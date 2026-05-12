package com.example.nammamistri.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = White,
    background = BgLight,
    surface = White,
    onBackground = TextDark,
    onSurface = TextDark,
)

@Composable
fun NammaMistriTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}