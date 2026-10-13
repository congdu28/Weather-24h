package com.weather24h.app.data.remote

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.weather24h.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Service tương tác với Google Maps Platform Weather API
 * Cung cấp dữ liệu từ mô hình AI dự báo thời tiết tiên tiến nhất: WeatherNext (Google DeepMind)
 * Dữ liệu đồng nhất 100% với Google Search và Google Pixel Weather.
 */
class GoogleWeatherApiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun getFullWeather(location: CityLocation, apiKey: String): WeatherData = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            throw IllegalArgumentException("Google Weather API Key trống")
        }

        val lat = location.latitude
        val lon = location.longitude

        // 1. Lấy Thời tiết hiện tại (Current Conditions)
        val currentUrl = "https://weather.googleapis.com/v1/currentConditions:lookup?" +
                "key=$apiKey&" +
                "location.latitude=$lat&" +
                "location.longitude=$lon&" +
                "unitsSystem=METRIC&" +
                "languageCode=vi"

        val currentReq = Request.Builder().url(currentUrl).build()
        val currentResp = client.newCall(currentReq).execute()
        if (!currentResp.isSuccessful) {
            val code = currentResp.code
            val errBody = currentResp.body?.string() ?: ""
            throw IllegalStateException("Google Weather API lỗi (HTTP $code): $errBody")
        }
        val currentBody = currentResp.body?.string() ?: throw IllegalStateException("Empty body from currentConditions")
        val currentJson = gson.fromJson(currentBody, JsonObject::class.java)

        // 2. Lấy Dự báo từng giờ (Hourly Forecast - 48h)
        val hourlyUrl = "https://weather.googleapis.com/v1/forecast/hours:lookup?" +
                "key=$apiKey&" +
                "location.latitude=$lat&" +
                "location.longitude=$lon&" +
                "unitsSystem=METRIC&" +
                "languageCode=vi&" +
                "hours=48"

        val hourlyReq = Request.Builder().url(hourlyUrl).build()
        val hourlyResp = client.newCall(hourlyReq).execute()
        val hourlyBody = if (hourlyResp.isSuccessful) hourlyResp.body?.string() else null
        val hourlyJson = if (hourlyBody != null) gson.fromJson(hourlyBody, JsonObject::class.java) else null

        // 3. Lấy Dự báo hàng ngày (Daily Forecast - 7 ngày)
        val dailyUrl = "https://weather.googleapis.com/v1/forecast/days:lookup?" +
                "key=$apiKey&" +
                "location.latitude=$lat&" +
                "location.longitude=$lon&" +
                "unitsSystem=METRIC&" +
                "languageCode=vi&" +
                "days=7"

        val dailyReq = Request.Builder().url(dailyUrl).build()
        val dailyResp = client.newCall(dailyReq).execute()
        val dailyBody = if (dailyResp.isSuccessful) dailyResp.body?.string() else null
        val dailyJson = if (dailyBody != null) gson.fromJson(dailyBody, JsonObject::class.java) else null

        // 4. Lấy AQI (Chất lượng không khí) từ Open-Meteo Air Quality để bổ trợ đầy đủ
        var airQuality: AirQuality? = null
        try {
            val aqiUrl = "https://air-quality-api.open-meteo.com/v1/air-quality?" +
                    "latitude=$lat&" +
                    "longitude=$lon&" +
                    "current=pm10,pm2_5,european_aqi"
            val aqiReq = Request.Builder().url(aqiUrl).build()
            val aqiResp = client.newCall(aqiReq).execute()
            val aqiStr = aqiResp.body?.string()
            if (aqiStr != null) {
                val aqiJson = gson.fromJson(aqiStr, JsonObject::class.java)
                val currAqi = aqiJson.getAsJsonObject("current")
                if (currAqi != null) {
                    airQuality = AirQuality(
                        pm25 = currAqi.get("pm2_5")?.asDouble ?: 18.0,
                        pm10 = currAqi.get("pm10")?.asDouble ?: 30.0,
                        aqiIndex = currAqi.get("european_aqi")?.asInt ?: 35
                    )
                }
            }
        } catch (_: Exception) {}

        // Chuyển đổi dữ liệu sang WeatherData
        parseGoogleWeatherData(location, currentJson, hourlyJson, dailyJson, airQuality)
    }

    private fun parseGoogleWeatherData(
        location: CityLocation,
        currentJson: JsonObject,
        hourlyJson: JsonObject?,
        dailyJson: JsonObject?,
        airQuality: AirQuality?
    ): WeatherData {
        // --- Current Weather ---
        val temp = currentJson.getAsJsonObject("temperature")?.get("degrees")?.asDouble ?: 0.0
        val feelsLike = currentJson.getAsJsonObject("feelsLikeTemperature")?.get("degrees")?.asDouble ?: temp
        val condObj = currentJson.getAsJsonObject("weatherCondition")
        val googleType = condObj?.get("type")?.asString
        val wmoCode = mapGoogleConditionToWmo(googleType)
        val humidity = currentJson.get("relativeHumidity")?.asInt ?: 50
        val windObj = currentJson.getAsJsonObject("wind")
        val windSpeed = windObj?.getAsJsonObject("speed")?.get("value")?.asDouble ?: 0.0
        val windDir = windObj?.getAsJsonObject("direction")?.get("degrees")?.asInt ?: 0
        val uv = currentJson.get("uvIndex")?.asDouble ?: 0.0
        val pressure = currentJson.getAsJsonObject("airPressure")?.get("meanSeaLevelMillibars")?.asDouble ?: 1013.25
        val visibility = currentJson.getAsJsonObject("visibility")?.getAsJsonObject("distance")?.get("value")?.asDouble ?: 10.0
        val precip = currentJson.getAsJsonObject("precipitation")?.getAsJsonObject("qpf")?.get("quantity")?.asDouble ?: 0.0
        val isDay = currentJson.get("isDaytime")?.asBoolean ?: true
        val timeNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

        val currentWeather = CurrentWeather(
            temperature = temp,
            feelsLike = feelsLike,
            weatherCode = wmoCode,
            humidity = humidity,
            windSpeed = windSpeed,
            windDirection = windDir,
            uvIndex = uv,
            surfacePressure = pressure,
            visibilityKm = visibility,
            precipitation = precip,
            isDay = isDay,
            time = timeNow
        )

        // --- All Hourly Forecast List ---
        val allHourly = mutableListOf<HourlyForecast>()
        val hourlyArr = hourlyJson?.getAsJsonArray("forecastHours")
        if (hourlyArr != null) {
            for (elem in hourlyArr) {
                val item = elem.asJsonObject
                val dt = item.getAsJsonObject("displayDateTime")
                val hour = dt?.get("hours")?.asInt ?: 0
                val year = dt?.get("year")?.asInt ?: LocalDate.now().year
                val month = dt?.get("month")?.asInt ?: LocalDate.now().monthValue
                val day = dt?.get("day")?.asInt ?: LocalDate.now().dayOfMonth

                val timeStr = String.format(Locale.US, "%04d-%02d-%02dT%02d:00", year, month, day, hour)
                val hourLabel = String.format(Locale.US, "%02d:00", hour)
                val hTemp = item.getAsJsonObject("temperature")?.get("degrees")?.asDouble ?: 0.0
                val hType = item.getAsJsonObject("weatherCondition")?.get("type")?.asString
                val hWmo = mapGoogleConditionToWmo(hType)
                val hProbObj = item.getAsJsonObject("precipitation")?.getAsJsonObject("probability")
                val hProb = hProbObj?.get("percent")?.asInt ?: hProbObj?.get("percentage")?.asInt ?: 0
                val hHumid = item.get("relativeHumidity")?.asInt ?: 50
                val hWind = item.getAsJsonObject("wind")?.getAsJsonObject("speed")?.get("value")?.asDouble ?: 0.0
                val hIsDay = item.get("isDaytime")?.asBoolean ?: true

                allHourly.add(
                    HourlyForecast(
                        timeString = timeStr,
                        hourLabel = hourLabel,
                        temperature = hTemp,
                        weatherCode = hWmo,
                        precipitationProbability = hProb,
                        humidity = hHumid,
                        windSpeed = hWind,
                        isDay = hIsDay
                    )
                )
            }
        }

        // Lấy 24 giờ tới cho trang chủ
        val first24h = allHourly.take(24)

        // --- Daily 7 Days Forecast ---
        val dailyList = mutableListOf<DailyForecast>()
        val dailyArr = dailyJson?.getAsJsonArray("forecastDays")
        val today = LocalDate.now()

        if (dailyArr != null) {
            for (i in 0 until dailyArr.size()) {
                val item = dailyArr.get(i).asJsonObject
                val dt = item.getAsJsonObject("displayDate")
                val year = dt?.get("year")?.asInt ?: today.year
                val month = dt?.get("month")?.asInt ?: today.monthValue
                val day = dt?.get("day")?.asInt ?: today.dayOfMonth
                val date = LocalDate.of(year, month, day)
                val dateStr = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)

                val dayOfWeek = if (date.isEqual(today)) "Hôm nay" else getVietnameseDayOfWeek(date)
                val maxT = item.getAsJsonObject("maxTemperature")?.get("degrees")?.asDouble ?: 0.0
                val minT = item.getAsJsonObject("minTemperature")?.get("degrees")?.asDouble ?: 0.0

                val dayPart = item.getAsJsonObject("daytimeForecast") ?: item.getAsJsonObject("nighttimeForecast")
                val dType = dayPart?.getAsJsonObject("weatherCondition")?.get("type")?.asString
                val dWmo = mapGoogleConditionToWmo(dType)
                val dProbObj = dayPart?.getAsJsonObject("precipitation")?.getAsJsonObject("probability")
                val dProb = dProbObj?.get("percent")?.asInt ?: dProbObj?.get("percentage")?.asInt ?: 0
                val dUv = dayPart?.get("uvIndex")?.asDouble ?: 5.0

                // Parse sunrise / sunset
                val sunObj = item.getAsJsonObject("sunEvents")
                val sunriseStr = formatIsoTimeToHourMin(sunObj?.get("sunriseTime")?.asString) ?: "06:00"
                val sunsetStr = formatIsoTimeToHourMin(sunObj?.get("sunsetTime")?.asString) ?: "18:00"

                // 24 giờ tương ứng cho ngày này
                val dayHourly = allHourly.filter { it.timeString.startsWith(dateStr) }

                dailyList.add(
                    DailyForecast(
                        dateString = dateStr,
                        dayOfWeek = dayOfWeek,
                        maxTemp = maxT,
                        minTemp = minT,
                        weatherCode = dWmo,
                        precipitationProbability = dProb,
                        sunrise = sunriseStr,
                        sunset = sunsetStr,
                        uvIndexMax = dUv,
                        hourly24h = dayHourly
                    )
                )
            }
        }

        val lastUpdated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy"))

        return WeatherData(
            location = location,
            current = currentWeather,
            hourly24h = first24h,
            daily7Days = dailyList,
            airQuality = airQuality,
            lastUpdated = lastUpdated,
            forecastModel = WeatherForecastModel.BEST_MATCH,
            isOffline = false,
            providerName = "Google Weather (WeatherNext AI)"
        )
    }


    private fun mapGoogleConditionToWmo(googleType: String?): Int {
        return when (googleType?.uppercase()?.trim()) {
            "CLEAR", "SUNNY" -> 0
            "MOSTLY_CLEAR" -> 1
            "PARTLY_CLOUDY" -> 2
            "MOSTLY_CLOUDY", "CLOUDY", "OVERCAST" -> 3
            "FOG", "HAZE", "SMOKE", "MIST" -> 45
            "DRIZZLE", "LIGHT_DRIZZLE" -> 51
            "HEAVY_DRIZZLE" -> 55
            "LIGHT_RAIN", "PATCHY_RAIN", "LIGHT_SHOWERS" -> 61
            "RAIN", "SHOWERS", "MODERATE_RAIN" -> 63
            "HEAVY_RAIN", "HEAVY_SHOWERS" -> 65
            "FREEZING_RAIN", "SLEET" -> 66
            "LIGHT_SNOW" -> 71
            "SNOW", "MODERATE_SNOW" -> 73
            "HEAVY_SNOW", "BLIZZARD" -> 75
            "THUNDERSTORM", "SCATTERED_THUNDERSTORMS", "SEVERE_THUNDERSTORM" -> 95
            "THUNDERSTORM_WITH_HAIL", "HAIL" -> 99
            else -> 2
        }
    }

    private fun getVietnameseDayOfWeek(date: LocalDate): String {
        return when (date.dayOfWeek) {
            java.time.DayOfWeek.MONDAY -> "Thứ Hai"
            java.time.DayOfWeek.TUESDAY -> "Thứ Ba"
            java.time.DayOfWeek.WEDNESDAY -> "Thứ Tư"
            java.time.DayOfWeek.THURSDAY -> "Thứ Năm"
            java.time.DayOfWeek.FRIDAY -> "Thứ Sáu"
            java.time.DayOfWeek.SATURDAY -> "Thứ Bảy"
            java.time.DayOfWeek.SUNDAY -> "Chủ Nhật"
            else -> ""
        }
    }

    private fun formatIsoTimeToHourMin(isoString: String?): String? {
        if (isoString.isNullOrBlank()) return null
        return try {
            val instant = Instant.parse(isoString)
            val ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            ldt.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (_: Exception) {
            null
        }
    }

    suspend fun testApiKey(apiKey: String): Result<String> = withContext(Dispatchers.IO) {
        val trimmed = apiKey.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Vui lòng nhập API Key"))
        }
        try {
            val testUrl = "https://weather.googleapis.com/v1/currentConditions:lookup?" +
                    "key=$trimmed&" +
                    "location.latitude=21.0285&" +
                    "location.longitude=105.8542&" +
                    "unitsSystem=METRIC&" +
                    "languageCode=vi"
            val req = Request.Builder().url(testUrl).build()
            val resp = client.newCall(req).execute()
            if (resp.isSuccessful) {
                Result.success("Kết nối thành công! Google Weather API đã sẵn sàng hoạt động.")
            } else {
                val code = resp.code
                val errBody = resp.body?.string() ?: ""
                val msg = when (code) {
                    400 -> "API Key không hợp lệ hoặc sai định dạng (HTTP 400)"
                    403 -> "API Key bị từ chối (HTTP 403). Hãy đảm bảo bạn đã bật 'Weather API' trong Google Cloud Console."
                    429 -> "Đã vượt quá hạn mức sử dụng (HTTP 429)"
                    else -> "Lỗi phản hồi từ Google (HTTP $code): $errBody"
                }
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối tới máy chủ Google: ${e.localizedMessage ?: e.message}"))
        }
    }
}

