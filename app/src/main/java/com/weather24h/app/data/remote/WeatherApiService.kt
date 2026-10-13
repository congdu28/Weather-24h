package com.weather24h.app.data.remote

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.weather24h.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
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

    suspend fun getFullWeather(
        location: CityLocation,
        model: WeatherForecastModel = WeatherForecastModel.BEST_MATCH
    ): WeatherData = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.open-meteo.com/v1/forecast?" +
                    "latitude=${location.latitude}&" +
                    "longitude=${location.longitude}&" +
                    "current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,precipitation,weather_code,surface_pressure,wind_speed_10m,wind_direction_10m,uv_index,visibility&" +
                    "hourly=temperature_2m,relative_humidity_2m,precipitation_probability,precipitation,weather_code,wind_speed_10m,is_day&" +
                    "daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max,precipitation_probability_max,precipitation_sum&" +
                    "timezone=auto&" +
                    "forecast_days=7&" +
                    "models=${model.apiValue}"

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
                    if (currentAqi?.get("pm2_5") != null && currentAqi.get("pm10") != null && currentAqi.get("european_aqi") != null) {
                        airQuality = AirQuality(
                            pm25 = currentAqi.get("pm2_5").asDouble,
                            pm10 = currentAqi.get("pm10").asDouble,
                            aqiIndex = currentAqi.get("european_aqi").asInt,
                            aqiScale = "European AQI"
                        )
                    }
                }
            } catch (_: Exception) {}

            parseWeatherData(location, json, airQuality, model)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw IllegalStateException("Không thể tải dữ liệu Open-Meteo", e)
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

    suspend fun getAddressFromCoordinates(
        context: android.content.Context,
        latitude: Double,
        longitude: Double,
        apiKey: String? = null
    ): Pair<String, String> = withContext(Dispatchers.IO) {
        // 1. Nếu có Google Maps / Gemini API Key, thử tra cứu Google Maps Geocoding API chính xác tuyệt đối
        if (!apiKey.isNullOrBlank()) {
            try {
                val googleUrl = "https://maps.googleapis.com/maps/api/geocode/json?latlng=$latitude,$longitude&language=vi&key=$apiKey"
                val request = Request.Builder().url(googleUrl).build()
                val response = client.newCall(request).execute()
                val body = response.body?.string()
                if (body != null) {
                    val json = gson.fromJson(body, JsonObject::class.java)
                    val status = json.get("status")?.asString
                    if (status == "OK") {
                        val results = json.getAsJsonArray("results")
                        if (results != null && results.size() > 0) {
                            val firstResult = results[0].asJsonObject
                            val components = firstResult.getAsJsonArray("address_components")
                            var gWard: String? = null
                            var gDistrict: String? = null
                            var gProvince: String? = null
                            var gCountry: String? = null

                            if (components != null) {
                                for (comp in components) {
                                    val obj = comp.asJsonObject
                                    val types = obj.getAsJsonArray("types")?.map { it.asString } ?: emptyList()
                                    val longName = obj.get("long_name")?.asString ?: ""

                                    if (types.contains("sublocality_level_1") || types.contains("sublocality") || types.contains("neighborhood")) {
                                        if (gWard == null) gWard = longName
                                    } else if (types.contains("administrative_area_level_2")) {
                                        if (gDistrict == null) gDistrict = longName
                                    } else if (types.contains("administrative_area_level_1")) {
                                        if (gProvince == null) gProvince = longName
                                    } else if (types.contains("country")) {
                                        if (gCountry == null) gCountry = longName
                                    }
                                }
                            }

                            if (!gWard.isNullOrBlank() || !gDistrict.isNullOrBlank() || !gProvince.isNullOrBlank()) {
                                return@withContext formatHierarchyLocation(gWard, gDistrict, gProvince, gCountry ?: "Việt Nam")
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        // 2. Dùng Geocoder gốc của Android (Google Play Services Geocoder) với Locale tiếng Việt
        try {
            val geocoder = android.location.Geocoder(context, java.util.Locale("vi", "VN"))
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                var ward: String? = addr.subLocality
                var district: String? = addr.subAdminArea
                var province: String? = addr.adminArea

                // Phân tích chuỗi địa chỉ đầy đủ (getAddressLine) để bóc tách Phường/Xã -> Quận/Huyện -> Tỉnh
                val fullLine = addr.getAddressLine(0)
                if (!fullLine.isNullOrBlank()) {
                    val segments = fullLine.split(",").map { it.trim() }
                    for (seg in segments) {
                        val lower = seg.lowercase()
                        if (ward == null && (
                            lower.startsWith("phường") || lower.startsWith("xã") ||
                            lower.startsWith("thị trấn") || lower.startsWith("p.") ||
                            lower.startsWith("x.") || lower.startsWith("tt.") ||
                            lower.contains("phường ") || lower.contains("xã ")
                        )) {
                            ward = seg
                        } else if (district == null && (
                            lower.startsWith("quận") || lower.startsWith("huyện") ||
                            lower.startsWith("thị xã") || lower.startsWith("q.") ||
                            lower.startsWith("h.") || lower.startsWith("tx.") ||
                            (lower.startsWith("thành phố") && !isCentralCity(seg)) ||
                            (lower.startsWith("tp.") && !isCentralCity(seg))
                        )) {
                            district = seg
                        } else if (province == null && (
                            lower.startsWith("tỉnh") || isCentralCity(seg)
                        )) {
                            province = seg
                        }
                    }
                }

                if (district == null && addr.locality != null && !isCentralCity(addr.locality!!)) {
                    district = addr.locality
                }

                if (!ward.isNullOrBlank() || !district.isNullOrBlank() || !province.isNullOrBlank()) {
                    return@withContext formatHierarchyLocation(ward, district, province, addr.countryName ?: "Việt Nam")
                }
            }
        } catch (_: Exception) {}

        // 3. Dự phòng qua OpenStreetMap Nominatim Reverse Geocoding
        try {
            val osmUrl = "https://nominatim.openstreetmap.org/reverse?lat=$latitude&lon=$longitude&format=json&accept-language=vi"
            val osmRequest = Request.Builder()
                .url(osmUrl)
                .header("User-Agent", "Weather24h-App/1.0 (Android; vi-VN)")
                .build()
            val osmResponse = client.newCall(osmRequest).execute()
            val osmBody = osmResponse.body?.string()
            if (osmBody != null) {
                val json = gson.fromJson(osmBody, JsonObject::class.java)
                val addressObj = json.getAsJsonObject("address")
                if (addressObj != null) {
                    val osmWard = addressObj.get("suburb")?.asString
                        ?: addressObj.get("quarter")?.asString
                        ?: addressObj.get("neighbourhood")?.asString
                        ?: addressObj.get("village")?.asString
                    val osmDistrict = addressObj.get("city_district")?.asString
                        ?: addressObj.get("district")?.asString
                        ?: addressObj.get("county")?.asString
                    val osmProvince = addressObj.get("city")?.asString
                        ?: addressObj.get("state")?.asString
                        ?: addressObj.get("province")?.asString
                    val osmCountry = addressObj.get("country")?.asString ?: "Việt Nam"

                    if (!osmWard.isNullOrBlank() || !osmDistrict.isNullOrBlank() || !osmProvince.isNullOrBlank()) {
                        return@withContext formatHierarchyLocation(osmWard, osmDistrict, osmProvince, osmCountry)
                    }
                }
            }
        } catch (_: Exception) {}

        // 4. Dự phòng qua REST Reverse Geocoding API BigDataCloud
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

                val finalWard = adminWard ?: locality.takeIf { it.contains("phường", ignoreCase = true) || it.contains("xã", ignoreCase = true) }
                val finalDistrict = adminDistrict ?: city.takeIf { !isCentralCity(it) }

                return@withContext formatHierarchyLocation(finalWard, finalDistrict, principalSubdivision, countryName)
            }
        } catch (_: Exception) {}

        Pair("Vị trí của bạn", "GPS")
    }

    private fun isCentralCity(name: String): Boolean {
        val lower = name.lowercase()
        return lower.contains("hà nội") ||
                lower.contains("hồ chí minh") ||
                lower.contains("đà nẵng") ||
                lower.contains("hải phòng") ||
                lower.contains("cần thơ")
    }

    private fun formatHierarchyLocation(
        ward: String?,
        district: String?,
        province: String?,
        country: String = "Việt Nam"
    ): Pair<String, String> {
        val cleanWard = ward?.trim()?.takeIf { it.isNotBlank() }
        val cleanDistrict = district?.trim()?.takeIf { it.isNotBlank() }
        val cleanProvince = province?.trim()?.takeIf { it.isNotBlank() }
        val cleanCountry = country.trim().ifBlank { "Việt Nam" }

        // Ưu tiên hiển thị hành chính Việt Nam: Cấp Phường/Xã -> Cấp Quận/Huyện -> Cấp Tỉnh/TP
        val primaryName = when {
            cleanWard != null && cleanDistrict != null && !cleanWard.equals(cleanDistrict, ignoreCase = true) -> {
                "$cleanWard, $cleanDistrict"
            }
            cleanWard != null && cleanProvince != null && !cleanWard.equals(cleanProvince, ignoreCase = true) -> {
                "$cleanWard, $cleanProvince"
            }
            cleanDistrict != null && cleanProvince != null && !cleanDistrict.equals(cleanProvince, ignoreCase = true) -> {
                "$cleanDistrict, $cleanProvince"
            }
            cleanWard != null -> cleanWard
            cleanDistrict != null -> cleanDistrict
            cleanProvince != null -> cleanProvince
            else -> "Vị trí của bạn"
        }

        val secondaryLocation = when {
            cleanProvince != null && !primaryName.contains(cleanProvince, ignoreCase = true) -> {
                "$cleanProvince, $cleanCountry"
            }
            cleanDistrict != null && !primaryName.contains(cleanDistrict, ignoreCase = true) -> {
                "$cleanDistrict, $cleanCountry"
            }
            else -> cleanCountry
        }

        return Pair(primaryName, secondaryLocation)
    }

    internal fun parseWeatherData(
        location: CityLocation,
        json: JsonObject,
        aqi: AirQuality?,
        model: WeatherForecastModel = WeatherForecastModel.BEST_MATCH
    ): WeatherData {
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
            uvIndex = currentObj.get("uv_index")?.takeUnless { it.isJsonNull }?.asDouble
                ?: dailyObj.getAsJsonArray("uv_index_max")?.get(0)?.takeUnless { it.isJsonNull }?.asDouble ?: 5.0,
            surfacePressure = currentObj.get("surface_pressure").asDouble,
            visibilityKm = currentObj.get("visibility")?.takeUnless { it.isJsonNull }?.asDouble?.div(1000.0) ?: 10.0,
            precipitation = currentObj.get("precipitation").asDouble,
            isDay = currentObj.get("is_day").asInt == 1,
            time = currentObj.get("time").asString
        )

        // Parse Hourly 24h chuẩn xác theo giờ hiện tại
        val hourlyTimes = hourlyObj?.getAsJsonArray("time") ?: com.google.gson.JsonArray()
        val hourlyTemps = hourlyObj?.getAsJsonArray("temperature_2m")
        if (hourlyTimes.size() == 0 || hourlyTemps == null) {
            throw IllegalStateException("Open-Meteo thiếu dự báo theo giờ")
        }
        val hourlyCodes = hourlyObj?.getAsJsonArray("weather_code")
        val hourlyRains = hourlyObj?.getAsJsonArray("precipitation_probability")
        val hourlyPrecips = hourlyObj?.getAsJsonArray("precipitation")
        val hourlyHumids = hourlyObj?.getAsJsonArray("relative_humidity_2m")
        val hourlyWinds = hourlyObj?.getAsJsonArray("wind_speed_10m")
        val hourlyIsDays = hourlyObj?.getAsJsonArray("is_day")

        val getHourlyRainProb = { idx: Int ->
            val prob = if (hourlyRains != null && idx < hourlyRains.size() && !hourlyRains.get(idx).isJsonNull) {
                hourlyRains.get(idx).asInt
            } else {
                -1
            }
            // Lượng mưa không thể quy đổi thành xác suất. -1 biểu thị nguồn không cung cấp.
            if (prob in 0..100) prob else -1
        }

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
            if (i >= hourlyTemps.size() || hourlyTemps.get(i).isJsonNull) continue
            val tStr = hourlyTimes.get(i).asString
            val rawHour = if (tStr.contains("T")) tStr.substringAfter("T") else tStr
            val isDayHour = if (hourlyIsDays != null && i < hourlyIsDays.size()) {
                hourlyIsDays.get(i).asInt == 1
            } else {
                val hourNum = rawHour.substringBefore(":").toIntOrNull() ?: 12
                hourNum in 6..18
            }

            val rainProb = getHourlyRainProb(i)
            hourlyList.add(
                HourlyForecast(
                    timeString = tStr,
                    hourLabel = rawHour,
                    temperature = hourlyTemps.get(i).asDouble,
                    weatherCode = hourlyCodes?.get(i)?.asInt ?: current.weatherCode,
                    precipitationProbability = rainProb,
                    humidity = hourlyHumids?.get(i)?.asInt ?: current.humidity,
                    windSpeed = hourlyWinds?.get(i)?.asDouble ?: current.windSpeed,
                    isDay = isDayHour,
                    precipitationMm = hourlyPrecips?.takeIf { i < it.size() }?.get(i)?.takeUnless { it.isJsonNull }?.asDouble
                )
            )
        }
        if (hourlyList.isEmpty()) throw IllegalStateException("Open-Meteo trả dự báo giờ rỗng")

        // Parse Daily 7-day
        val dailyTimes = dailyObj?.getAsJsonArray("time") ?: com.google.gson.JsonArray()
        val dailyCodes = dailyObj?.getAsJsonArray("weather_code")
        val dailyMaxs = dailyObj?.getAsJsonArray("temperature_2m_max")
        val dailyMins = dailyObj?.getAsJsonArray("temperature_2m_min")
        val dailySunrises = dailyObj?.getAsJsonArray("sunrise")
        val dailySunsets = dailyObj?.getAsJsonArray("sunset")
        val dailyUvs = dailyObj?.getAsJsonArray("uv_index_max")
        val dailyRains = dailyObj?.getAsJsonArray("precipitation_probability_max")


        val getDailyRainProb = { idx: Int ->
            if (dailyRains != null && idx < dailyRains.size() && !dailyRains.get(idx).isJsonNull) {
                dailyRains.get(idx).asInt.takeIf { it in 0..100 } ?: -1
            } else {
                -1
            }
        }

        val dailyList = mutableListOf<DailyForecast>()
        val dCount = minOf(7, dailyTimes.size())
        for (i in 0 until dCount) {
            if (dailyMaxs == null || dailyMins == null || i >= dailyMaxs.size() ||
                i >= dailyMins.size() || dailyMaxs.get(i).isJsonNull || dailyMins.get(i).isJsonNull
            ) continue
            val dateStr = dailyTimes.get(i).asString
            val dayOfWeek = if (i == 0) "Hôm nay" else parseDayOfWeek(dateStr)
            val sunriseStr = dailySunrises?.get(i)?.asString?.substringAfter("T") ?: "06:00"
            val sunsetStr = dailySunsets?.get(i)?.asString?.substringAfter("T") ?: "18:00"

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
                            temperature = hourlyTemps?.get(j)?.asDouble ?: 25.0,
                            weatherCode = hourlyCodes?.get(j)?.asInt ?: 1,
                            precipitationProbability = getHourlyRainProb(j),
                            humidity = hourlyHumids?.get(j)?.asInt ?: 70,
                            windSpeed = hourlyWinds?.get(j)?.asDouble ?: 10.0,
                            isDay = isDayHour,
                            precipitationMm = hourlyPrecips?.takeIf { j < it.size() }?.get(j)?.takeUnless { it.isJsonNull }?.asDouble
                        )
                    )
                }
            }

            val dailyRainProb = getDailyRainProb(i)
            dailyList.add(
                DailyForecast(
                    dateString = dateStr,
                    dayOfWeek = dayOfWeek,
                    maxTemp = dailyMaxs.get(i).asDouble,
                    minTemp = dailyMins.get(i).asDouble,
                    weatherCode = dailyCodes?.get(i)?.asInt ?: 1,
                    precipitationProbability = dailyRainProb,
                    sunrise = sunriseStr,
                    sunset = sunsetStr,
                    uvIndexMax = dailyUvs?.get(i)?.asDouble ?: 5.0,
                    hourly24h = dayHourly,
                    sourceName = "Open-Meteo"
                )
            )
        }
        if (dailyList.isEmpty()) throw IllegalStateException("Open-Meteo trả dự báo ngày rỗng")

        val updatedStr = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy").format(
            LocalDateTime.parse(currentObj.get("time").asString)
        )

        return WeatherData(
            location = location,
            current = current,
            hourly24h = hourlyList,
            daily7Days = dailyList,
            airQuality = aqi,
            lastUpdated = updatedStr,
            forecastModel = model,
            providerName = "Open-Meteo (${model.title})"
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

}

