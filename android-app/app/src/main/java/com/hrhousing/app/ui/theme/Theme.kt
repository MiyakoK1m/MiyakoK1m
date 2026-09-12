package com.hrhousing.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BluePrimary = Color(0xFF1E3A5F)
val BlueLight = Color(0xFF3B6EA5)
val AccentTeal = Color(0xFF0F9D8B)
val WarnRed = Color(0xFFD32F2F)
val WarnAmber = Color(0xFFF59E0B)

private val LightColors = lightColorScheme(
    primary = BluePrimary,
    secondary = AccentTeal,
    error = WarnRed,
)

private val DarkColors = darkColorScheme(
    primary = BlueLight,
    secondary = AccentTeal,
    error = Color(0xFFEF9A9A),
)

@Composable
fun HrHousingTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, typography = MaterialTheme.typography, content = content)
}

/** Heat-map intensity scale used on the Дэшборд, from least to most occupied (5 steps). */
fun heatColor(step: Int, darkTheme: Boolean): Color {
    val lightSteps = listOf(
        Color(0xFFE0E0E0), Color(0xFFB8C9DE), Color(0xFF7FA3C9), Color(0xFF4472A8), Color(0xFF1E3A5F),
    )
    val darkSteps = listOf(
        Color(0xFF3A3A3A), Color(0xFF32476B), Color(0xFF2C5A8A), Color(0xFF2E76B6), Color(0xFF5FA8E8),
    )
    val steps = if (darkTheme) darkSteps else lightSteps
    return steps[step.coerceIn(0, steps.size - 1)]
}
