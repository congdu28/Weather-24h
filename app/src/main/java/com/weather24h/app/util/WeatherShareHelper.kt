package com.weather24h.app.util

import android.content.Context
import android.content.Intent
import com.weather24h.app.data.model.OutfitRecommendation
import com.weather24h.app.data.model.WeatherData
import com.weather24h.app.data.model.WeatherType
import kotlin.math.roundToInt

object WeatherShareHelper {

    const val APP_RELEASE_URL = "https://github.com/congdu28/Weather-24h/releases/latest"

    /**
     * Chia sẻ bản tin dự báo thời tiết chi tiết kèm gợi ý trang phục & link tải app
     */
    fun shareWeatherForecast(
        context: Context,
        weather: WeatherData,
        outfit: OutfitRecommendation? = null
    ) {
        val curr = weather.current
        val locationName = weather.location.name
        val temp = curr.temperature.roundToInt()
        val feelsLike = curr.feelsLike.roundToInt()
        val desc = curr.condition.description
        val minTemp = weather.minTemp24h.roundToInt()
        val maxTemp = weather.maxTemp24h.roundToInt()
        val maxRain = weather.hourly24h.maxOfOrNull { it.precipitationProbability } ?: 0

        val emoji = when (curr.condition.type) {
            WeatherType.CLEAR -> if (curr.isDay) "☀️" else "🌙"
            WeatherType.PARTLY_CLOUDY -> if (curr.isDay) "🌤️" else "☁️"
            WeatherType.CLOUDY -> "☁️"
            WeatherType.RAIN -> "🌧️"
            WeatherType.DRIZZLE -> "🌦️"
            WeatherType.THUNDERSTORM -> "⛈️"
            WeatherType.SNOW -> "❄️"
            WeatherType.FOG -> "🌫️"
        }

        val aqiInfo = weather.airQuality?.let { aqi ->
            " • AQI: ${aqi.aqi} (${aqi.category})"
        } ?: ""

        val outfitTip = outfit?.let {
            "\n👔 Gợi ý trang phục: ${it.summary}\n• Áo: ${it.top}\n• Quần: ${it.bottom}" +
                (if (it.accessories.isNotEmpty()) "\n• Phụ kiện: ${it.accessories.take(3).joinToString(", ")}" else "")
        } ?: ""

        val shareText = buildString {
            append("$emoji Dự báo thời tiết tại $locationName:\n")
            append("🌡️ Nhiệt độ: $temp°C (Cảm giác: $feelsLike°C)\n")
            append("📈 Biên độ: $minTemp°C - $maxTemp°C • $desc\n")
            append("💧 Xác suất mưa: $maxRain% • Độ ẩm: ${curr.humidity}%$aqiInfo\n")
            if (outfitTip.isNotBlank()) {
                append(outfitTip)
                append("\n")
            }
            append("\n📲 Xem radar trực tiếp & dự báo chi tiết tại Weather 24h:\n$APP_RELEASE_URL")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Chia sẻ dự báo thời tiết")
        context.startActivity(shareIntent)
    }

    /**
     * Chia sẻ liên kết giới thiệu ứng dụng cho bạn bè
     */
    fun shareApp(context: Context) {
        val shareText = """
        🌤️ Tải ngay Weather 24h - Ứng dụng dự báo thời tiết trực quan & chính xác:
        
        ✨ Giao diện nền khí quyển sống động theo thời tiết thực (nắng, mưa, sấm chớp, sao đêm).
        📡 Bản đồ Radar mây mưa trực tiếp thời gian thực (RainViewer / Windy).
        👔 Gợi ý phối đồ thông minh AI theo nhiệt độ & khả năng mưa.
        📱 Bộ 4 Widget màn hình chính đa dạng tự động định vị GPS.
        ⚡ Khởi động tức thì 0s, không quảng cáo, tối ưu pin.
        
        📲 Tải miễn phí bản mới nhất tại:
        $APP_RELEASE_URL
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Giới thiệu Weather 24h với bạn bè")
        context.startActivity(shareIntent)
    }
}
