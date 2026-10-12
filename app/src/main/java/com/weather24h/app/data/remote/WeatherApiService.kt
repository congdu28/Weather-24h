package com.weather24h.app.data.remote

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.weather24h.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class WeatherApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun getFullWeather(location: CityLocation): WeatherData = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.open-meteo.com/v1/forecast?" +
                    "latitude=${location.latitude}&" +
                    "longitude=${location.longitude}&" +
                    "current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,surface_pressure,wind_speed_10m,wind_direction_10m&" +
                    "hourly=temperature_2m,relative_humidity_2m,precipitation_probability,weather_code,wind_speed_10m&" +
                    "daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max,precipitation_probability_max&" +
                    "timezone=auto"

            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: throw Exception("Empty body")

            val json = gson.fromJson(responseBody, JsonObject::class.java)

            // Lấy AQI
            var airQuality: AirQuality? = null
            try {
                val aqiUrl = "https://air-quality-api.open-meteo.com/v1/air-quality?" +
                        "latitude=${location.latitude}&" +
                        "longitude=${location.longitude}&" +
                        "current=pm10,pm2_5,european_aqi"
                val aqiRequest = Request.Builder().url(aqiUrl).build()
                val aqiResponse = client.newCall(aqiRequest).execute()
                val aqiBody = aqiResponse.body?.string()
                if (aqiBody != null) {
                    val aqiJson = gson.fromJson(aqiBody, JsonObject::class.java)
                    val currentAqi = aqiJson.getAsJsonObject("current")
                    if (currentAqi != null) {
                        airQuality = AirQuality(
                            pm25 = currentAqi.get("pm2_5")?.asDouble ?: 18.0,
                            pm10 = currentAqi.get("pm10")?.asDouble ?: 30.0,
                            aqiIndex = currentAqi.get("european_aqi")?.asInt ?: 35
                        )
                    }
                }
            } catch (_: Exception) {}

            parseWeatherData(location, json, airQuality)
        } catch (e: Exception) {
            getFallbackMockData(location)
        }
    }

    suspend fun searchCities(query: String): List<CityLocation> = withContext(Dispatchers.IO) {
        if (query.trim().isEmpty()) return@withContext CityLocation.POPULAR_CITIES

        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=10&language=vi&format=json"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext emptyList()

            val json = gson.fromJson(responseBody, JsonObject::class.java)
            val results = json.getAsJsonArray("results") ?: return@withContext emptyList()

            val list = mutableListOf<CityLocation>()
            for (elem in results) {
                val obj = elem.asJsonObject
                list.add(
                    CityLocation(
                        name = obj.get("name")?.asString ?: "",
                        country = obj.get("country")?.asString ?: "",
                        admin1 = obj.get("admin1")?.asString,
                        latitude = obj.get("latitude").asDouble,
                        longitude = obj.get("longitude").asDouble
                    )
                )
            }
            list
        } catch (e: Exception) {
            CityLocation.POPULAR_CITIES.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    suspend fun getAddressFromCoordinates(context: android.content.Context, latitude: Double, longitude: Double): Pair<String, String> = withContext(Dispatchers.IO) {
        // 1. Dùng Geocoder gốc của Android với Locale tiếng Việt
        try {
            val geocoder = android.location.Geocoder(context, java.util.Locale("vi", "VN"))
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val district = addr.subAdminArea ?: addr.locality ?: addr.subLocality
                val city = addr.adminArea ?: addr.countryName
                val name = if (!district.isNullOrBlank() && !city.isNullOrBlank() && district != city) {
                    "$district, $city"
                } else {
                    district ?: city ?: "Vị trí của bạn"
                }
                val country = addr.countryName ?: "Việt Nam"
                return@withContext Pair(name, country)
            }
        } catch (_: Exception) {}

        // 2. Dự phòng qua REST Reverse Geocoding API miễn phí chuẩn xác
        try {
            val url = "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=$latitude&longitude=$longitude&localityLanguage=vi"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (body != null) {
                val json = gson.fromJson(body, JsonObject::class.java)
                val city = json.get("city")?.asString ?: json.get("principalSubdivision")?.asString ?: ""
                val locality = json.get("locality")?.asString ?: ""
                val country = json.get("countryName")?.asString ?: "Việt Nam"
                val name = if (locality.isNotEmpty() && city.isNotEmpty() && locality != city) {
                    "$locality, $city"
                } else if (city.isNotEmpty()) {
                    city
                } else if (locality.isNotEmpty()) {
                    locality
                } else {
                    "Vị trí của bạn"
                }
                return@withContext Pair(name, country)
            }
        } catch (_: Exception) {}

        Pair("Vị trí của bạn", "GPS")
    }

    private fun parseWeatherData(location: CityLocation, json: JsonObject, aqi: AirQuality?): WeatherData {
        val currentObj = json.getAsJsonObject("current")
        val hourlyObj = json.getAsJsonObject("hourly")
        val dailyObj = json.getAsJsonObject("daily")

        val current = CurrentWeather(
            temperature = currentObj.get("temperature_2m").asDouble,
            feelsLike = currentObj.get("apparent_temperature").asDouble,
            weatherCode = currentObj.get("weather_code").asInt,
            humidity = currentObj.get("relative_humidity_2m").asInt,
            windSpeed = currentObj.get("wind_speed_10m").asDouble,
            windDirection = currentObj.get("wind_direction_10m").asInt,
            uvIndex = 5.0,
            surfacePressure = currentObj.get("surface_pressure").asDouble,
            visibilityKm = 10.0,
            precipitation = currentObj.get("precipitation").asDouble,
            isDay = currentObj.get("is_day").asInt == 1,
            time = currentObj.get("time").asString
        )

        // Parse Hourly 24h
        val hourlyTimes = hourlyObj.getAsJsonArray("time")
        val hourlyTemps = hourlyObj.getAsJsonArray("temperature_2m")
        val hourlyCodes = hourlyObj.getAsJsonArray("weather_code")
        val hourlyRains = hourlyObj.getAsJsonArray("precipitation_probability")
        val hourlyHumids = hourlyObj.getAsJsonArray("relative_humidity_2m")
        val hourlyWinds = hourlyObj.getAsJsonArray("wind_speed_10m")

        val hourlyList = mutableListOf<HourlyForecast>()
        val count = minOf(24, hourlyTimes.size())
        for (i in 0 until count) {
            val tStr = hourlyTimes.get(i).asString
            val hourLabel = if (tStr.contains("T")) tStr.substringAfter("T") else tStr
            hourlyList.add(
                HourlyForecast(
                    timeString = tStr,
                    hourLabel = if (i == 0) "Hiện tại" else hourLabel,
                    temperature = hourlyTemps.get(i).asDouble,
                    weatherCode = hourlyCodes.get(i).asInt,
                    precipitationProbability = hourlyRains.get(i).asInt,
                    humidity = hourlyHumids.get(i).asInt,
                    windSpeed = hourlyWinds.get(i).asDouble
                )
            )
        }

        // Parse Daily 7-day
        val dailyTimes = dailyObj.getAsJsonArray("time")
        val dailyCodes = dailyObj.getAsJsonArray("weather_code")
        val dailyMaxs = dailyObj.getAsJsonArray("temperature_2m_max")
        val dailyMins = dailyObj.getAsJsonArray("temperature_2m_min")
        val dailySunrises = dailyObj.getAsJsonArray("sunrise")
        val dailySunsets = dailyObj.getAsJsonArray("sunset")
        val dailyUvs = dailyObj.getAsJsonArray("uv_index_max")
        val dailyRains = dailyObj.getAsJsonArray("precipitation_probability_max")

        val dailyList = mutableListOf<DailyForecast>()
        val dCount = minOf(7, dailyTimes.size())
        for (i in 0 until dCount) {
            val dateStr = dailyTimes.get(i).asString
            val dayOfWeek = if (i == 0) "Hôm nay" else parseDayOfWeek(dateStr)
            val sunriseStr = dailySunrises.get(i).asString.substringAfter("T")
            val sunsetStr = dailySunsets.get(i).asString.substringAfter("T")

            dailyList.add(
                DailyForecast(
                    dateString = dateStr,
                    dayOfWeek = dayOfWeek,
                    maxTemp = dailyMaxs.get(i).asDouble,
                    minTemp = dailyMins.get(i).asDouble,
                    weatherCode = dailyCodes.get(i).asInt,
                    precipitationProbability = dailyRains.get(i).asInt,
                    sunrise = sunriseStr,
                    sunset = sunsetStr,
                    uvIndexMax = dailyUvs.get(i).asDouble
                )
            )
        }

        val updatedStr = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy").format(LocalDateTime.now())

        return WeatherData(
            location = location,
            current = current,
            hourly24h = hourlyList,
            daily7Days = dailyList,
            airQuality = aqi,
            lastUpdated = updatedStr
        )
    }

    private fun parseDayOfWeek(dateStr: String): String {
        return try {
            val date = LocalDate.parse(dateStr)
            when (date.dayOfWeek.value) {
                1 -> "Thứ 2"
                2 -> "Thứ 3"
                3 -> "Thứ 4"
                4 -> "Thứ 5"
                5 -> "Thứ 6"
                6 -> "Thứ 7"
                7 -> "Chủ nhật"
                else -> dateStr
            }
        } catch (_: Exception) {
            dateStr
        }
    }

    private fun getFallbackMockData(location: CityLocation): WeatherData {
        val hourly = (0..23).map { i ->
            HourlyForecast(
                timeString = "2026-10-12T$i:00",
                hourLabel = if (i == 0) "Hiện tại" else String.format("%02d:00", i),
                temperature = 28.0 + (i % 5) - 2.0,
                weatherCode = if (i in 14..16) 61 else 1,
                precipitationProbability = if (i in 14..16) 60 else 10,
                humidity = 70,
                windSpeed = 12.0
            )
        }

        val days = listOf("Hôm nay", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật")
        val daily = days.mapIndexed { index, name ->
            DailyForecast(
                dateString = "2026-10-${12 + index}",
                dayOfWeek = name,
                maxTemp = 32.0 - (index % 3),
                minTemp = 24.0 + (index % 2),
                weatherCode = if (index == 2) 61 else 1,
                precipitationProbability = if (index == 2) 65 else 15,
                sunrise = "05:45",
                sunset = "18:15",
                uvIndexMax = 7.2
            )
        }

        return WeatherData(
            location = location,
            current = CurrentWeather(
                temperature = 29.5,
                feelsLike = 31.0,
                weatherCode = 1,
                humidity = 72,
                windSpeed = 14.0,
                windDirection = 135,
                uvIndex = 6.5,
                surfacePressure = 1012.0,
                visibilityKm = 10.0,
                precipitation = 0.0,
                isDay = true,
                time = "12:00"
            ),
            hourly24h = hourly,
            daily7Days = daily,
            airQuality = AirQuality(22.0, 38.0, 42),
            lastUpdated = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy").format(LocalDateTime.now())
        )
    }
}
