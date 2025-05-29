package com.naulify.agent.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Custom colors
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Naulify brand colors
val NaulifyBlue = Color(0xFF2196F3)
val NaulifyDarkBlue = Color(0xFF1976D2)
val NaulifyLightBlue = Color(0xFF64B5F6)
val NaulifySurface = Color(0xFFF5F5F5)
val NaulifyError = Color(0xFFB00020)

private val DarkColorScheme = darkColorScheme(
    primary = NaulifyBlue,
    secondary = NaulifyLightBlue,
    tertiary = NaulifyDarkBlue,
    surface = Color(0xFF121212),
    error = NaulifyError
)

private val LightColorScheme = lightColorScheme(
    primary = NaulifyBlue,
    secondary = NaulifyLightBlue,
    tertiary = NaulifyDarkBlue,
    surface = NaulifySurface,
    error = NaulifyError
)

@Composable
fun NaulifyTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
