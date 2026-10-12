package com.weather24h.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.weather24h.app.data.model.WeatherType

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val PrimaryBlue = Color(0xFF2193B0)
val SkyBlue = Color(0xFF6DD5ED)
val AccentYellow = Color(0xFFFFD166)
val AccentGreen = Color(0xFF06D6A0)
val AccentRed = Color(0xFFEF476F)
val DarkNavy = Color(0xFF0F172A)
val CardGlassBg = Color(0x26FFFFFF)
val CardBorder = Color(0x40FFFFFF)

fun getSkyGradient(type: WeatherType, isDay: Boolean): Brush {
    if (!isDay) {
        return when (type) {
            WeatherType.THUNDERSTORM -> Brush.verticalGradient(listOf(Color(0xFF1F1C2C), Color(0xFF928DAB)))
            WeatherType.RAIN -> Brush.verticalGradient(listOf(Color(0xFF1E293B), Color(0xFF334155)))
            else -> Brush.verticalGradient(listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)))
        }
    }
    return when (type) {
        WeatherType.CLEAR -> Brush.verticalGradient(listOf(Color(0xFF2193B0), Color(0xFF6DD5ED)))
        WeatherType.PARTLY_CLOUDY -> Brush.verticalGradient(listOf(Color(0xFF3A7BD5), Color(0xFF3A6073)))
        WeatherType.CLOUDY, WeatherType.FOG -> Brush.verticalGradient(listOf(Color(0xFF4B6CB7), Color(0xFF182848)))
        WeatherType.DRIZZLE, WeatherType.RAIN -> Brush.verticalGradient(listOf(Color(0xFF373B44), Color(0xFF4286F4)))
        WeatherType.THUNDERSTORM -> Brush.verticalGradient(listOf(Color(0xFF1F1C2C), Color(0xFF928DAB)))
        WeatherType.SNOW -> Brush.verticalGradient(listOf(Color(0xFF83A4D4), Color(0xFFB6FBFF)))
    }
}
