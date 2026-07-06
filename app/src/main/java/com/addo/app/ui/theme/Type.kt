package com.addo.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.addo.app.R

// Fredoka: chubby and round, for anything loud.
// Nunito: soft and friendly, for everything you actually read.

@OptIn(ExperimentalTextApi::class)
private fun variableFont(resId: Int, weight: FontWeight) = Font(
    resId = resId,
    weight = weight,
    variationSettings = FontVariation.Settings(weight, FontStyle.Normal)
)

val Fredoka = FontFamily(
    variableFont(R.font.fredoka, FontWeight.Normal),
    variableFont(R.font.fredoka, FontWeight.Medium),
    variableFont(R.font.fredoka, FontWeight.SemiBold),
    variableFont(R.font.fredoka, FontWeight.Bold)
)

val Nunito = FontFamily(
    variableFont(R.font.nunito, FontWeight.Normal),
    variableFont(R.font.nunito, FontWeight.Medium),
    variableFont(R.font.nunito, FontWeight.SemiBold),
    variableFont(R.font.nunito, FontWeight.Bold),
    variableFont(R.font.nunito, FontWeight.ExtraBold)
)

val AddoTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 56.sp
    ),
    displayMedium = TextStyle(
        fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 46.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 28.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 26.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Fredoka, fontWeight = FontWeight.Medium, fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 23.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 21.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 13.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, letterSpacing = 0.4.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 12.5.sp
    )
)
