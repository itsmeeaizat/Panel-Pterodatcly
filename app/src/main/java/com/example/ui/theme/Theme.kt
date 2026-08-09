package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PurplePteroPrimary,
    onPrimary = Color.White,
    primaryContainer = PurplePteroOnContainer,
    onPrimaryContainer = PurplePteroContainer,
    secondary = PurplePteroPrimary,
    onSecondary = Color.White,
    secondaryContainer = Slate800,
    onSecondaryContainer = Slate200,
    tertiary = Emerald500,
    background = Slate950,
    onBackground = Slate200,
    surface = Slate900,
    onSurface = Slate200,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    outline = Slate700,
    error = Rose500
)

private val LightColorScheme = lightColorScheme(
    primary = CleanSkyPrimary,
    onPrimary = CleanSkyOnPrimary,
    primaryContainer = CleanSkyContainer,
    onPrimaryContainer = CleanSkyOnContainer,
    secondary = CleanSkyPrimary,
    onSecondary = Color.White,
    tertiary = Emerald500,
    background = CleanBg,
    onBackground = CleanTextMain,
    surface = CleanCardBg,
    onSurface = CleanTextMain,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = CleanTextMuted,
    outline = CleanCardBorder,
    error = Rose500
)

@Composable
fun PterodactylTheme(
    darkTheme: Boolean = false, // Clean Light Theme default
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

