package com.weather24h.app.data.model

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoriteCitiesSerializationTest {

    private val gson = Gson()

    @Test
    fun testCityLocationSerializationWithSerializedName() {
        val city = CityLocation(
            name = "Hà Nội",
            country = "Việt Nam",
            admin1 = "Thủ đô",
            latitude = 21.0285,
            longitude = 105.8542,
            isCurrentLocation = true
        )

        val json = gson.toJson(city)
        assertTrue(json.contains("\"isCurrentLocation\":true"))
        assertTrue(json.contains("\"name\":\"Hà Nội\""))

        val deserialized = gson.fromJson(json, CityLocation::class.java)
        assertEquals(city.name, deserialized.name)
        assertEquals(city.country, deserialized.country)
        assertEquals(city.latitude, deserialized.latitude, 0.0001)
        assertEquals(city.longitude, deserialized.longitude, 0.0001)
        assertTrue(deserialized.isCurrentLocation)
    }

    @Test
    fun testLegacyJsonWithoutIsCurrentLocation() {
        val legacyJson = """[{"name":"Đà Nẵng","country":"Việt Nam","latitude":16.0544,"longitude":108.2022}]"""

        // Test array deserialization (immune to generic erasure)
        val array = gson.fromJson(legacyJson, Array<CityLocation>::class.java)
        assertNotNull(array)
        assertEquals(1, array.size)
        assertEquals("Đà Nẵng", array[0].name)
        assertEquals(false, array[0].isCurrentLocation)

        // Test manual json parser fallback
        val element = JsonParser.parseString(legacyJson).asJsonArray
        assertEquals(1, element.size())
        val first = element.get(0).asJsonObject
        assertEquals("Đà Nẵng", first.get("name").asString)
    }

    @Test
    fun testMultiCityListDeserialization() {
        val cities = listOf(
            CityLocation(name = "Hà Nội", country = "Việt Nam", latitude = 21.0285, longitude = 105.8542),
            CityLocation(name = "TP. Hồ Chí Minh", country = "Việt Nam", latitude = 10.8231, longitude = 106.6297)
        )

        val json = gson.toJson(cities)
        val array = gson.fromJson(json, Array<CityLocation>::class.java)
        assertEquals(2, array.size)
        assertEquals("Hà Nội", array[0].name)
        assertEquals("TP. Hồ Chí Minh", array[1].name)
    }
}
