package com.weather24h.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.weather24h.app.data.model.WeatherType

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val PrimaryBlue = Color(0xFF0284C7)
val SkyBlue = Color(0xFF38BDF8)
val AccentYellow = Color(0xFFFFD166)
val AccentGreen = Color(0xFF06D6A0)
val AccentRed = Color(0xFFEF476F)
val DarkNavy = Color(0xFF0F172A)

// Card glass background with solid backing to prevent contrast issues
val CardGlassBgDark = Color(0xB80F172A) // Deep frosted navy (high contrast against sky)
val CardBorderDark = Color(0x38FFFFFF)

val CardGlassBgLight = Color(0xF2FFFFFF) // Frosted pure white
val CardBorderLight = Color(0x1F0F172A)

// Default fallbacks
val CardGlassBg = CardGlassBgDark
val CardBorder = CardBorderDark

fun getSkyGradient(type: WeatherType, isDay: Boolean, isDarkTheme: Boolean = true): Brush {
    if (!isDarkTheme) {
        // Chế độ Sáng (Light Theme): Nền trời tươi sáng, thanh thoát, dịu mắt
        return when (type) {
            WeatherType.CLEAR -> Brush.verticalGradient(
                listOf(Color(0xFFBAE6FD), Color(0xFFE0F2FE), Color(0xFFF0F9FF))
            )
            WeatherType.PARTLY_CLOUDY -> Brush.verticalGradient(
                listOf(Color(0xFFCBD5E1), Color(0xFFE2E8F0), Color(0xFFF8FAFC))
            )
            WeatherType.CLOUDY, WeatherType.FOG -> Brush.verticalGradient(
                listOf(Color(0xFF94A3B8), Color(0xFFCBD5E1), Color(0xFFF1F5F9))
            )
            WeatherType.DRIZZLE, WeatherType.RAIN -> Brush.verticalGradient(
                listOf(Color(0xFF7DD3FC), Color(0xFFBAE6FD), Color(0xFFE2E8F0))
            )
            WeatherType.THUNDERSTORM -> Brush.verticalGradient(
                listOf(Color(0xFF64748B), Color(0xFF94A3B8), Color(0xFFCBD5E1))
            )
            WeatherType.SNOW -> Brush.verticalGradient(
                listOf(Color(0xFFE0F2FE), Color(0xFFF0FDF4), Color(0xFFFFFFFF))
            )
        }
    }

    // Chế độ Tối (Dark Theme): Bầu trời khí quyển trầm đậm, bảo vệ mắt và tôn nổi bật icon
    if (!isDay) {
        return when (type) {
            WeatherType.THUNDERSTORM -> Brush.verticalGradient(
                listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF311042))
            )
            WeatherType.RAIN -> Brush.verticalGradient(
                listOf(Color(0xFF020617), Color(0xFF0F172A), Color(0xFF1E293B))
            )
            else -> Brush.verticalGradient(
                listOf(Color(0xFF020617), Color(0xFF0F172A), Color(0xFF1E293B))
            )
        }
    }
    return when (type) {
        WeatherType.CLEAR -> Brush.verticalGradient(
            listOf(Color(0xFF0369A1), Color(0xFF0284C7), Color(0xFF0C4A6E))
        )
        WeatherType.PARTLY_CLOUDY -> Brush.verticalGradient(
            listOf(Color(0xFF1E293B), Color(0xFF334155), Color(0xFF0F172A))
        )
        WeatherType.CLOUDY, WeatherType.FOG -> Brush.verticalGradient(
            listOf(Color(0xFF1E293B), Color(0xFF475569), Color(0xFF0F172A))
        )
        WeatherType.DRIZZLE, WeatherType.RAIN -> Brush.verticalGradient(
            listOf(Color(0xFF0F172A), Color(0xFF1E3A5F), Color(0xFF0369A1))
        )
        WeatherType.THUNDERSTORM -> Brush.verticalGradient(
            listOf(Color(0xFF1E1B4B), Color(0xFF2E1065), Color(0xFF0F172A))
        )
        WeatherType.SNOW -> Brush.verticalGradient(
            listOf(Color(0xFF1E293B), Color(0xFF38BDF8).copy(alpha = 0.4f), Color(0xFF0F172A))
        )
    }
}
