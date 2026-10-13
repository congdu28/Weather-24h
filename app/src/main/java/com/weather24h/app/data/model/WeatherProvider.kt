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
        badge = "Khuyên dùng • Mặc định"
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
        subtitle = "Dữ liệu khí tượng mở toàn cầu, đa siêu máy tính (ECMWF, JMA Nhật Bản,...)",
        badge = "Đa mô hình • Miễn phí"
    );

    companion object {
        fun fromId(id: String?): WeatherProvider {
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: WEATHER_API
        }
    }
}
