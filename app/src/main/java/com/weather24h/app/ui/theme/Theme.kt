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
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme
    val cardGlassBg = if (isDark) CardGlassBgDark else CardGlassBgLight
    val cardBorder = if (isDark) CardBorderDark else CardBorderLight
    val contentTextColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtextColor = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)

    CompositionLocalProvider(
        LocalIsDarkTheme provides isDark,
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
