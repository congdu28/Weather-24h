package com.weather24h.app.data.remote

import com.google.gson.JsonParser
import com.weather24h.app.data.model.*
import org.junit.Assert.*
import org.junit.Test

class WeatherApiComServiceTest {
    // Minimal reproduction of the current/forecast distinction in the Thanh Hóa report.
    private fun response() = JsonParser.parseString("""
        {
          "location": {"localtime": "2026-10-13 13:29"},
          "current": {"temp_c": 28.7, "humidity": 77, "precip_mm": 1.17, "is_day": 1,
            "last_updated": "2026-10-13 13:15", "condition": {"code": 1063, "text": "Mưa lác đác gần đây"}},
          "forecast": {"forecastday": [{"date": "2026-10-13",
            "day": {"maxtemp_c": 32, "mintemp_c": 23, "daily_chance_of_rain": 73, "condition": {"code": 1063}},
            "hour": [{"time": "2026-10-13 14:00", "temp_c": 28.7, "chance_of_rain": 73,
              "precip_mm": 2.68, "is_day": 1, "condition": {"code": 1243, "text": "Mưa rào vừa đến nặng"}}]
          }]}
        }
    """).asJsonObject

    private val location = CityLocation("Thanh Hóa", latitude = 19.8075, longitude = 105.7764)

    @Test fun preservesNearbyDescriptionAndSourceUpdateTime() {
        val weather = WeatherApiComService().parseWeatherApiData(location, response(), null)
        assertEquals("Có thể mưa rải rác quanh khu vực", weather.current.condition.description)
        assertEquals(WeatherType.CLOUDY, weather.current.condition.type)
        assertEquals("13:15 - 13/10/2026", weather.lastUpdated)
        assertEquals(73, weather.hourly24h.single().precipitationProbability)
        assertEquals(2.68, weather.hourly24h.single().precipitationMm!!, 0.001)
        assertFalse(SevereWeatherDetector.detectAlerts(weather).any { it.id == "alert_heavy_rain" })
    }

    @Test fun missingProbabilityIsNotFabricatedFromRainAmount() {
        val json = response()
        val day = json.getAsJsonObject("forecast").getAsJsonArray("forecastday")[0].asJsonObject
        day.getAsJsonArray("hour")[0].asJsonObject.remove("chance_of_rain")
        day.getAsJsonObject("day").remove("daily_chance_of_rain")
        val weather = WeatherApiComService().parseWeatherApiData(location, json, null)
        assertEquals("—", weather.hourly24h.single().rainProbabilityText)
        assertEquals("—", weather.daily7Days.single().rainProbabilityText)
    }
}
