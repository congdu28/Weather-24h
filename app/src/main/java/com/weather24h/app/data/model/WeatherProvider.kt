package com.weather24h.app.data.model

enum class WeatherProvider(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String
) {
    GOOGLE(
        id = "google",
        title = "Google Weather (WeatherNext AI)",
        subtitle = "Mô hình AI dự báo của Google DeepMind, đồng nhất 100% với Google Search",
        badge = "Khuyên dùng • Mặc định"
    ),
    OPEN_METEO(
        id = "open_meteo",
        title = "Open-Meteo",
        subtitle = "Dữ liệu khí tượng mở toàn cầu, miễn phí, không yêu cầu API Key",
        badge = "Dự phòng • Miễn phí"
    );

    companion object {
        fun fromId(id: String?): WeatherProvider {
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: GOOGLE
        }
    }
}
