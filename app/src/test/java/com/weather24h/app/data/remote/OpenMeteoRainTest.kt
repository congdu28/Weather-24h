package com.weather24h.app.data.remote

import com.google.gson.JsonParser
import com.weather24h.app.data.model.CityLocation
import org.junit.Assert.*
import org.junit.Test

class OpenMeteoRainTest {
    @Test fun missingProbabilityStaysUnknownDespiteLargeRainAmount() {
        val json = JsonParser.parseString("""
            {
              "current": {"temperature_2m":29, "apparent_temperature":33, "weather_code":61,
                "relative_humidity_2m":77, "wind_speed_10m":12, "wind_direction_10m":90,
                "surface_pressure":1010, "precipitation":0, "is_day":1, "time":"2026-10-13T13:15"},
              "hourly": {"time":["2026-10-13T13:00"], "temperature_2m":[29],
                "weather_code":[61], "precipitation":[6], "relative_humidity_2m":[77],
                "wind_speed_10m":[12], "is_day":[1]},
              "daily": {"time":["2026-10-13"], "temperature_2m_max":[32], "temperature_2m_min":[23],
                "weather_code":[61], "uv_index_max":[3], "precipitation_sum":[10]}
            }
        """).asJsonObject
        val location = CityLocation("Thanh Hóa", latitude = 19.8075, longitude = 105.7764)
        val weather = WeatherApiService().parseWeatherData(location, json, null)
        assertEquals("—", weather.hourly24h.single().rainProbabilityText)
        assertEquals(6.0, weather.hourly24h.single().precipitationMm!!, 0.001)
        assertEquals("—", weather.daily7Days.single().rainProbabilityText)
        assertEquals("—", weather.daily7Days.single().hourly24h.single().rainProbabilityText)
    }
}
