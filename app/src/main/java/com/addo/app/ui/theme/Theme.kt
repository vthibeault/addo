package com.addo.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Candy, not corporate: warm cream paper, coral + sunshine + mint + grape.
// One deliberate palette instead of whatever the wallpaper says.

object AddoColors {
    val coral = Color(0xFFFF6B57)
    val sunshine = Color(0xFFFFB020)
    val mint = Color(0xFF2EC4B6)
    val grape = Color(0xFF8B5CF6)
    val bubblegum = Color(0xFFFF7BAC)

    // Brighter siblings for dark mode
    val coralBright = Color(0xFFFF8A78)
    val sunshineBright = Color(0xFFFFC94D)
    val mintBright = Color(0xFF4FD8CB)
    val grapeBright = Color(0xFFA78BFA)
}

private val LightColors = lightColorScheme(
    primary = AddoColors.coral,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFE1DB),
    onPrimaryContainer = Color(0xFF6B1D10),
    secondary = AddoColors.sunshine,
    onSecondary = Color(0xFF402E00),
    secondaryContainer = Color(0xFFFFE9B8),
    onSecondaryContainer = Color(0xFF4C3900),
    tertiary = AddoColors.mint,
    onTertiary = Color(0xFF00332F),
    tertiaryContainer = Color(0xFFC7F4EF),
    onTertiaryContainer = Color(0xFF00423D),
    background = Color(0xFFFFF6EC),
    onBackground = Color(0xFF33223A),
    surface = Color(0xFFFFFCF6),
    onSurface = Color(0xFF33223A),
    surfaceVariant = Color(0xFFFCEBD9),
    onSurfaceVariant = Color(0xFF77607F),
    outline = Color(0xFFDCC7BB),
    error = Color(0xFFC2404B),
    onError = Color(0xFFFFFFFF)
)

private val DarkColors = darkColorScheme(
    primary = AddoColors.coralBright,
    onPrimary = Color(0xFF4A130A),
    primaryContainer = Color(0xFF7A2A1D),
    onPrimaryContainer = Color(0xFFFFE1DB),
    secondary = AddoColors.sunshineBright,
    onSecondary = Color(0xFF3A2A00),
    secondaryContainer = Color(0xFF6B4E00),
    onSecondaryContainer = Color(0xFFFFE9B8),
    tertiary = AddoColors.mintBright,
    onTertiary = Color(0xFF00332F),
    tertiaryContainer = Color(0xFF00504A),
    onTertiaryContainer = Color(0xFFC7F4EF),
    background = Color(0xFF221733),
    onBackground = Color(0xFFF4EAFB),
    surface = Color(0xFF2C1F40),
    onSurface = Color(0xFFF4EAFB),
    surfaceVariant = Color(0xFF3B2B52),
    onSurfaceVariant = Color(0xFFCBB6DC),
    outline = Color(0xFF5C4870),
    error = Color(0xFFFF8791),
    onError = Color(0xFF4A0E15)
)

private val AddoShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun AddoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AddoTypography,
        shapes = AddoShapes,
        content = content
    )
}
