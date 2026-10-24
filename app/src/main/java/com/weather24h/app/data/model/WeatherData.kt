package com.weather24h.app.data.model

data class CurrentWeather(
    val temperature: Double,
    val feelsLike: Double,
    val weatherCode: Int,
    val humidity: Int,
    val windSpeed: Double,
    val windDirection: Int,
    val uvIndex: Double,
    val surfacePressure: Double,
    val visibilityKm: Double,
    val precipitation: Double,
    val isDay: Boolean,
    val time: String,
    val sourceCondition: WeatherCondition? = null
) {
    val condition: WeatherCondition
        get() {
            val base = sourceCondition ?: WeatherCondition.fromWmoCode(weatherCode)

            // 1. Khi lượng mưa thực tế = 0.0mm (<= 0.05mm):
            // Tuyệt đối không bao giờ báo "Mưa rào vừa đến to", "Mưa nhẹ", "Mưa phùn" hay "Dông bão đang diễn ra"
            if (precipitation <= 0.05) {
                if (base.type == WeatherType.THUNDERSTORM) {
                    return if (isDay && (temperature >= 30.0 || feelsLike >= 34.0)) {
                        WeatherCondition("Trời nắng, mây đối lưu", WeatherType.PARTLY_CLOUDY)
                    } else {
                        WeatherCondition(
                            if (isDay) "Nhiều mây, có thể có dông quanh vùng" else "Nhiều mây, âm u",
                            WeatherType.CLOUDY
                        )
                    }
                }
                if (base.type == WeatherType.DRIZZLE || base.type == WeatherType.RAIN) {
                    return when {
                        isDay && (temperature >= 32.0 || feelsLike >= 36.0) ->
                            WeatherCondition("Trời nắng, mây thay đổi", WeatherType.PARTLY_CLOUDY)
                        isDay && (temperature >= 27.0 || feelsLike >= 30.0) ->
                            WeatherCondition("Nắng xen mây", WeatherType.PARTLY_CLOUDY)
                        temperature >= 23.0 ->
                            WeatherCondition(
                                if (isDay) "Nhiều mây, có lúc hửng nắng" else "Trời nhiều mây",
                                WeatherType.PARTLY_CLOUDY
                            )
                        else ->
                            WeatherCondition("Nhiều mây, âm u", WeatherType.CLOUDY)
                    }
                }
            }

            // 2. Khi lượng mưa chỉ ở mức vi lượng (0.06 - 0.25mm):
            if (precipitation <= 0.25) {
                if ((base.type == WeatherType.DRIZZLE || base.type == WeatherType.RAIN || base.type == WeatherType.THUNDERSTORM) &&
                    isDay && (temperature >= 28.0 || feelsLike >= 33.0)
                ) {
                    return if (temperature >= 32.0 || feelsLike >= 36.0) {
                        WeatherCondition("Trời nắng, mây thay đổi", WeatherType.PARTLY_CLOUDY)
                    } else {
                        WeatherCondition("Nắng xen mây", WeatherType.PARTLY_CLOUDY)
                    }
                }
                if (base.type == WeatherType.RAIN || base.type == WeatherType.THUNDERSTORM) {
                    return WeatherCondition(
                        if (temperature >= 24.0) "Mưa nhỏ rải rác" else "Mưa phùn nhẹ",
                        WeatherType.DRIZZLE
                    )
                }
            }

            // 3. Khi lượng mưa thực tế nhỏ (< 1.5mm) nhưng mã WMO lại báo mưa rào vừa đến to (63, 65, 81, 82):
            if (precipitation < 1.5 && weatherCode in listOf(63, 65, 81, 82)) {
                return if (precipitation < 0.6) {
                    WeatherCondition("Mưa nhỏ rải rác", WeatherType.DRIZZLE)
                } else {
                    WeatherCondition("Mưa nhẹ", WeatherType.RAIN)
                }
            }

            // 4. Trời ấm/nóng (>= 25°C) có mưa nhỏ thì không gọi là "Mưa phùn nhẹ"
            if (base.type == WeatherType.DRIZZLE && temperature >= 25.0) {
                return WeatherCondition("Mưa nhỏ rải rác", WeatherType.DRIZZLE)
            }

            // 5. Trời quang đãng nhưng nhiệt độ ban ngày rất nóng
            if (base.type == WeatherType.CLEAR && isDay && (temperature >= 34.0 || feelsLike >= 38.0)) {
                return WeatherCondition("Trời nắng nóng", WeatherType.CLEAR)
            }

            return base
        }

    /**
     * Lượng mưa hiển thị đồng nhất với trạng thái thời tiết chính:
     * Loại bỏ sai số vi lượng (<= 0.25mm) khi trời đang nắng/có mây để không gây mâu thuẫn trên giao diện.
     */
    val effectivePrecipitationMm: Double
        get() {
            if (precipitation <= 0.05) return 0.0
            val condType = condition.type
            if (precipitation <= 0.25 &&
                condType != WeatherType.DRIZZLE &&
                condType != WeatherType.RAIN &&
                condType != WeatherType.THUNDERSTORM
            ) {
                return 0.0
            }
            return kotlin.math.round(precipitation * 10.0) / 10.0
        }

    val windDirectionText: String
        get() {
            val directions = listOf("Bắc", "Đông Bắc", "Đông", "Đông Nam", "Nam", "Tây Nam", "Tây", "Tây Bắc")
            val index = (((windDirection + 22.5) % 360) / 45).toInt()
            return directions[index % 8]
        }
}

data class HourlyForecast(
    val timeString: String,
    val hourLabel: String,
    val temperature: Double,
    val weatherCode: Int,
    val precipitationProbability: Int,
    val humidity: Int,
    val windSpeed: Double,
    val isDay: Boolean = true,
    val precipitationMm: Double? = null,
    val sourceCondition: WeatherCondition? = null
) {
    val condition: WeatherCondition
        get() {
            val base = sourceCondition ?: WeatherCondition.fromWmoCode(weatherCode)
            val precip = precipitationMm ?: 0.0
            val isHeavyRainCode = weatherCode in listOf(63, 65, 81, 82, 95, 96, 99)

            // 0. Nếu có dữ liệu lượng mưa = 0.0mm (<= 0.05mm), kể cả khi mã WMO báo mưa to (63, 65, 81, 82):
            // Không bao giờ hiển thị "Mưa rào vừa đến to" khi lượng mưa dự báo là 0.0mm
            if (precipitationMm != null && precip <= 0.05 &&
                (base.type == WeatherType.DRIZZLE || base.type == WeatherType.RAIN)
            ) {
                return when {
                    temperature >= 28.0 -> {
                        if (precipitationProbability in 30..100) {
                            WeatherCondition(
                                if (isDay) "Nắng xen mây, có thể có mưa nhẹ" else "Có mây, khả năng mưa thấp",
                                WeatherType.PARTLY_CLOUDY
                            )
                        } else {
                            WeatherCondition(
                                if (isDay) "Trời nắng, có mây" else "Trời quang, có mây",
                                WeatherType.PARTLY_CLOUDY
                            )
                        }
                    }
                    temperature >= 23.0 -> {
                        WeatherCondition(
                            if (isDay) "Nhiều mây, có lúc hửng nắng" else "Có mây",
                            WeatherType.PARTLY_CLOUDY
                        )
                    }
                    else -> WeatherCondition("Nhiều mây, âm u", WeatherType.CLOUDY)
                }
            }

            // 1. Khi có lượng mưa thực tế rất nhỏ (<= 0.15mm) và không phải mã mưa to/dông bão:
            if (precipitationMm != null && precip <= 0.15 && !isHeavyRainCode &&
                (base.type == WeatherType.DRIZZLE || base.type == WeatherType.RAIN)
            ) {
                return when {
                    temperature >= 28.0 -> {
                        if (precipitationProbability in 25..100) {
                            WeatherCondition(
                                if (isDay) "Nắng xen mây, có thể có mưa nhẹ" else "Có mây, khả năng mưa thấp",
                                WeatherType.PARTLY_CLOUDY
                            )
                        } else {
                            WeatherCondition(
                                if (isDay) "Trời nắng, có mây" else "Trời quang, có mây",
                                WeatherType.PARTLY_CLOUDY
                            )
                        }
                    }
                    temperature >= 23.0 -> {
                        WeatherCondition(
                            if (isDay) "Nhiều mây, có lúc hửng nắng" else "Có mây",
                            WeatherType.PARTLY_CLOUDY
                        )
                    }
                    else -> {
                        WeatherCondition("Nhiều mây, âm u", WeatherType.CLOUDY)
                    }
                }
            }

            // 2. Nếu nhiệt độ cao (>= 28°C) hoặc xác suất mưa thấp (<= 35%), mà lượng mưa thực tế <= 0.25mm:
            if ((base.type == WeatherType.DRIZZLE || (base.type == WeatherType.RAIN && precip <= 0.25 && !isHeavyRainCode)) &&
                precip <= 0.25 &&
                (temperature >= 28.0 || precipitationProbability in 0..35)
            ) {
                return if (precipitationProbability >= 25) {
                    WeatherCondition(
                        if (isDay) "Nắng xen mây, có thể có mưa nhẹ" else "Có mây, khả năng mưa thấp",
                        WeatherType.PARTLY_CLOUDY
                    )
                } else {
                    WeatherCondition(
                        if (isDay) "Trời nắng, có mây" else "Trời quang, có mây",
                        WeatherType.PARTLY_CLOUDY
                    )
                }
            }

            // 3. Nếu mã WMO là mưa rào vừa đến to (63, 65, 81, 82) nhưng lượng mưa dự báo thực tế nhỏ (< 1.2mm):
            if (precipitationMm != null && precip < 1.2 && weatherCode in listOf(63, 65, 81, 82)) {
                return if (precip <= 0.35) {
                    WeatherCondition("Mưa nhỏ rải rác", WeatherType.DRIZZLE)
                } else {
                    WeatherCondition("Mưa nhẹ", WeatherType.RAIN)
                }
            }

            // 4. Khi trời ấm/nóng (>= 25°C) và thực sự có mưa nhỏ, tránh gọi là "Mưa phùn nhẹ"
            if (base.type == WeatherType.DRIZZLE && temperature >= 25.0) {
                return WeatherCondition("Mưa nhỏ rải rác", WeatherType.DRIZZLE)
            }

            return base
        }

    val rainProbabilityText: String
        get() = if (precipitationProbability in 0..100) "$precipitationProbability%" else "—"
}

data class DailyForecast(
    val dateString: String,
    val dayOfWeek: String,
    val maxTemp: Double,
    val minTemp: Double,
    val weatherCode: Int,
    val precipitationProbability: Int,
    val sunrise: String,
    val sunset: String,
    val uvIndexMax: Double,
    val hourly24h: List<HourlyForecast> = emptyList(),
    val sourceName: String? = null,
    val precipitationSum: Double? = null
) {
    val rainProbabilityText: String
        get() = if (precipitationProbability in 0..100) "$precipitationProbability%" else "—"

    /**
     * Tổng hợp thông minh trạng thái thời tiết trong ngày từ chuỗi 24 giờ, lượng mưa tích lũy,
     * nhiệt độ tối đa và chỉ số UV — khắc phục triệt để việc Open-Meteo lấy MAX(weather_code)
     * khiến ngày nắng nóng (31-35°C, UV cao) bị báo nhầm thành "Mưa phùn nhẹ" chỉ vì 1-2 giờ ẩm.
     */
    val condition: WeatherCondition
        get() {
            if (hourly24h.isNotEmpty()) {
                val dayHours = hourly24h.filter { it.isDay }.ifEmpty { hourly24h }
                val totalRainMm = precipitationSum ?: hourly24h.sumOf { it.precipitationMm ?: 0.0 }

                // Đếm số giờ thực sự có mưa (đã qua bộ lọc HourlyForecast.condition)
                val rainHours = hourly24h.count { h ->
                    val type = h.condition.type
                    (type == WeatherType.DRIZZLE || type == WeatherType.RAIN || type == WeatherType.THUNDERSTORM) &&
                        ((h.precipitationMm ?: 0.0) >= 0.25 || (h.precipitationMm == null && h.precipitationProbability >= 45))
                }
                val dayRainHours = dayHours.count { h ->
                    val type = h.condition.type
                    (type == WeatherType.DRIZZLE || type == WeatherType.RAIN || type == WeatherType.THUNDERSTORM) &&
                        ((h.precipitationMm ?: 0.0) >= 0.25 || (h.precipitationMm == null && h.precipitationProbability >= 45))
                }
                val thunderHours = hourly24h.count { h ->
                    h.condition.type == WeatherType.THUNDERSTORM &&
                        ((h.precipitationMm ?: 0.0) >= 0.4 || h.precipitationProbability >= 40)
                }

                val clearDayHours = dayHours.count { it.condition.type == WeatherType.CLEAR }
                val partlyCloudyDayHours = dayHours.count { it.condition.type == WeatherType.PARTLY_CLOUDY }
                val sunnyOrPartlyDayHours = clearDayHours + partlyCloudyDayHours

                // 1. Có dông bão thực sự trong ngày
                if (thunderHours >= 2 || (thunderHours >= 1 && totalRainMm >= 2.5)) {
                    return if (sunnyOrPartlyDayHours >= dayHours.size / 2 && dayRainHours <= 2) {
                        WeatherCondition("Ngày nắng, chiều tối có dông", WeatherType.THUNDERSTORM)
                    } else {
                        WeatherCondition("Mưa rào và dông", WeatherType.THUNDERSTORM)
                    }
                }

                // 2. Ngày khô ráo / nắng / có mây (lượng mưa cả ngày < 1.2mm và tối đa 2 giờ mưa nhỏ thoáng qua)
                if (totalRainMm < 1.2 && rainHours <= 2) {
                    return when {
                        maxTemp >= 35.0 && (uvIndexMax >= 6.5 || sunnyOrPartlyDayHours >= dayHours.size / 2) -> {
                            WeatherCondition("Nắng nóng gay gắt", WeatherType.CLEAR)
                        }
                        maxTemp >= 32.0 && (uvIndexMax >= 6.0 || sunnyOrPartlyDayHours >= dayHours.size / 2) -> {
                            if (clearDayHours >= dayHours.size / 2) {
                                WeatherCondition("Trời nắng nóng", WeatherType.CLEAR)
                            } else {
                                WeatherCondition("Trời nắng, mây thay đổi", WeatherType.PARTLY_CLOUDY)
                            }
                        }
                        maxTemp >= 28.0 && (uvIndexMax >= 5.0 || sunnyOrPartlyDayHours >= dayHours.size / 2) -> {
                            if (clearDayHours >= dayHours.size * 0.6) {
                                WeatherCondition("Trời nắng đẹp", WeatherType.CLEAR)
                            } else {
                                WeatherCondition("Nắng xen mây", WeatherType.PARTLY_CLOUDY)
                            }
                        }
                        clearDayHours >= dayHours.size / 2 -> {
                            WeatherCondition("Trời quang đãng", WeatherType.CLEAR)
                        }
                        sunnyOrPartlyDayHours >= dayHours.size / 2 -> {
                            WeatherCondition("Mây rải rác, hửng nắng", WeatherType.PARTLY_CLOUDY)
                        }
                        else -> {
                            WeatherCondition("Nhiều mây", WeatherType.CLOUDY)
                        }
                    }
                }

                // 3. Ban ngày chủ yếu nắng ráo nhưng có mưa rào ngắn vài giờ
                if (sunnyOrPartlyDayHours >= (dayHours.size * 0.55).toInt() && rainHours in 1..4 && totalRainMm < 8.0) {
                    return if (dayRainHours <= 1) {
                        WeatherCondition("Ngày nắng, tối có lúc mưa rào", WeatherType.PARTLY_CLOUDY)
                    } else {
                        WeatherCondition("Nắng gián đoạn, có lúc mưa rào", WeatherType.DRIZZLE)
                    }
                }

                // 4. Ngày có mưa nhiều giờ hoặc lượng mưa đáng kể
                val heavyRainHours = hourly24h.count {
                    (it.precipitationMm ?: 0.0) >= 4.0 ||
                        (it.weatherCode in listOf(63, 65, 81, 82) && (it.precipitationMm == null || it.precipitationMm >= 2.0))
                }
                if (totalRainMm >= 15.0 || heavyRainHours >= 2) {
                    return WeatherCondition("Mưa rào vừa đến to", WeatherType.RAIN)
                }
                if (maxTemp >= 25.0) {
                    return if (totalRainMm >= 4.0 || rainHours >= 5) {
                        WeatherCondition("Có mưa rào rải rác", WeatherType.RAIN)
                    } else {
                        WeatherCondition("Có lúc mưa nhỏ", WeatherType.DRIZZLE)
                    }
                }
                val raw = WeatherCondition.fromWmoCode(weatherCode)
                return if (raw.type in listOf(WeatherType.DRIZZLE, WeatherType.RAIN, WeatherType.THUNDERSTORM)) {
                    raw
                } else {
                    WeatherCondition("Có mưa nhỏ", WeatherType.DRIZZLE)
                }
            }

            // Fallback khi nguồn dữ liệu không có danh sách giờ cho ngày đó
            val rawCondition = WeatherCondition.fromWmoCode(weatherCode)
            val sumMm = precipitationSum
            val isLightOrDrizzle = rawCondition.type == WeatherType.DRIZZLE ||
                (rawCondition.type == WeatherType.RAIN && weatherCode !in listOf(63, 65, 81, 82))

            if (sumMm != null && sumMm <= 0.1 &&
                rawCondition.type in listOf(WeatherType.DRIZZLE, WeatherType.RAIN, WeatherType.THUNDERSTORM)
            ) {
                return if (maxTemp >= 30.0) {
                    WeatherCondition("Trời nắng, mây thay đổi", WeatherType.PARTLY_CLOUDY)
                } else {
                    WeatherCondition("Mây rải rác", WeatherType.PARTLY_CLOUDY)
                }
            }

            if (isLightOrDrizzle) {
                val isDryBySum = sumMm != null && sumMm < 1.0
                val isLowProbWarm = precipitationProbability in 0..40 && maxTemp >= 28.0
                val isHotHighUv = maxTemp >= 30.0 && uvIndexMax >= 6.5 && (sumMm == null || sumMm < 2.5)
                if (isDryBySum || isLowProbWarm || isHotHighUv) {
                    return if (maxTemp >= 32.0 && uvIndexMax >= 6.5) {
                        WeatherCondition("Trời nắng, mây thay đổi", WeatherType.PARTLY_CLOUDY)
                    } else {
                        WeatherCondition("Nắng xen mây", WeatherType.PARTLY_CLOUDY)
                    }
                }
                if (rawCondition.type == WeatherType.DRIZZLE && maxTemp >= 25.0) {
                    return WeatherCondition("Có lúc mưa nhỏ", WeatherType.DRIZZLE)
                }
            }

            if (rawCondition.type == WeatherType.CLEAR) {
                if (precipitationProbability >= 60 && (sumMm == null || sumMm >= 2.0)) {
                    return WeatherCondition("Nắng gián đoạn, có lúc mưa rào", WeatherType.DRIZZLE)
                }
                if (precipitationProbability in 40..59 && (sumMm == null || sumMm >= 1.0)) {
                    return WeatherCondition("Nắng xen mây, có thể có mưa", WeatherType.PARTLY_CLOUDY)
                }
                if (maxTemp >= 33.0) {
                    return WeatherCondition("Trời nắng nóng", WeatherType.CLEAR)
                }
            }

            return rawCondition
        }
}

data class AirQuality(
    val pm25: Double,
    val pm10: Double,
    val aqiIndex: Int,
    val aqiScale: String? = null
) {
    val statusText: String
        get() = when {
            pm25 <= 12.0 -> "Tuyệt vời"
            pm25 <= 35.4 -> "Tốt"
            pm25 <= 55.4 -> "Trung bình"
            pm25 <= 150.4 -> "Kém"
            else -> "Nguy hại"
        }
}

data class WeatherData(
    val location: CityLocation,
    val current: CurrentWeather,
    val hourly24h: List<HourlyForecast>,
    val daily7Days: List<DailyForecast>,
    val airQuality: AirQuality?,
    val lastUpdated: String,
    val forecastModel: WeatherForecastModel = WeatherForecastModel.BEST_MATCH,
    val isOffline: Boolean = false,
    val providerName: String = "Không rõ nguồn"
) {
    val maxTemp24h: Double
        get() = hourly24h.maxOfOrNull { it.temperature } ?: current.temperature

    val minTemp24h: Double
        get() = hourly24h.minOfOrNull { it.temperature } ?: current.temperature
}
