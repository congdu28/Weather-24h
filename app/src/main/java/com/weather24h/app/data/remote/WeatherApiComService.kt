package com.weather24h.app.data.remote

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.weather24h.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.concurrent.TimeUnit

/** Dữ liệu hiện tại và ba ngày dự báo từ WeatherAPI.com. */
class WeatherApiComService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun getFullWeather(
        location: CityLocation,
        apiKey: String,
        fallbackOpenMeteo: suspend () -> WeatherData? = { null }
    ): WeatherData = withContext(Dispatchers.IO) {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isEmpty()) {
            throw IllegalArgumentException("WeatherAPI Key trống")
        }

        // Tải bổ sung song song từ Open-Meteo để không gây chậm trễ
        val openMeteoDeferred = async {
            try {
                fallbackOpenMeteo()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }
        }

        val lat = location.latitude
        val lon = location.longitude

        val url = "https://api.weatherapi.com/v1/forecast.json?" +
                "key=$trimmedKey&" +
                "q=$lat,$lon&" +
                "days=3&" +
                "aqi=yes&" +
                "lang=vi"

        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            val code = response.code
            val errBody = response.body?.string() ?: ""
            throw IllegalStateException("WeatherAPI lỗi (HTTP $code): $errBody")
        }

        val body = response.body?.string() ?: throw IllegalStateException("Empty body from WeatherAPI")
        val json = gson.fromJson(body, JsonObject::class.java)

        val openMeteoData = openMeteoDeferred.await()
        parseWeatherApiData(location, json, openMeteoData)
    }

    internal fun parseWeatherApiData(
        location: CityLocation,
        json: JsonObject,
        openMeteoSupplement: WeatherData?
    ): WeatherData {
        val locationObj = json.getAsJsonObject("location")
            ?: throw IllegalStateException("WeatherAPI thiếu thông tin địa điểm")
        val localTime = locationObj.get("localtime")?.asString
            ?: throw IllegalStateException("WeatherAPI thiếu giờ địa phương")
        val nowLdt = LocalDateTime.parse(localTime.replace(" ", "T"))
        val currentObj = json.getAsJsonObject("current") ?: throw IllegalStateException("Missing current object")
        val forecastDaysArr = json.getAsJsonObject("forecast")?.getAsJsonArray("forecastday")
            ?: throw IllegalStateException("WeatherAPI thiếu dữ liệu dự báo")
        if (forecastDaysArr.size() == 0) throw IllegalStateException("WeatherAPI trả về dự báo rỗng")

        // 1. Current Weather
        val temp = currentObj.get("temp_c")?.asDouble
            ?: throw IllegalStateException("WeatherAPI thiếu nhiệt độ hiện tại")
        val feelsLike = currentObj.get("feelslike_c")?.asDouble ?: temp
        val condObj = currentObj.getAsJsonObject("condition")
        val condCode = condObj?.get("code")?.asInt ?: 1000
        val wmoCode = mapWeatherApiCodeToWmo(condCode)
        val humidity = currentObj.get("humidity")?.asInt ?: 65
        val windKph = currentObj.get("wind_kph")?.asDouble ?: 10.0
        val windDegree = currentObj.get("wind_degree")?.asInt ?: 0
        val uv = currentObj.get("uv")?.asDouble ?: 5.0
        val pressureMb = currentObj.get("pressure_mb")?.asDouble ?: 1013.0
        val visKm = currentObj.get("vis_km")?.asDouble ?: 10.0
        val precipMm = currentObj.get("precip_mm")?.asDouble ?: 0.0
        val isDay = (currentObj.get("is_day")?.asInt ?: 1) == 1
        val timeNow = nowLdt.format(DateTimeFormatter.ofPattern("HH:mm"))

        val currentWeather = CurrentWeather(
            temperature = temp,
            feelsLike = feelsLike,
            weatherCode = wmoCode,
            humidity = humidity,
            windSpeed = windKph,
            windDirection = windDegree,
            uvIndex = uv,
            surfacePressure = pressureMb,
            visibilityKm = visKm,
            precipitation = precipMm,
            isDay = isDay,
            time = timeNow,
            sourceCondition = sourceCondition(condCode, condObj?.get("text")?.asString)
        )

        // 2. Air Quality (AQI)
        var airQuality: AirQuality? = null
        val aqiObj = currentObj.getAsJsonObject("air_quality")
        if (aqiObj != null) {
            val pm25 = aqiObj.get("pm2_5")?.asDouble
            val pm10 = aqiObj.get("pm10")?.asDouble
            val epaIndex = aqiObj.get("us-epa-index")?.asInt
            if (pm25 != null && pm10 != null && epaIndex != null && epaIndex in 1..6) {
                airQuality = AirQuality(pm25, pm10, epaIndex, "US EPA (1–6)")
            }
        }

        // 3. Hourly Forecasts (từ 3 ngày của WeatherAPI)
        val allHourlyList = mutableListOf<HourlyForecast>()
        val today = nowLdt.toLocalDate()

        if (forecastDaysArr != null) {
            for (dayElem in forecastDaysArr) {
                val dayObj = dayElem.asJsonObject
                val hoursArr = dayObj.getAsJsonArray("hour") ?: continue
                for (hElem in hoursArr) {
                    val hObj = hElem.asJsonObject
                    val timeStr = hObj.get("time")?.asString ?: "" // Format: "2026-10-13 14:00"
                    val hourLdt = try {
                        LocalDateTime.parse(timeStr.replace(" ", "T"))
                    } catch (e: Exception) {
                        continue
                    }

                    val hourLabel = hourLdt.format(DateTimeFormatter.ofPattern("HH:00"))
                    val hTemp = hObj.get("temp_c")?.asDouble ?: continue
                    val hCode = hObj.getAsJsonObject("condition")?.get("code")?.asInt ?: continue
                    val hWmo = mapWeatherApiCodeToWmo(hCode)
                    val hRainProb = hObj.get("chance_of_rain")?.takeUnless { it.isJsonNull }?.asInt
                        ?.takeIf { it in 0..100 } ?: -1
                    val hHumid = hObj.get("humidity")?.asInt ?: 60
                    val hWind = hObj.get("wind_kph")?.asDouble ?: 10.0
                    val hIsDay = (hObj.get("is_day")?.asInt ?: 1) == 1

                    allHourlyList.add(
                        HourlyForecast(
                            timeString = hourLdt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            hourLabel = hourLabel,
                            temperature = hTemp,
                            weatherCode = hWmo,
                            precipitationProbability = hRainProb,
                            humidity = hHumid,
                            windSpeed = hWind,
                            isDay = hIsDay,
                            precipitationMm = hObj.get("precip_mm")?.takeUnless { it.isJsonNull }?.asDouble,
                            sourceCondition = sourceCondition(hCode, hObj.getAsJsonObject("condition")?.get("text")?.asString)
                        )
                    )
                }
            }
        }

        // Lấy 24 giờ tới tính từ giờ hiện tại
        val futureHours = allHourlyList.filter {
            try {
                val ldt = LocalDateTime.parse(it.timeString)
                !ldt.isBefore(nowLdt.truncatedTo(ChronoUnit.HOURS))
            } catch (e: Exception) {
                false
            }
        }
        val first24h = futureHours.take(24)
        if (first24h.isEmpty()) throw IllegalStateException("WeatherAPI thiếu dự báo theo giờ")

        // 4. Daily 7 Days Forecast
        val dailyList = mutableListOf<DailyForecast>()

        // 3 ngày đầu từ WeatherAPI
        if (forecastDaysArr != null) {
            for (dayElem in forecastDaysArr) {
                val dayObj = dayElem.asJsonObject
                val dateStr = dayObj.get("date")?.asString ?: continue
                val dateObj = try {
                    LocalDate.parse(dateStr)
                } catch (e: Exception) {
                    continue
                }

                val dayInfo = dayObj.getAsJsonObject("day") ?: continue
                val astro = dayObj.getAsJsonObject("astro")

                val maxT = dayInfo.get("maxtemp_c")?.asDouble ?: continue
                val minT = dayInfo.get("mintemp_c")?.asDouble ?: continue
                val dCode = dayInfo.getAsJsonObject("condition")?.get("code")?.asInt ?: continue
                val dWmo = mapWeatherApiCodeToWmo(dCode)
                val dProb = dayInfo.get("daily_chance_of_rain")?.takeUnless { it.isJsonNull }?.asInt
                    ?.takeIf { it in 0..100 } ?: -1
                val dUv = dayInfo?.get("uv")?.asDouble ?: 5.0

                val sunrise = astro?.get("sunrise")?.asString?.let { formatAstroTime(it) } ?: "05:45"
                val sunset = astro?.get("sunset")?.asString?.let { formatAstroTime(it) } ?: "17:55"

                val dayOfWeek = if (dateObj.isEqual(today)) "Hôm nay" else getVietnameseDayOfWeek(dateObj)
                val dayHourly = allHourlyList.filter { it.timeString.startsWith(dateStr) }

                dailyList.add(
                    DailyForecast(
                        dateString = dateStr,
                        dayOfWeek = dayOfWeek,
                        maxTemp = maxT,
                        minTemp = minT,
                        weatherCode = dWmo,
                        precipitationProbability = dProb,
                        sunrise = sunrise,
                        sunset = sunset,
                        uvIndexMax = dUv,
                        hourly24h = dayHourly,
                        sourceName = "WeatherAPI.com"
                    )
                )
            }
        }

        // Bổ sung ngày 4-7 từ Open-Meteo nếu có để đảm bảo đủ 7 ngày hoàn chỉnh
        if (openMeteoSupplement != null && openMeteoSupplement.daily7Days.isNotEmpty()) {
            val existingDates = dailyList.map { it.dateString }.toSet()
            val remainingDays = openMeteoSupplement.daily7Days.filter {
                it.dateString !in existingDates && it.dateString >= today.toString()
            }
            dailyList.addAll(remainingDays)
        }
        if (dailyList.isEmpty()) throw IllegalStateException("Không có ngày dự báo hợp lệ")

        val observedAt = currentObj.get("last_updated")?.asString?.let {
            runCatching { LocalDateTime.parse(it.replace(" ", "T")) }.getOrNull()
        } ?: nowLdt
        val lastUpdated = observedAt.format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy"))

        return WeatherData(
            location = location,
            current = currentWeather,
            hourly24h = first24h,
            daily7Days = dailyList.distinctBy { it.dateString }.sortedBy { it.dateString }.take(7),
            airQuality = airQuality,
            lastUpdated = lastUpdated,
            forecastModel = WeatherForecastModel.BEST_MATCH,
            isOffline = false,
            providerName = "WeatherAPI.com"
        )
    }

    private fun sourceCondition(code: Int, text: String?): WeatherCondition {
        val nearbyDescription = when (code) {
            1063 -> "Có thể mưa rải rác quanh khu vực"
            1066 -> "Có thể có tuyết quanh khu vực"
            1069 -> "Có thể có mưa tuyết quanh khu vực"
            1072 -> "Có thể mưa phùn lạnh quanh khu vực"
            1087 -> "Có thể có dông quanh khu vực"
            else -> null
        }
        return WeatherCondition.fromSource(mapWeatherApiCodeToWmo(code), nearbyDescription ?: text, nearbyDescription != null)
    }

    private fun mapWeatherApiCodeToWmo(code: Int): Int {
        return when (code) {
            1000 -> 0 // Sunny / Clear
            1003 -> 2 // Partly cloudy
            1006, 1009 -> 3 // Cloudy / Overcast
            1030, 1135, 1147 -> 45 // Mist, Fog
            1063, 1180, 1183 -> 61 // Light rain
            1186, 1189 -> 63 // Moderate rain
            1192, 1195 -> 65 // Heavy rain
            1240 -> 80 // Light rain shower
            1243 -> 81 // Moderate/heavy shower
            1246 -> 82 // Torrential shower
            1087, 1273, 1276 -> 95 // Thunderstorm
            1279, 1282 -> 99 // Thunderstorm with hail/snow
            1150, 1153 -> 51 // Drizzle
            1168, 1171, 1072 -> 56 // Freezing drizzle
            1066, 1210, 1213 -> 71 // Light snow
            1216, 1219 -> 73 // Moderate snow
            1114, 1117, 1222, 1225 -> 75 // Heavy snow / Blizzard
            1069, 1204, 1207, 1249, 1252 -> 68 // Sleet
            else -> 2
        }
    }

    private fun formatAstroTime(astroStr: String): String {
        // Chuyển "05:45 AM" -> "05:45" hoặc "06:12 PM" -> "18:12"
        return try {
            val parts = astroStr.trim().split(" ")
            val timeParts = parts[0].split(":")
            var hour = timeParts[0].toInt()
            val min = timeParts[1].toInt()
            if (parts.size > 1 && parts[1].equals("PM", ignoreCase = true) && hour < 12) {
                hour += 12
            } else if (parts.size > 1 && parts[1].equals("AM", ignoreCase = true) && hour == 12) {
                hour = 0
            }
            String.format(Locale.US, "%02d:%02d", hour, min)
        } catch (e: Exception) {
            astroStr
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

    suspend fun testApiKey(apiKey: String): Result<String> = withContext(Dispatchers.IO) {
        val trimmed = apiKey.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Vui lòng nhập API Key"))
        }
        try {
            val testUrl = "https://api.weatherapi.com/v1/forecast.json?" +
                "key=$trimmed&q=21.0285,105.8542&days=3&aqi=yes&lang=vi"
            val req = Request.Builder().url(testUrl).build()
            client.newCall(req).execute().use { resp ->
                val body = resp.body?.string().orEmpty()
                if (resp.isSuccessful) {
                    val json = gson.fromJson(body, JsonObject::class.java)
                    val days = json.getAsJsonObject("forecast")?.getAsJsonArray("forecastday")
                    if (json.getAsJsonObject("current") == null || days == null || days.size() < 3) {
                        Result.failure(Exception("WeatherAPI chưa trả đủ 3 ngày dự báo cho cấu hình hiện tại"))
                    } else {
                        Result.success("Key dùng được với forecast.json; đã nhận ${days.size()} ngày dự báo.")
                    }
                } else {
                    val errorCode = runCatching {
                        gson.fromJson(body, JsonObject::class.java)
                            .getAsJsonObject("error")?.get("code")?.asInt
                    }.getOrNull()
                    val msg = when (errorCode) {
                        1002, 2006, 2008 -> "API key thiếu, không hợp lệ hoặc đã bị vô hiệu hóa."
                        2007 -> "API key đã vượt hạn mức gọi trong tháng."
                        2009 -> "Gói API hiện tại không có quyền dùng tài nguyên này."
                        1006 -> "Không tìm thấy địa điểm thử nghiệm."
                        else -> "WeatherAPI trả lỗi HTTP ${resp.code}${errorCode?.let { " (mã $it)" } ?: ""}."
                    }
                    Result.failure(Exception(msg))
                }
            }
        } catch (e: Exception) {
            Result.failure(Exception("Không thể kết nối tới máy chủ WeatherAPI: ${e.localizedMessage ?: e.message}"))
        }
    }
}
