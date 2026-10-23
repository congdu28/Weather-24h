package com.weather24h.app.data.model

import org.junit.Assert.*
import org.junit.Test

class RainInterpretationTest {
    private fun hour(probability: Int, amount: Double? = null, code: Int = 61) =
        HourlyForecast("2026-10-13T14:00:00", "14:00", 29.0, code, probability, 77, 12.0, precipitationMm = amount)

    private fun weather(hour: HourlyForecast, humidity: Int = 77) = WeatherData(
        location = CityLocation("Thanh Hóa", "Việt Nam", latitude = 19.8075, longitude = 105.7764),
        current = CurrentWeather(29.0, 33.0, 2, humidity, 12.0, 0, 0.0, 1010.0, 10.0, 0.0, true, "13:00"),
        hourly24h = listOf(hour), daily7Days = emptyList(), airQuality = null, lastUpdated = "13:15"
    )

    @Test fun highProbabilityDoesNotChangeSunnyOrCloudyCondition() {
        assertEquals(WeatherType.CLEAR, hour(90, code = 0).condition.type)
        assertEquals(WeatherType.PARTLY_CLOUDY, hour(90, code = 2).condition.type)
        assertEquals(WeatherType.CLOUDY, hour(90, code = 3).condition.type)
    }

    @Test fun highProbabilityWithLightRainDoesNotTriggerHeavyRainAlert() {
        val alerts = SevereWeatherDetector.detectAlerts(weather(hour(73, 2.68, 81)))
        assertFalse(alerts.any { it.id == "alert_heavy_rain" })
        assertTrue(alerts.any { it.id == "alert_rain_chance" })
        assertFalse(alerts.any { "DIỆN RỘNG" in it.title })
    }

    @Test fun largeRainAmountCanTriggerAlertEvenWithLowProbability() {
        assertTrue(SevereWeatherDetector.detectAlerts(weather(hour(20, 6.0))).any { it.id == "alert_heavy_rain" })
    }

    @Test fun heavyRainCodeCanTriggerAlertWithoutAmount() {
        assertTrue(SevereWeatherDetector.detectAlerts(weather(hour(40, code = 65))).any { it.id == "alert_heavy_rain" })
    }

    @Test fun missingProbabilityIsNotPresentedAsZeroOrUsedForRainWarning() {
        val missingHour = hour(-1)
        assertEquals("—", missingHour.rainProbabilityText)
        assertFalse(SevereWeatherDetector.detectAlerts(weather(missingHour)).any { it.id.contains("rain") })
        assertEquals("0%", hour(0).rainProbabilityText)
    }

    @Test fun nearbyThunderIsNotReportedAsCurrentThunderstorm() {
        val nearby = WeatherCondition.fromSource(95, "Có thể có dông quanh khu vực", nearby = true)
        val weather = weather(hour(10, code = 2))
        val current = weather.current.copy(weatherCode = 95, sourceCondition = nearby)
        assertFalse(SevereWeatherDetector.detectAlerts(weather.copy(current = current)).any { it.id == "alert_thunder" })
    }

    @Test fun humidity77IsNotDryAir() {
        val moisture = LifestyleCalculator.calculate(weather(hour(20))).single { it.id == "moisture" }
        assertNotEquals("Khô hanh", moisture.status)
        assertFalse(moisture.shortTip.contains("thấp"))
    }

    @Test fun tropicalHeatWithTracePrecipitationDoesNotShowDrizzleOrRain() {
        // Tái hiện trường hợp tại Thanh Hóa: trưa hè nắng nóng 32°C, cảm giác 38°C, Open-Meteo trả mã 51 (mưa phùn) nhưng lượng mưa 0.1mm
        val current = CurrentWeather(
            temperature = 32.0,
            feelsLike = 38.0,
            weatherCode = 51,
            humidity = 68,
            windSpeed = 12.0,
            windDirection = 90,
            uvIndex = 6.0,
            surfacePressure = 1010.0,
            visibilityKm = 15.0,
            precipitation = 0.1,
            isDay = true,
            time = "12:15"
        )
        assertNotEquals(WeatherType.DRIZZLE, current.condition.type)
        assertNotEquals(WeatherType.RAIN, current.condition.type)
        assertEquals(WeatherType.PARTLY_CLOUDY, current.condition.type)
        assertTrue(current.condition.description.contains("nắng", ignoreCase = true))

        val weatherData = WeatherData(
            location = CityLocation("Thanh Hóa", "Việt Nam", latitude = 19.8075, longitude = 105.7764),
            current = current,
            hourly24h = listOf(hour(25, 0.1, code = 51)),
            daily7Days = emptyList(),
            airQuality = null,
            lastUpdated = "12:15"
        )
        val alerts = SevereWeatherDetector.detectAlerts(weatherData)
        // Phải kích hoạt cảnh báo nắng nóng
        assertTrue(alerts.any { it.id == "alert_heat" })
        // Tuyệt đối không kích hoạt cảnh báo mưa dông hay mưa lớn
        assertFalse(alerts.any { it.id == "alert_heavy_rain" })
        assertFalse(alerts.any { it.id == "alert_thunder" })
    }
}
