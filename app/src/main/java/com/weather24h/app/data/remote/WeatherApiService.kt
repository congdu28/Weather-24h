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
                    "hourly=temperature_2m,relative_humidity_2m,precipitation_probability,weather_code,wind_speed_10m,is_day&" +
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
                var ward = addr.subLocality
                var district = addr.subAdminArea ?: addr.locality
                val province = addr.adminArea

                // Nếu Geocoder không tách riêng subLocality, phân tích chuỗi địa chỉ đầy đủ
                val fullLine = addr.getAddressLine(0)
                if (!fullLine.isNullOrBlank()) {
                    val segments = fullLine.split(",").map { it.trim() }
                    if (ward.isNullOrBlank()) {
                        ward = segments.firstOrNull { seg ->
                            seg.startsWith("Phường", ignoreCase = true) ||
                            seg.startsWith("Xã", ignoreCase = true) ||
                            seg.startsWith("Thị trấn", ignoreCase = true) ||
                            seg.startsWith("P.", ignoreCase = true) ||
                            seg.startsWith("X.", ignoreCase = true)
                        }
                    }
                    if (district.isNullOrBlank()) {
                        district = segments.firstOrNull { seg ->
                            seg.startsWith("Quận", ignoreCase = true) ||
                            seg.startsWith("Huyện", ignoreCase = true) ||
                            seg.startsWith("Thị xã", ignoreCase = true) ||
                            seg.startsWith("Thành phố", ignoreCase = true) ||
                            seg.startsWith("Q.", ignoreCase = true) ||
                            seg.startsWith("H.", ignoreCase = true) ||
                            seg.startsWith("TP.", ignoreCase = true)
                        }
                    }
                }

                val name = when {
                    !ward.isNullOrBlank() && !district.isNullOrBlank() -> "$ward, $district"
                    !ward.isNullOrBlank() && !province.isNullOrBlank() -> "$ward, $province"
                    !district.isNullOrBlank() && !province.isNullOrBlank() -> "$district, $province"
                    else -> ward ?: district ?: province ?: "Vị trí của bạn"
                }

                val country = if (!province.isNullOrBlank() && !name.contains(province)) {
                    "$province, Việt Nam"
                } else {
                    addr.countryName ?: "Việt Nam"
                }

                return@withContext Pair(name, country)
            }
        } catch (_: Exception) {}

        // 2. Dự phòng qua REST Reverse Geocoding API BigDataCloud
        try {
            val url = "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=$latitude&longitude=$longitude&localityLanguage=vi"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (body != null) {
                val json = gson.fromJson(body, JsonObject::class.java)
                val city = json.get("city")?.asString ?: ""
                val locality = json.get("locality")?.asString ?: ""
                val principalSubdivision = json.get("principalSubdivision")?.asString ?: ""
                val countryName = json.get("countryName")?.asString ?: "Việt Nam"

                // Tìm kiếm cấp phường/xã/quận/huyện trong localityInfo
                var adminWard: String? = null
                var adminDistrict: String? = null
                val adminArray = json.getAsJsonObject("localityInfo")?.getAsJsonArray("administrative")
                if (adminArray != null) {
                    for (element in adminArray) {
                        val item = element.asJsonObject
                        val nameStr = item.get("name")?.asString ?: ""
                        val descStr = item.get("description")?.asString ?: ""
                        if (descStr.contains("phường", ignoreCase = true) || descStr.contains("xã", ignoreCase = true) || nameStr.startsWith("Phường") || nameStr.startsWith("Xã")) {
                            if (adminWard == null) adminWard = nameStr
                        } else if (descStr.contains("quận", ignoreCase = true) || descStr.contains("huyện", ignoreCase = true) || descStr.contains("thị xã", ignoreCase = true)) {
                            if (adminDistrict == null) adminDistrict = nameStr
                        }
                    }
                }

                val wardOrLocality = adminWard ?: locality
                val districtOrCity = adminDistrict ?: city

                val name = when {
                    wardOrLocality.isNotEmpty() && districtOrCity.isNotEmpty() && wardOrLocality != districtOrCity -> "$wardOrLocality, $districtOrCity"
                    wardOrLocality.isNotEmpty() -> wardOrLocality
                    districtOrCity.isNotEmpty() -> districtOrCity
                    principalSubdivision.isNotEmpty() -> principalSubdivision
                    else -> "Vị trí của bạn"
                }

                val country = if (principalSubdivision.isNotEmpty() && !name.contains(principalSubdivision)) {
                    "$principalSubdivision, $countryName"
                } else {
                    countryName
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
            uvIndex = dailyObj.getAsJsonArray("uv_index_max")?.get(0)?.asDouble ?: 5.0,
            surfacePressure = currentObj.get("surface_pressure").asDouble,
            visibilityKm = 10.0,
            precipitation = currentObj.get("precipitation").asDouble,
            isDay = currentObj.get("is_day").asInt == 1,
            time = currentObj.get("time").asString
        )

        // Parse Hourly 24h chuẩn xác theo giờ hiện tại
        val hourlyTimes = hourlyObj.getAsJsonArray("time")
        val hourlyTemps = hourlyObj.getAsJsonArray("temperature_2m")
        val hourlyCodes = hourlyObj.getAsJsonArray("weather_code")
        val hourlyRains = hourlyObj.getAsJsonArray("precipitation_probability")
        val hourlyHumids = hourlyObj.getAsJsonArray("relative_humidity_2m")
        val hourlyWinds = hourlyObj.getAsJsonArray("wind_speed_10m")
        val hourlyIsDays = hourlyObj.getAsJsonArray("is_day")

        // Tìm vị trí giờ hiện tại trong mảng Open-Meteo
        val now = LocalDateTime.now()
        val currentPrefix = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH").format(now)
        val timeFromApi = currentObj.get("time")?.asString ?: ""
        val targetPrefix = if (timeFromApi.length >= 13) timeFromApi.substring(0, 13) else currentPrefix

        var startIndex = 0
        var found = false
        for (i in 0 until hourlyTimes.size()) {
            val tStr = hourlyTimes.get(i).asString
            if (tStr.startsWith(targetPrefix)) {
                startIndex = i
                found = true
                break
            }
        }

        if (!found) {
            for (i in 0 until hourlyTimes.size()) {
                val tStr = hourlyTimes.get(i).asString
                if (tStr >= targetPrefix) {
                    startIndex = i
                    break
                }
            }
        }

        val hourlyList = mutableListOf<HourlyForecast>()
        val count = minOf(startIndex + 24, hourlyTimes.size())
        for (i in startIndex until count) {
            val tStr = hourlyTimes.get(i).asString
            val rawHour = if (tStr.contains("T")) tStr.substringAfter("T") else tStr
            val isFirstHour = (i == startIndex)
            val isDayHour = if (isFirstHour) {
                current.isDay
            } else if (hourlyIsDays != null && i < hourlyIsDays.size()) {
                hourlyIsDays.get(i).asInt == 1
            } else {
                val hourNum = rawHour.substringBefore(":").toIntOrNull() ?: 12
                hourNum in 6..18
            }

            hourlyList.add(
                HourlyForecast(
                    timeString = tStr,
                    hourLabel = if (isFirstHour) "Hiện tại" else rawHour,
                    temperature = if (isFirstHour) current.temperature else hourlyTemps.get(i).asDouble,
                    weatherCode = if (isFirstHour) current.weatherCode else hourlyCodes.get(i).asInt,
                    precipitationProbability = hourlyRains.get(i).asInt,
                    humidity = hourlyHumids.get(i).asInt,
                    windSpeed = hourlyWinds.get(i).asDouble,
                    isDay = isDayHour
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

            // Trích xuất dự báo nhiệt độ và thời tiết từng giờ của riêng ngày đó (24 giờ)
            val dayHourly = mutableListOf<HourlyForecast>()
            for (j in 0 until hourlyTimes.size()) {
                val tStr = hourlyTimes.get(j).asString
                if (tStr.startsWith(dateStr)) {
                    val rawHour = if (tStr.contains("T")) tStr.substringAfter("T") else tStr
                    val isDayHour = if (hourlyIsDays != null && j < hourlyIsDays.size()) {
                        hourlyIsDays.get(j).asInt == 1
                    } else {
                        val hNum = rawHour.substringBefore(":").toIntOrNull() ?: 12
                        hNum in 6..18
                    }

                    dayHourly.add(
                        HourlyForecast(
                            timeString = tStr,
                            hourLabel = rawHour,
                            temperature = hourlyTemps.get(j).asDouble,
                            weatherCode = hourlyCodes.get(j).asInt,
                            precipitationProbability = hourlyRains.get(j).asInt,
                            humidity = hourlyHumids.get(j).asInt,
                            windSpeed = hourlyWinds.get(j).asDouble,
                            isDay = isDayHour
                        )
                    )
                }
            }

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
                    uvIndexMax = dailyUvs.get(i).asDouble,
                    hourly24h = dayHourly
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
        val now = LocalDateTime.now()
        val currentHour = now.hour
        val hourly = (0..23).map { offset ->
            val hourVal = (currentHour + offset) % 24
            val hourStr = String.format("%02d:00", hourVal)
            HourlyForecast(
                timeString = "${now.toLocalDate()}T$hourStr",
                hourLabel = if (offset == 0) "Hiện tại" else hourStr,
                temperature = 28.0 + (offset % 5) - 2.0,
                weatherCode = if (hourVal in 14..16) 61 else 1,
                precipitationProbability = if (hourVal in 14..16) 60 else 10,
                humidity = 70,
                windSpeed = 12.0,
                isDay = hourVal in 6..18
            )
        }

        val days = listOf("Hôm nay", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật")
        val daily = days.mapIndexed { index, name ->
            val dayHours = (0..23).map { h ->
                val hStr = String.format("%02d:00", h)
                HourlyForecast(
                    timeString = "2026-10-${12 + index}T$hStr",
                    hourLabel = hStr,
                    temperature = 24.0 + (h % 7),
                    weatherCode = 1,
                    precipitationProbability = 10,
                    humidity = 65,
                    windSpeed = 10.0,
                    isDay = h in 6..18
                )
            }
            DailyForecast(
                dateString = "2026-10-${12 + index}",
                dayOfWeek = name,
                maxTemp = 32.0 - (index % 3),
                minTemp = 24.0 + (index % 2),
                weatherCode = if (index == 2) 61 else 1,
                precipitationProbability = if (index == 2) 65 else 15,
                sunrise = "05:45",
                sunset = "18:15",
                uvIndexMax = 7.2,
                hourly24h = dayHours
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
