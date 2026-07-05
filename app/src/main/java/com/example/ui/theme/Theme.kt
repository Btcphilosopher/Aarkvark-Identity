package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BentoColorScheme = lightColorScheme(
    primary = AardvarkCyan,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF6750A4),
    onSecondary = Color.White,
    background = SlateBg,
    onBackground = TextWhite,
    surface = CardSlate,
    onSurface = TextWhite,
    surfaceVariant = CardSlateLight,
    onSurfaceVariant = TextGray,
    error = StatusRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Strictly apply the gorgeous Bento Grid light theme
    val colorScheme = BentoColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
