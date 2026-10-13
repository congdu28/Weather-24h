package com.weather24h.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

enum class AppThemeMode(val title: String) {
    SYSTEM("Theo thiết bị"),
    LIGHT("Sáng"),
    DARK("Tối")
}

val LocalIsDarkTheme = staticCompositionLocalOf { true }
val LocalCardOpacity = staticCompositionLocalOf { 0.80f }
val LocalCardGlassBg = staticCompositionLocalOf { CardGlassBgDark }
val LocalCardBorder = staticCompositionLocalOf { CardBorderDark }
val LocalContentTextColor = staticCompositionLocalOf { Color.White }
val LocalSubtextColor = staticCompositionLocalOf { Color(0xFFCBD5E1) }

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    secondary = SkyBlue,
    tertiary = Pink80,
    background = DarkNavy,
    surface = Color(0xFF1E293B),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = SkyBlue,
    tertiary = Pink80,
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color(0xFF0F172A),
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

@Composable
fun Weather24hTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    cardOpacity: Float = 0.80f,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme
    val clampedOpacity = cardOpacity.coerceIn(0.20f, 1.0f)
    val baseDark = Color(0xFF0F172A)
    val baseLight = Color.White
    val cardGlassBg = if (isDark) baseDark.copy(alpha = clampedOpacity) else baseLight.copy(alpha = clampedOpacity)
    val cardBorder = if (isDark) CardBorderDark.copy(alpha = (clampedOpacity * 0.40f).coerceIn(0.08f, 0.40f)) else CardBorderLight.copy(alpha = (clampedOpacity * 0.30f).coerceIn(0.06f, 0.30f))
    val contentTextColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155)

    CompositionLocalProvider(
        LocalIsDarkTheme provides isDark,
        LocalCardOpacity provides clampedOpacity,
        LocalCardGlassBg provides cardGlassBg,
        LocalCardBorder provides cardBorder,
        LocalContentTextColor provides contentTextColor,
        LocalSubtextColor provides subtextColor,
        LocalTextStyle provides TextStyle(fontFamily = TikTokSans)
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
