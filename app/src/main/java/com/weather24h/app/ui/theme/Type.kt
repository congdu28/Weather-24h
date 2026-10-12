package com.weather24h.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.weather24h.app.R

val TikTokSans = FontFamily(
    Font(R.font.tiktoksans_regular, FontWeight.Normal),
    Font(R.font.tiktoksans_medium, FontWeight.Medium),
    Font(R.font.tiktoksans_semibold, FontWeight.SemiBold),
    Font(R.font.tiktoksans_bold, FontWeight.Bold)
)

val Typography = Typography(
    displayLarge = TextStyle(fontFamily = TikTokSans, fontWeight = FontWeight.Bold, fontSize = 52.sp, lineHeight = 58.sp),
    displayMedium = TextStyle(fontFamily = TikTokSans, fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 46.sp),
    displaySmall = TextStyle(fontFamily = TikTokSans, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp),
    headlineLarge = TextStyle(fontFamily = TikTokSans, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 34.sp),
    headlineMedium = TextStyle(fontFamily = TikTokSans, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp),
    headlineSmall = TextStyle(fontFamily = TikTokSans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleLarge = TextStyle(fontFamily = TikTokSans, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = TikTokSans, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = TikTokSans, fontWeight = FontWeight.Medium, fontSize = 13.5.sp, lineHeight = 18.sp),
    bodyLarge = TextStyle(fontFamily = TikTokSans, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp, letterSpacing = 0.3.sp),
    bodyMedium = TextStyle(fontFamily = TikTokSans, fontWeight = FontWeight.Normal, fontSize = 13.5.sp, lineHeight = 19.sp),
    bodySmall = TextStyle(fontFamily = TikTokSans, fontWeight = FontWeight.Normal, fontSize = 11.5.sp, lineHeight = 15.sp),
    labelLarge = TextStyle(fontFamily = TikTokSans, fontWeight = FontWeight.Medium, fontSize = 13.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontFamily = TikTokSans, fontWeight = FontWeight.Medium, fontSize = 11.5.sp, lineHeight = 15.sp),
    labelSmall = TextStyle(fontFamily = TikTokSans, fontWeight = FontWeight.Medium, fontSize = 10.5.sp, lineHeight = 14.sp)
)
