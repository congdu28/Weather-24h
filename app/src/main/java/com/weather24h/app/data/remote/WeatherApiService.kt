package com.weather24h.app.data.remote

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.weather24h.app.data.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return@withContext CityLocation.POPULAR_CITIES

        val normQuery = CityLocation.normalizeVietnamese(trimmed)
        // Nếu người dùng nhập "xã Hoằng Thanh", "huyện Hoằng Hóa"..., chỉ lấy kết quả tỉnh/TP lớn khi thực sự khớp tên
        val isSpecificCommuneQuery = normQuery.startsWith("xa ") ||
                normQuery.startsWith("phuong ") ||
                normQuery.startsWith("thi tran ") ||
                normQuery.startsWith("huyen ") ||
                normQuery.startsWith("quan ") ||
                normQuery.startsWith("thon ")

        val localMatches = if (isSpecificCommuneQuery) {
            emptyList()
        } else {
            CityLocation.VIETNAM_MAJOR_CITIES.filter { city ->
                val normName = CityLocation.normalizeVietnamese(city.name)
                val normAdmin = city.admin1?.let { CityLocation.normalizeVietnamese(it) } ?: ""
                normName.contains(normQuery) || normAdmin.contains(normQuery) ||
                (normQuery == "hcm" && normName.contains("ho chi minh")) ||
                (normQuery == "tphcm" && normName.contains("ho chi minh")) ||
                (normQuery == "sg" && normName.contains("ho chi minh")) ||
                (normQuery == "saigon" && normName.contains("ho chi minh")) ||
                (normQuery == "hn" && normName.contains("ha noi")) ||
                (normQuery == "dn" && normName.contains("da nang")) ||
                (normQuery == "hp" && normName.contains("hai phong")) ||
                (normQuery == "dl" && normName.contains("da lat"))
            }
        }

        // Chạy song song Open-Meteo Geocoding và OpenStreetMap Nominatim (hỗ trợ cấp xã/phường/huyện chi tiết tại Việt Nam)
        val openMeteoDeferred = async {
            try {
                val cleanedForOpenMeteo = trimmed.replace(
                    Regex("^(phường|xã|thị trấn|quận|huyện|thị xã)\\s+", RegexOption.IGNORE_CASE),
                    ""
                ).trim().ifBlank { trimmed }
                val encoded = URLEncoder.encode(cleanedForOpenMeteo, "UTF-8")
                val url = "https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=10&language=vi&format=json"
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: return@async emptyList()

                val json = gson.fromJson(responseBody, JsonObject::class.java)
                val results = json.getAsJsonArray("results") ?: return@async emptyList()

                val list = mutableListOf<CityLocation>()
                for (elem in results) {
                    val obj = elem.asJsonObject
                    val name = obj.get("name")?.asString ?: continue
                    val lat = obj.get("latitude")?.asDouble ?: continue
                    val lon = obj.get("longitude")?.asDouble ?: continue
                    list.add(
                        CityLocation(
                            name = name,
                            country = obj.get("country")?.asString ?: "",
                            admin1 = obj.get("admin1")?.asString,
                            latitude = lat,
                            longitude = lon
                        )
                    )
                }
                list
            } catch (_: Exception) {
                emptyList()
            }
        }

        val nominatimDeferred = async {
            try {
                val encoded = URLEncoder.encode(trimmed, "UTF-8")
                val osmUrl = "https://nominatim.openstreetmap.org/search?q=$encoded&format=json&addressdetails=1&limit=6&accept-language=vi"
                val osmRequest = Request.Builder()
                    .url(osmUrl)
                    .header("User-Agent", "Weather24h-App/1.0 (Android; vi-VN)")
                    .build()
                val osmResponse = client.newCall(osmRequest).execute()
                val osmBody = osmResponse.body?.string() ?: return@async emptyList()
                val arr = gson.fromJson(osmBody, JsonArray::class.java) ?: return@async emptyList()

                val list = mutableListOf<CityLocation>()
                for (elem in arr) {
                    if (!elem.isJsonObject) continue
                    val obj = elem.asJsonObject
                    val lat = obj.get("lat")?.asString?.toDoubleOrNull() ?: continue
                    val lon = obj.get("lon")?.asString?.toDoubleOrNull() ?: continue
                    val addr = obj.getAsJsonObject("address")
                    val rawName = obj.get("name")?.asString?.trim()
                        ?: obj.get("display_name")?.asString?.substringBefore(",")?.trim()
                        ?: continue

                    val ward = addr?.get("suburb")?.asString
                        ?: addr?.get("quarter")?.asString
                        ?: addr?.get("village")?.asString
                        ?: addr?.get("town")?.asString
                        ?: addr?.get("hamlet")?.asString
                        ?: rawName
                    val district = addr?.get("county")?.asString
                        ?: addr?.get("district")?.asString
                        ?: addr?.get("city_district")?.asString
                        ?: addr?.get("municipality")?.asString
                    val province = addr?.get("state")?.asString
                        ?: addr?.get("province")?.asString
                        ?: addr?.get("city")?.asString
                    val country = addr?.get("country")?.asString ?: "Việt Nam"

                    val formatted = formatHierarchyLocation(ward, district, province, country)
                    val cleanAdmin = compactVietnameseAdministrativeName(province) ?: compactVietnameseAdministrativeName(district)

                    list.add(
                        CityLocation(
                            name = formatted.first,
                            country = country,
                            admin1 = cleanAdmin,
                            latitude = lat,
                            longitude = lon
                        )
                    )
                }
                list
            } catch (_: Exception) {
                emptyList()
            }
        }

        val openMeteoList = openMeteoDeferred.await()
        val nominatimList = nominatimDeferred.await()

        val merged = mutableListOf<CityLocation>()
        // Nếu người dùng tìm cụ thể cấp xã/phường/huyện, ưu tiên kết quả chi tiết từ Nominatim lên đầu
        val orderedSources = if (isSpecificCommuneQuery) {
            nominatimList + openMeteoList + localMatches
        } else {
            localMatches + nominatimList + openMeteoList
        }

        for (candidate in orderedSources) {
            val isDuplicate = merged.any { existing ->
                (existing.name.equals(candidate.name, ignoreCase = true) &&
                    kotlin.math.abs(existing.latitude - candidate.latitude) < 0.03 &&
                    kotlin.math.abs(existing.longitude - candidate.longitude) < 0.03) ||
                (kotlin.math.abs(existing.latitude - candidate.latitude) < 0.008 &&
                    kotlin.math.abs(existing.longitude - candidate.longitude) < 0.008)
            }
            if (!isDuplicate) {
                merged.add(candidate)
            }
        }

        merged.ifEmpty {
            CityLocation.POPULAR_CITIES.filter {
                CityLocation.normalizeVietnamese(it.name).contains(normQuery)
            }
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

                                    if (types.contains("sublocality_level_1") ||
                                        types.contains("sublocality") ||
                                        types.contains("administrative_area_level_3") ||
                                        types.contains("neighborhood")
                                    ) {
                                        if (gWard == null) gWard = longName
                                    } else if (types.contains("administrative_area_level_2")) {
                                        if (gDistrict == null) gDistrict = longName
                                    } else if (types.contains("administrative_area_level_1")) {
                                        if (gProvince == null) gProvince = longName
                                    } else if (types.contains("locality") && gWard == null) {
                                        gWard = longName
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
                    val segments = fullLine.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    var wardIndex = -1
                    for ((idx, seg) in segments.withIndex()) {
                        val lower = seg.lowercase()
                        if (ward == null && (
                            lower.startsWith("phường") || lower.startsWith("xã") ||
                            lower.startsWith("thị trấn") || lower.startsWith("p.") ||
                            lower.startsWith("x.") || lower.startsWith("tt.") ||
                            lower.contains("phường ") || lower.contains("xã ")
                        )) {
                            ward = seg
                            wardIndex = idx
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

                    // Nếu tìm thấy Xã/Phường ở segments[wardIndex] nhưng Huyện tiếp theo không có chữ "Huyện" ở đầu
                    // (ví dụ: "Xã Hoằng Thanh, Hoằng Hóa, Thanh Hóa, Việt Nam")
                    if (wardIndex >= 0 && district == null && wardIndex + 2 < segments.size) {
                        val candidateDistrict = segments[wardIndex + 1]
                        val cleanProv = compactVietnameseAdministrativeName(province)
                        val cleanCand = compactVietnameseAdministrativeName(candidateDistrict)
                        if (!cleanCand.isNullOrBlank() &&
                            !cleanCand.equals(cleanProv, ignoreCase = true) &&
                            !cleanCand.equals("Việt Nam", ignoreCase = true)
                        ) {
                            district = candidateDistrict
                        }
                    }
                }

                if (district == null && addr.locality != null && !isCentralCity(addr.locality!!)) {
                    val cleanLoc = compactVietnameseAdministrativeName(addr.locality)
                    val cleanProv = compactVietnameseAdministrativeName(province)
                    val cleanWard = compactVietnameseAdministrativeName(ward)
                    if (!cleanLoc.equals(cleanProv, ignoreCase = true) &&
                        !cleanLoc.equals(cleanWard, ignoreCase = true)
                    ) {
                        district = addr.locality
                    }
                }

                if (!ward.isNullOrBlank() || !district.isNullOrBlank() || !province.isNullOrBlank()) {
                    return@withContext formatHierarchyLocation(ward, district, province, addr.countryName ?: "Việt Nam")
                }
            }
        } catch (_: Exception) {}

        // 3. Dự phòng qua OpenStreetMap Nominatim Reverse Geocoding
        try {
            val osmUrl = "https://nominatim.openstreetmap.org/reverse?lat=$latitude&lon=$longitude&format=json&addressdetails=1&accept-language=vi"
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
                        ?: addressObj.get("town")?.asString
                        ?: addressObj.get("hamlet")?.asString
                    val osmDistrict = addressObj.get("city_district")?.asString
                        ?: addressObj.get("district")?.asString
                        ?: addressObj.get("county")?.asString
                        ?: addressObj.get("municipality")?.asString
                    val osmProvince = addressObj.get("state")?.asString
                        ?: addressObj.get("province")?.asString
                        ?: addressObj.get("city")?.asString
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

    internal fun formatHierarchyLocation(
        ward: String?,
        district: String?,
        province: String?,
        country: String = "Việt Nam"
    ): Pair<String, String> {
        val cleanWard = compactVietnameseAdministrativeName(ward)
        val cleanDistrict = compactVietnameseAdministrativeName(district)
        val cleanProvince = compactVietnameseAdministrativeName(province)
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

    private fun compactVietnameseAdministrativeName(value: String?): String? {
        val name = value?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return name.replace(
            Regex("^(phường|xã|thị trấn|quận|huyện|thị xã|thành phố|tỉnh)\\s+", RegexOption.IGNORE_CASE),
            ""
        ).trim().ifBlank { name }
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
        val hourlyTimes = hourlyObj?.getAsJsonArray("time") ?: JsonArray()
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

        val getEffectiveHourlyRainProb = { idx: Int, temp: Double, wCode: Int, precipMm: Double?, isDayHour: Boolean ->
            val rawProb = if (hourlyRains != null && idx < hourlyRains.size() && !hourlyRains.get(idx).isJsonNull) {
                hourlyRains.get(idx).asInt
            } else {
                -1
            }
            if (rawProb !in 0..100) {
                -1
            } else if (precipMm != null) {
                val isSevereCode = wCode in listOf(63, 65, 80, 81, 82, 95, 96, 99)
                when {
                    // Khi lượng mưa thực tế = 0.0mm và không phải mã mưa rào/dông:
                    // khử nhiễu xác suất mưa Ensemble lưới rộng (ven biển) bị vọt ảo lên 60-80%
                    precipMm <= 0.05 && !isSevereCode -> {
                        if (wCode in 0..3) minOf(rawProb, 15) else minOf(rawProb, 25)
                    }
                    // Khi lượng mưa <= 0.15mm (vết ẩm) giữa ban ngày trời nắng nóng >= 28°C:
                    precipMm <= 0.15 && isDayHour && temp >= 28.0 && wCode !in listOf(63, 65, 81, 82, 95, 96, 99) -> {
                        minOf(rawProb, 30)
                    }
                    else -> rawProb
                }
            } else {
                rawProb
            }
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

            val hTemp = hourlyTemps.get(i).asDouble
            val hCode = hourlyCodes?.get(i)?.asInt ?: current.weatherCode
            val hPrecip = hourlyPrecips?.takeIf { i < it.size() }?.get(i)?.takeUnless { it.isJsonNull }?.asDouble
            val rainProb = getEffectiveHourlyRainProb(i, hTemp, hCode, hPrecip, isDayHour)

            hourlyList.add(
                HourlyForecast(
                    timeString = tStr,
                    hourLabel = rawHour,
                    temperature = hTemp,
                    weatherCode = hCode,
                    precipitationProbability = rainProb,
                    humidity = hourlyHumids?.get(i)?.asInt ?: current.humidity,
                    windSpeed = hourlyWinds?.get(i)?.asDouble ?: current.windSpeed,
                    isDay = isDayHour,
                    precipitationMm = hPrecip
                )
            )
        }
        if (hourlyList.isEmpty()) throw IllegalStateException("Open-Meteo trả dự báo giờ rỗng")

        // Parse Daily 7-day
        val dailyTimes = dailyObj?.getAsJsonArray("time") ?: JsonArray()
        val dailyCodes = dailyObj?.getAsJsonArray("weather_code")
        val dailyMaxs = dailyObj?.getAsJsonArray("temperature_2m_max")
        val dailyMins = dailyObj?.getAsJsonArray("temperature_2m_min")
        val dailySunrises = dailyObj?.getAsJsonArray("sunrise")
        val dailySunsets = dailyObj?.getAsJsonArray("sunset")
        val dailyUvs = dailyObj?.getAsJsonArray("uv_index_max")
        val dailyRains = dailyObj?.getAsJsonArray("precipitation_probability_max")
        val dailyPrecipSums = dailyObj?.getAsJsonArray("precipitation_sum")

        val getRawDailyRainProb = { idx: Int ->
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
            val precipSum = dailyPrecipSums?.takeIf { i < it.size() }?.get(i)?.takeUnless { it.isJsonNull }?.asDouble

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
                    val hTemp = hourlyTemps?.get(j)?.asDouble ?: 25.0
                    val hCode = hourlyCodes?.get(j)?.asInt ?: 1
                    val hPrecip = hourlyPrecips?.takeIf { j < it.size() }?.get(j)?.takeUnless { it.isJsonNull }?.asDouble

                    dayHourly.add(
                        HourlyForecast(
                            timeString = tStr,
                            hourLabel = rawHour,
                            temperature = hTemp,
                            weatherCode = hCode,
                            precipitationProbability = getEffectiveHourlyRainProb(j, hTemp, hCode, hPrecip, isDayHour),
                            humidity = hourlyHumids?.get(j)?.asInt ?: 70,
                            windSpeed = hourlyWinds?.get(j)?.asDouble ?: 10.0,
                            isDay = isDayHour,
                            precipitationMm = hPrecip
                        )
                    )
                }
            }

            val rawDailyProb = getRawDailyRainProb(i)
            val effectiveDailyRainProb = if (rawDailyProb !in 0..100) {
                -1
            } else if (dayHourly.isNotEmpty()) {
                val relevantHours = if (i == 0) {
                    dayHourly.filter { it.timeString >= targetPrefix }.ifEmpty { dayHourly }
                } else {
                    dayHourly
                }
                val totalMm = precipSum ?: dayHourly.sumOf { it.precipitationMm ?: 0.0 }
                val maxHourlyProb = relevantHours.maxOfOrNull { it.precipitationProbability }?.takeIf { it >= 0 }
                val hasMeasurableHour = relevantHours.any { (it.precipitationMm ?: 0.0) >= 0.25 }

                when {
                    // Nếu cả ngày lượng mưa <= 0.3mm và không có giờ nào mưa >= 0.25mm:
                    // tránh việc báo xác suất mưa 70-80% cho một ngày thực tế nắng ráo
                    totalMm <= 0.3 && !hasMeasurableHour -> {
                        minOf(maxHourlyProb ?: rawDailyProb, 25)
                    }
                    // Nếu tổng lượng mưa nhỏ (< 1.0mm): lấy theo max xác suất mưa đã hiệu chỉnh của các giờ trong ngày
                    totalMm < 1.0 && maxHourlyProb != null -> {
                        minOf(rawDailyProb, maxHourlyProb)
                    }
                    // Với ngày Hôm nay, ưu tiên xác suất mưa của các khung giờ còn lại từ hiện tại
                    i == 0 && maxHourlyProb != null -> {
                        maxHourlyProb
                    }
                    else -> rawDailyProb
                }
            } else {
                if (precipSum != null && precipSum <= 0.2) minOf(rawDailyProb, 25) else rawDailyProb
            }

            dailyList.add(
                DailyForecast(
                    dateString = dateStr,
                    dayOfWeek = dayOfWeek,
                    maxTemp = dailyMaxs.get(i).asDouble,
                    minTemp = dailyMins.get(i).asDouble,
                    weatherCode = dailyCodes?.get(i)?.asInt ?: 1,
                    precipitationProbability = effectiveDailyRainProb,
                    sunrise = sunriseStr,
                    sunset = sunsetStr,
                    uvIndexMax = dailyUvs?.get(i)?.asDouble ?: 5.0,
                    hourly24h = dayHourly,
                    sourceName = "Open-Meteo",
                    precipitationSum = precipSum
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
