package com.weather24h.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherProviderTest {
    @Test fun newOrUnknownProviderUsesOpenMeteo() {
        assertEquals(WeatherProvider.OPEN_METEO, WeatherProvider.fromId(null))
        assertEquals(WeatherProvider.OPEN_METEO, WeatherProvider.fromId("unknown"))
    }

    @Test fun explicitSupportedProviderChoiceIsPreserved() {
        assertEquals(WeatherProvider.WEATHER_API, WeatherProvider.fromId("weather_api"))
        assertEquals(WeatherProvider.OPEN_METEO, WeatherProvider.fromId("open_meteo"))
    }
}
