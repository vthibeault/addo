package com.addo.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Calm, warm, low-stimulation palette. No alarm reds for overdue —
// urgency is communicated with words and position, not panic colors.
private val LightColors = lightColorScheme(
    primary = Color(0xFF2E6B5E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFBCEDE0),
    onPrimaryContainer = Color(0xFF00201A),
    secondary = Color(0xFF9A6A3B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDCBF),
    onSecondaryContainer = Color(0xFF2E1600),
    tertiary = Color(0xFF5B6236),
    background = Color(0xFFFBF9F4),
    onBackground = Color(0xFF1C1B18),
    surface = Color(0xFFFBF9F4),
    onSurface = Color(0xFF1C1B18),
    surfaceVariant = Color(0xFFE6E2D6),
    onSurfaceVariant = Color(0xFF49463E)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA0D1C4),
    onPrimary = Color(0xFF03372E),
    primaryContainer = Color(0xFF1D4E43),
    onPrimaryContainer = Color(0xFFBCEDE0),
    secondary = Color(0xFFF0BD8F),
    onSecondary = Color(0xFF4B2708),
    secondaryContainer = Color(0xFF7B4F26),
    onSecondaryContainer = Color(0xFFFFDCBF),
    tertiary = Color(0xFFC3CB94),
    background = Color(0xFF141311),
    onBackground = Color(0xFFE7E2DA),
    surface = Color(0xFF141311),
    onSurface = Color(0xFFE7E2DA),
    surfaceVariant = Color(0xFF49463E),
    onSurfaceVariant = Color(0xFFCAC6BA)
)

@Composable
fun AddoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
