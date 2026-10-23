package com.weather24h.app.data.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class CurrentWeather(
    val temperature: Double,
    val feelsLike: Double,
    val weatherCode: Int,
    val humidity: Int,
    val windSpeed: Double,
    val windDirection: Int,
    val uvIndex: Double,
    val surfacePressure: Double,
    val visibilityKm: Double,
    val precipitation: Double,
    val isDay: Boolean,
    val time: String,
    val sourceCondition: WeatherCondition? = null
) {
    val condition: WeatherCondition
        get() {
            val base = sourceCondition ?: WeatherCondition.fromWmoCode(weatherCode)
            // Loại bỏ hoàn toàn lỗi mâu thuẫn thời tiết: Trời nắng nóng nhiệt độ cao (>= 28°C hoặc cảm giác >= 33°C)
            // nhưng mô hình khí tượng gắn nhầm mã mưa phùn/mưa nhẹ (51, 53, 55, 61) do độ ẩm tầng cao, trong khi lượng mưa <= 0.2mm
            if ((base.type == WeatherType.DRIZZLE || (base.type == WeatherType.RAIN && precipitation <= 0.2)) &&
                precipitation <= 0.2 &&
                isDay && (temperature >= 28.0 || feelsLike >= 33.0)
            ) {
                return if (temperature >= 32.0 || feelsLike >= 36.0) {
                    WeatherCondition("Trời nắng, mây thay đổi", WeatherType.PARTLY_CLOUDY)
                } else {
                    WeatherCondition("Nắng xen mây", WeatherType.PARTLY_CLOUDY)
                }
            }
            // Nếu lượng mưa = 0.0mm mà báo mưa phùn (drizzle):
            if (base.type == WeatherType.DRIZZLE && precipitation <= 0.05) {
                return if (temperature >= 24.0) {
                    WeatherCondition(if (isDay) "Nhiều mây, có lúc hửng nắng" else "Nhiều mây", WeatherType.PARTLY_CLOUDY)
                } else {
                    WeatherCondition("Nhiều mây, âm u", WeatherType.CLOUDY)
                }
            }
            return base
        }

    val windDirectionText: String
        get() {
            val directions = listOf("Bắc", "Đông Bắc", "Đông", "Đông Nam", "Nam", "Tây Nam", "Tây", "Tây Bắc")
            val index = (((windDirection + 22.5) % 360) / 45).toInt()
            return directions[index % 8]
        }
}

data class HourlyForecast(
    val timeString: String,
    val hourLabel: String,
    val temperature: Double,
    val weatherCode: Int,
    val precipitationProbability: Int,
    val humidity: Int,
    val windSpeed: Double,
    val isDay: Boolean = true,
    val precipitationMm: Double? = null,
    val sourceCondition: WeatherCondition? = null
) {
    val condition: WeatherCondition
        get() {
            val base = sourceCondition ?: WeatherCondition.fromWmoCode(weatherCode)
            val precip = precipitationMm ?: 0.0
            // Nếu nhiệt độ cao (>= 28°C) hoặc xác suất mưa thấp (<= 35%), mà lượng mưa thực tế <= 0.2mm
            // nhưng mã dự báo Open-Meteo lại gán mưa phùn/mưa nhẹ (51, 53, 55, 61):
            if ((base.type == WeatherType.DRIZZLE || (base.type == WeatherType.RAIN && precip <= 0.2)) &&
                precip <= 0.2 &&
                (temperature >= 28.0 || precipitationProbability in 0..35)
            ) {
                return if (precipitationProbability in 20..35) {
                    WeatherCondition(if (isDay) "Nắng xen mây, có thể có mưa nhẹ" else "Có mây, khả năng mưa thấp", WeatherType.PARTLY_CLOUDY)
                } else {
                    WeatherCondition(if (isDay) "Trời nắng, có mây" else "Trời quang, có mây", WeatherType.PARTLY_CLOUDY)
                }
            }
            return base
        }
    val rainProbabilityText: String
        get() = if (precipitationProbability in 0..100) "$precipitationProbability%" else "—"
}

data class DailyForecast(
    val dateString: String,
    val dayOfWeek: String,
    val maxTemp: Double,
    val minTemp: Double,
    val weatherCode: Int,
    val precipitationProbability: Int,
    val sunrise: String,
    val sunset: String,
    val uvIndexMax: Double,
    val hourly24h: List<HourlyForecast> = emptyList(),
    val sourceName: String? = null
) {
    val rainProbabilityText: String
        get() = if (precipitationProbability in 0..100) "$precipitationProbability%" else "—"
}

data class AirQuality(
    val pm25: Double,
    val pm10: Double,
    val aqiIndex: Int,
    val aqiScale: String? = null
) {
    val statusText: String
        get() = when {
            pm25 <= 12.0 -> "Tuyệt vời"
            pm25 <= 35.4 -> "Tốt"
            pm25 <= 55.4 -> "Trung bình"
            pm25 <= 150.4 -> "Kém"
            else -> "Nguy hại"
        }
}

data class WeatherData(
    val location: CityLocation,
    val current: CurrentWeather,
    val hourly24h: List<HourlyForecast>,
    val daily7Days: List<DailyForecast>,
    val airQuality: AirQuality?,
    val lastUpdated: String,
    val forecastModel: WeatherForecastModel = WeatherForecastModel.BEST_MATCH,
    val isOffline: Boolean = false,
    val providerName: String = "Không rõ nguồn"
) {
    val maxTemp24h: Double
        get() = hourly24h.maxOfOrNull { it.temperature } ?: current.temperature

    val minTemp24h: Double
        get() = hourly24h.minOfOrNull { it.temperature } ?: current.temperature
}
