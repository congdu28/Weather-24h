package com.weather24h.app.util

import com.weather24h.app.data.model.AirQuality
import com.weather24h.app.data.model.CityLocation
import com.weather24h.app.data.model.CurrentWeather
import com.weather24h.app.data.model.HourlyForecast
import com.weather24h.app.data.model.OutfitRecommendation
import com.weather24h.app.data.model.WeatherData
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherShareHelperTest {

    private fun createSampleWeather(
        airQuality: AirQuality? = null,
        rainProbability: Int = 30
    ): WeatherData {
        val hour = HourlyForecast(
            timeString = "2026-10-19T10:00:00",
            hourLabel = "10:00",
            temperature = 28.0,
            weatherCode = 1,
            precipitationProbability = rainProbability,
            humidity = 70,
            windSpeed = 15.0
        )
        return WeatherData(
            location = CityLocation("Hà Nội", "Việt Nam", latitude = 21.0285, longitude = 105.8542),
            current = CurrentWeather(
                temperature = 28.4,
                feelsLike = 31.2,
                weatherCode = 1,
                humidity = 72,
                windSpeed = 14.5,
                windDirection = 120,
                uvIndex = 5.0,
                surfacePressure = 1012.0,
                visibilityKm = 10.0,
                precipitation = 0.0,
                isDay = true,
                time = "10:00"
            ),
            hourly24h = listOf(hour),
            daily7Days = emptyList(),
            airQuality = airQuality,
            lastUpdated = "10:15"
        )
    }

    @Test
    fun testBuildForecastShareText_withoutAirQualityAndOutfit() {
        val weather = createSampleWeather()
        val text = WeatherShareHelper.buildForecastShareText(weather)

        assertTrue("Should contain location name", text.contains("Hà Nội"))
        assertTrue("Should contain temperature", text.contains("28°C"))
        assertTrue("Should contain feels like temperature", text.contains("31°C"))
        assertTrue("Should contain rain chance", text.contains("30%"))
        assertFalse("Should not leak github URL", text.contains("github.com"))
        assertFalse("Should not mention AQI when null", text.contains("AQI:"))
    }

    @Test
    fun testBuildForecastShareText_withAirQualityAndOutfit() {
        val aqi = AirQuality(
            pm25 = 25.0,
            pm10 = 45.0,
            aqiIndex = 68,
            aqiScale = "vn"
        )
        val outfit = OutfitRecommendation(
            summary = "Trang phục thoáng mát năng động",
            top = "Áo phông cotton",
            bottom = "Quần shorts hoặc kaki mỏng",
            footwear = "Giày thể thao",
            accessories = listOf("Kính râm", "Nón lưỡi trai"),
            activityTip = "Thích hợp dạo phố",
            styleVibe = "Năng động"
        )
        val weather = createSampleWeather(airQuality = aqi)
        val text = WeatherShareHelper.buildForecastShareText(weather, outfit)

        assertTrue("Should contain location name", text.contains("Hà Nội"))
        assertTrue("Should contain AQI index", text.contains("68"))
        assertTrue("Should contain outfit top", text.contains("Áo phông cotton"))
        assertTrue("Should contain outfit bottom", text.contains("Quần shorts hoặc kaki mỏng"))
        assertTrue("Should contain accessory", text.contains("Kính râm"))
        assertFalse("Should not leak github URL", text.contains("github.com"))
    }

    @Test
    fun testBuildAppShareText() {
        val text = WeatherShareHelper.buildAppShareText()

        assertTrue("Should contain app name", text.contains("Weather 24h"))
        assertTrue("Should contain features", text.contains("Radar") || text.contains("Widget"))
        assertFalse("Should not leak github URL", text.contains("github.com"))
    }
}
