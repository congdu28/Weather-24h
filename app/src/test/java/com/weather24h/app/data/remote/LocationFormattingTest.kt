package com.weather24h.app.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test

class LocationFormattingTest {
    private val service = WeatherApiService()

    @Test
    fun `formats Vietnamese ward and province like local weather results`() {
        val result = service.formatHierarchyLocation(
            ward = "Phường Hạc Thành",
            district = null,
            province = "Tỉnh Thanh Hóa"
        )

        assertEquals("Hạc Thành, Thanh Hóa", result.first)
        assertEquals("Việt Nam", result.second)
    }

    @Test
    fun `does not repeat matching district and province`() {
        val result = service.formatHierarchyLocation(
            ward = "Hạc Thành",
            district = "Thành phố Thanh Hóa",
            province = "Thanh Hóa"
        )

        assertEquals("Hạc Thành, Thanh Hóa", result.first)
        assertEquals("Việt Nam", result.second)
    }
}
