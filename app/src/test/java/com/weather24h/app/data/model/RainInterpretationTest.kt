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

    @Test fun dailyForecastSunnyHotDayDoesNotShowDrizzleWhenHourlyIsDry() {
        // Tái hiện trường hợp ngày 2026-10-24: Cao nhất 31°C, Thấp nhất 26°C, UV 7.4 Rất cao,
        // các giờ trong ngày khô ráo (0.0 - 0.1mm), nhưng Open-Meteo daily.weather_code = 51 (Mưa phùn nhẹ)
        val hours = (0..23).map { h ->
            HourlyForecast(
                timeString = String.format("2026-10-24T%02d:00", h),
                hourLabel = String.format("%02d:00", h),
                temperature = if (h in 10..16) 31.0 else 27.0,
                weatherCode = if (h == 17) 51 else 1,
                precipitationProbability = if (h == 17) 35 else 8,
                humidity = 78,
                windSpeed = 12.0,
                isDay = h in 6..17,
                precipitationMm = if (h == 17) 0.1 else 0.0
            )
        }
        val day = DailyForecast(
            dateString = "2026-10-24",
            dayOfWeek = "Hôm nay",
            maxTemp = 31.0,
            minTemp = 26.0,
            weatherCode = 51,
            precipitationProbability = 78,
            sunrise = "05:50",
            sunset = "17:30",
            uvIndexMax = 7.4,
            hourly24h = hours,
            sourceName = "Open-Meteo",
            precipitationSum = 0.1
        )
        assertNotEquals(WeatherType.DRIZZLE, day.condition.type)
        assertNotEquals(WeatherType.RAIN, day.condition.type)
        assertTrue(day.condition.type in listOf(WeatherType.CLEAR, WeatherType.PARTLY_CLOUDY))
        assertTrue(day.condition.description.contains("nắng", ignoreCase = true))
    }

    @Test fun zeroPrecipitationNeverReportsHeavyRainOnMainCardOrAlerts() {
        val currentZeroRain = CurrentWeather(
            temperature = 27.0,
            feelsLike = 29.0,
            weatherCode = 65, // WMO 65 = Mưa rào vừa đến to, nhưng thực đo 0.0mm
            humidity = 75,
            windSpeed = 10.0,
            windDirection = 120,
            uvIndex = 3.0,
            surfacePressure = 1012.0,
            visibilityKm = 10.0,
            precipitation = 0.0,
            isDay = true,
            time = "15:00"
        )
        assertNotEquals(WeatherType.RAIN, currentZeroRain.condition.type)
        assertNotEquals(WeatherType.DRIZZLE, currentZeroRain.condition.type)
        assertEquals(0.0, currentZeroRain.effectivePrecipitationMm, 0.001)

        val hourZeroRain = hour(probability = 25, amount = 0.0, code = 65)
        assertNotEquals(WeatherType.RAIN, hourZeroRain.condition.type)

        val data = WeatherData(
            location = CityLocation("Hoằng Thanh", "Việt Nam", latitude = 19.85, longitude = 105.92),
            current = currentZeroRain,
            hourly24h = listOf(hourZeroRain),
            daily7Days = emptyList(),
            airQuality = null,
            lastUpdated = "15:00"
        )
        val alerts = SevereWeatherDetector.detectAlerts(data)
        assertFalse(alerts.any { it.id == "alert_heavy_rain" })
    }
}
