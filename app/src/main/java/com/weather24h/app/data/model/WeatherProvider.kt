package com.weather24h.app.data.model

enum class WeatherProvider(
    val id: String,
    val title: String,
    val subtitle: String,
    val badge: String
) {
    WEATHER_API(
        id = "weather_api",
        title = "WeatherAPI.com",
        subtitle = "Thời tiết hiện tại và 3 ngày dự báo; Open-Meteo bổ sung khi cần",
        badge = "Nguồn tùy chọn"
    ),
    GOOGLE(
        id = "google",
        title = "Google Weather (WeatherNext AI)",
        subtitle = "Mô hình AI dự báo của Google DeepMind (Cần cấu hình API Key Google Cloud)",
        badge = "Google AI"
    ),
    OPEN_METEO(
        id = "open_meteo",
        title = "Open-Meteo",
        subtitle = "Chọn mô hình theo tọa độ • Không cần API key",
        badge = "Khuyên dùng • Mặc định"
    );

    companion object {
        fun fromId(id: String?): WeatherProvider {
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: OPEN_METEO
        }
    }
}
