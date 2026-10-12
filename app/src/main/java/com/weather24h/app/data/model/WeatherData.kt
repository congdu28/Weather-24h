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
    val time: String
) {
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
    val windSpeed: Double
)

data class DailyForecast(
    val dateString: String,
    val dayOfWeek: String,
    val maxTemp: Double,
    val minTemp: Double,
    val weatherCode: Int,
    val precipitationProbability: Int,
    val sunrise: String,
    val sunset: String,
    val uvIndexMax: Double
)

data class AirQuality(
    val pm25: Double,
    val pm10: Double,
    val aqiIndex: Int
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
    val lastUpdated: String
) {
    val maxTemp24h: Double
        get() = hourly24h.maxOfOrNull { it.temperature } ?: current.temperature

    val minTemp24h: Double
        get() = hourly24h.minOfOrNull { it.temperature } ?: current.temperature
}
