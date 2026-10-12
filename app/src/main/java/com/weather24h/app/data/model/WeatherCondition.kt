package com.weather24h.app.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class WeatherType {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    DRIZZLE,
    RAIN,
    SNOW,
    THUNDERSTORM
}

data class WeatherCondition(
    val descriptionVi: String,
    val type: WeatherType
) {
    val description: String
        get() = descriptionVi

    companion object {
        fun fromWmoCode(code: Int, rainProb: Int = 0, isDay: Boolean = true): WeatherCondition {
            // Nếu xác suất mưa cao (>= 40-50%), dung hòa trạng thái thời tiết chính xác với thực tế
            if (code <= 2 && rainProb >= 50) {
                return WeatherCondition(
                    if (isDay) "Nắng có lúc có mưa rào" else "Đêm có lúc có mưa rào",
                    WeatherType.RAIN
                )
            }
            if (code <= 2 && rainProb in 35..49) {
                return WeatherCondition(
                    if (isDay) "Nắng gián đoạn, có thể mưa" else "Đêm ít mây, có thể mưa",
                    WeatherType.PARTLY_CLOUDY
                )
            }
            if (code == 3 && rainProb >= 50) {
                return WeatherCondition("Âm u, có mưa rào", WeatherType.RAIN)
            }
            if (code == 3 && rainProb in 35..49) {
                return WeatherCondition("Nhiều mây, khả năng có mưa", WeatherType.CLOUDY)
            }

            return when (code) {
                0 -> WeatherCondition("Trời quang đãng", WeatherType.CLEAR)
                1 -> WeatherCondition("Ít mây, trời trong", WeatherType.CLEAR)
                2 -> WeatherCondition("Mây rải rác", WeatherType.PARTLY_CLOUDY)
                3 -> WeatherCondition("Nhiều mây, âm u", WeatherType.CLOUDY)
                45, 48 -> WeatherCondition("Sương mù dày", WeatherType.FOG)
                51, 53, 55 -> WeatherCondition("Mưa phùn nhẹ", WeatherType.DRIZZLE)
                61 -> WeatherCondition("Mưa rào nhẹ", WeatherType.RAIN)
                63, 65 -> WeatherCondition("Mưa rào vừa đến to", WeatherType.RAIN)
                71, 73, 75, 77 -> WeatherCondition("Tuyết rơi", WeatherType.SNOW)
                80, 81, 82 -> WeatherCondition("Mưa rào từng đợt", WeatherType.RAIN)
                95, 96, 99 -> WeatherCondition("Dông bão, có sấm sét", WeatherType.THUNDERSTORM)
                else -> WeatherCondition("Thời tiết ôn hòa", WeatherType.PARTLY_CLOUDY)
            }
        }

        fun getWeatherIcon(type: WeatherType, isDay: Boolean = true): ImageVector {
            return when (type) {
                WeatherType.CLEAR -> if (isDay) Icons.Default.WbSunny else Icons.Default.NightsStay
                WeatherType.PARTLY_CLOUDY -> if (isDay) Icons.Default.WbCloudy else Icons.Default.NightsStay
                WeatherType.CLOUDY -> Icons.Default.Cloud
                WeatherType.FOG -> Icons.Default.Dehaze
                WeatherType.DRIZZLE -> Icons.Default.Grain
                WeatherType.RAIN -> Icons.Default.WaterDrop
                WeatherType.THUNDERSTORM -> Icons.Default.Thunderstorm
                WeatherType.SNOW -> Icons.Default.AcUnit
            }
        }

        fun getWeatherIconTint(type: WeatherType, isDay: Boolean = true): Color {
            return when (type) {
                WeatherType.CLEAR -> if (isDay) Color(0xFFFFB703) else Color(0xFFBAE6FD)
                WeatherType.PARTLY_CLOUDY -> if (isDay) Color(0xFFFFD166) else Color(0xFF93C5FD)
                WeatherType.CLOUDY -> Color(0xFFCBD5E1)
                WeatherType.FOG -> Color(0xFF94A3B8)
                WeatherType.DRIZZLE -> Color(0xFF38BDF8)
                WeatherType.RAIN -> Color(0xFF0284C7)
                WeatherType.THUNDERSTORM -> Color(0xFFF59E0B)
                WeatherType.SNOW -> Color(0xFFE0F2FE)
            }
        }
    }
}
