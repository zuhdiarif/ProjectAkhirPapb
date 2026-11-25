package com.zuhdiarifazizi.projectakhirpapb.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.zuhdiarifazizi.projectakhirpapb.R

val PoppinsFontFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

val AuraTitleTextStyle = TextStyle(
    fontFamily = PoppinsFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 36.sp
)

val AuraButtonTextStyle = TextStyle(
    fontFamily = PoppinsFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 28.sp
)

val AuraBodyTextStyle = TextStyle(
    fontFamily = PoppinsFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 18.sp,
    lineHeight = 26.sp
)

val AuraHelperTextStyle = TextStyle(
    fontFamily = PoppinsFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 22.sp
)

val Typography = Typography(
    displayLarge = AuraTitleTextStyle,
    displayMedium = AuraTitleTextStyle,
    displaySmall = AuraTitleTextStyle,
    headlineLarge = AuraTitleTextStyle,
    headlineMedium = AuraTitleTextStyle,
    headlineSmall = AuraTitleTextStyle,
    titleLarge = AuraTitleTextStyle,
    titleMedium = AuraButtonTextStyle,
    titleSmall = AuraHelperTextStyle.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = AuraBodyTextStyle,
    bodyMedium = AuraHelperTextStyle,
    bodySmall = AuraHelperTextStyle,
    labelLarge = AuraButtonTextStyle,
    labelMedium = AuraHelperTextStyle,
    labelSmall = AuraHelperTextStyle
)
