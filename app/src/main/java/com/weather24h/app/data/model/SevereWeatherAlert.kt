package com.weather24h.app.data.model

enum class AlertSeverity {
    INFO,
    WARNING,
    DANGER
}

data class SevereWeatherAlert(
    val id: String,
    val title: String,
    val headline: String,
    val severity: AlertSeverity,
    val iconEmoji: String,
    val timeInfo: String,
    val safetyTips: List<String>
)

object SevereWeatherDetector {
    fun detectAlerts(weather: WeatherData): List<SevereWeatherAlert> {
        val alerts = mutableListOf<SevereWeatherAlert>()
        val current = weather.current
        val hourly = weather.hourly24h
        val daily = weather.daily7Days.firstOrNull()

        // 1. CẢNH BÁO DÔNG SÉT / MƯA DÔNG NGUY HIỂM (WMO 95, 96, 99)
        // Tránh báo động giả khi lượng mưa dự báo = 0.0mm và xác suất mưa thấp
        val thunderHour = hourly.take(8).firstOrNull {
            it.weatherCode in listOf(95, 96, 99) &&
                (it.precipitationMm == null || it.precipitationMm >= 0.3 || it.precipitationProbability >= 45)
        }
        val isCurrentThunder = current.condition.type == WeatherType.THUNDERSTORM && current.precipitation > 0.05
        if (isCurrentThunder || thunderHour != null) {
            val timeText = if (isCurrentThunder) "Đang diễn ra" else "Khoảng ${thunderHour?.hourLabel ?: "sắp tới"}"
            alerts.add(
                SevereWeatherAlert(
                    id = "alert_thunder",
                    title = "CẢNH BÁO DÔNG SÉT & GIÓ GIẬT",
                    headline = "Khu vực có khả năng xuất hiện mưa dông, sấm sét mạnh và gió giật nguy hiểm.",
                    severity = AlertSeverity.DANGER,
                    iconEmoji = "⚡",
                    timeInfo = timeText,
                    safetyTips = listOf(
                        "Hạn chế tối đa việc di chuyển ngoài trời khi có sấm sét.",
                        "Tránh xa các gốc cây to, cột điện, hàng rào kim loại.",
                        "Ngắt các thiết bị điện tử nhạy cảm trong nhà đề phòng sét đánh lan truyền."
                    )
                )
            )
        }

        // 2. CẢNH BÁO MƯA TO / MƯA XỐI XẢ
        // Chỉ cảnh báo mưa lớn khi lượng mưa thực sự lớn (>= 5.0mm) hoặc có mã mưa to kèm lượng mưa >= 2.5mm (hoặc nguồn không cung cấp mm)
        val heavyRainHour = hourly.take(8).firstOrNull {
            (it.precipitationMm ?: 0.0) >= 5.0 ||
                (it.weatherCode in listOf(65, 82) && (it.precipitationMm == null || it.precipitationMm >= 2.5))
        }
        val isCurrentHeavyRain = current.precipitation >= 5.0 ||
            (current.weatherCode in listOf(65, 82) && current.precipitation >= 2.5)
        if ((isCurrentHeavyRain || heavyRainHour != null) && alerts.none { it.id == "alert_thunder" }) {
            val timeText = if (isCurrentHeavyRain) "Theo dữ liệu hiện tại" else "Dự báo khoảng ${heavyRainHour?.hourLabel ?: "sắp tới"}"
            alerts.add(
                SevereWeatherAlert(
                    id = "alert_heavy_rain",
                    title = "DỰ BÁO MƯA LỚN TẠI KHU VỰC",
                    headline = "Nguồn dự báo cho thấy lượng mưa lớn tại tọa độ đã chọn. Chú ý đường trơn và những điểm trũng thấp.",
                    severity = AlertSeverity.WARNING,
                    iconEmoji = "🌧️",
                    timeInfo = timeText,
                    safetyTips = listOf(
                        "Luôn mang theo áo mưa hoặc ô gấp gọn khi ra ngoài.",
                        "Giảm tốc độ, giữ khoảng cách an toàn khi lái xe trong trời mưa.",
                        "Tránh đi vào những đoạn đường có vũng nước ngập sâu hoặc nắp cống."
                    )
                )
            )
        }

        // Xác suất cao cho biết khả năng xảy ra mưa (chỉ báo khi lượng mưa dự báo thực tế >= 0.2mm hoặc không có dữ liệu mm)
        val likelyRainHour = hourly.take(8).firstOrNull {
            it.precipitationProbability >= 70 && (it.precipitationMm == null || it.precipitationMm >= 0.2)
        }
        if (likelyRainHour != null && alerts.none { it.id in listOf("alert_thunder", "alert_heavy_rain") }) {
            alerts.add(
                SevereWeatherAlert(
                    id = "alert_rain_chance",
                    title = "KHẢ NĂNG CÓ MƯA",
                    headline = "Xác suất mưa ${likelyRainHour.precipitationProbability}% trong khung giờ dự báo. Chỉ số này không có nghĩa là đang mưa hoặc sẽ mưa lớn.",
                    severity = AlertSeverity.INFO,
                    iconEmoji = "☂️",
                    timeInfo = "Khoảng ${likelyRainHour.hourLabel}",
                    safetyTips = listOf("Mang theo ô hoặc áo mưa nếu ra ngoài.", "Kiểm tra lại dự báo gần thời điểm di chuyển.")
                )
            )
        }

        // 3. CẢNH BÁO TIA CỰC TÍM (UV) NGUY HẠI (Chỉ cảnh báo ban ngày khi chỉ số UV cao hoặc sắp tới giờ đỉnh điểm)
        if (current.isDay) {
            val currentHour = try {
                val timeStr = current.time
                if (timeStr.contains(":")) timeStr.substringBefore(":").toIntOrNull() ?: 12 else 12
            } catch (_: Exception) { 12 }

            val isUvTimeWindow = currentHour in 9..15
            val maxUv = if (isUvTimeWindow) maxOf(current.uvIndex, daily?.uvIndexMax ?: 0.0) else current.uvIndex

            if (maxUv >= 8.0) {
                val isExtreme = maxUv >= 11.0
                alerts.add(
                    SevereWeatherAlert(
                        id = "alert_uv",
                        title = if (isExtreme) "BỨC XẠ UV CỰC ĐỘ (MỨC TÍM)" else "CẢNH BÁO TIA UV NGUY HẠI",
                        headline = "Chỉ số UV đạt đỉnh ${kotlin.math.round(maxUv * 10) / 10.0}, bức xạ mặt trời có thể gây cháy nắng và tổn thương mắt nhanh chóng.",
                        severity = if (isExtreme) AlertSeverity.DANGER else AlertSeverity.WARNING,
                        iconEmoji = "☀️",
                        timeInfo = "10:30 - 14:30",
                        safetyTips = listOf(
                            "Bôi kem chống nắng phổ rộng SPF 50+ trước khi ra ngoài 20 phút.",
                            "Mặc áo chống nắng sợi dày, đội mũ rộng vành và đeo kính râm chống tia UV.",
                            "Tìm bóng râm trú mát khi ở ngoài trời vào buổi trưa."
                        )
                    )
                )
            }
        }

        // 4. CẢNH BÁO NẮNG NÓNG GAY GẮT / SỐC NHIỆT (Chỉ cảnh báo khi nhiệt độ thực sự gay gắt ban ngày)
        val maxTemp = weather.maxTemp24h
        val isSevereHeat = current.feelsLike >= 40.0 || (current.isDay && (current.feelsLike >= 37.0 || (maxTemp >= 37.0 && current.feelsLike >= 33.0)))
        if (isSevereHeat) {
            val headlineText = if (current.feelsLike >= 37.0) {
                "Nhiệt độ cảm giác thật lên đến ${current.feelsLike.toInt()}°C. Nguy cơ say nắng, kiệt sức vì nhiệt cao."
            } else {
                "Dự báo nhiệt độ cao nhất đạt ${maxTemp.toInt()}°C. Cẩn trọng say nắng và mất nước vào buổi trưa và chiều."
            }
            alerts.add(
                SevereWeatherAlert(
                    id = "alert_heat",
                    title = "CẢNH BÁO NẮNG NÓNG GAY GẮT",
                    headline = headlineText,
                    severity = AlertSeverity.WARNING,
                    iconEmoji = "🌡️",
                    timeInfo = "Trưa & chiều",
                    safetyTips = listOf(
                        "Uống nhiều nước từng ngụm nhỏ, bổ sung oresol hoặc nước dừa / điện giải.",
                        "Hạn chế hoạt động thể lực nặng ngoài trời vào khung giờ nắng gắt.",
                        "Tránh thay đổi nhiệt độ đột ngột từ phòng điều hòa lạnh ra ngoài trời nắng."
                    )
                )
            )
        }

        // 5. CẢNH BÁO RÉT ĐẬM / RÉT HẠI (Min temp <= 13°C)
        val minTemp = weather.minTemp24h
        if (minTemp <= 13.0 && minTemp > 0.0) {
            alerts.add(
                SevereWeatherAlert(
                    id = "alert_cold",
                    title = "CẢNH BÁO RÉT ĐẬM & GIÓ LẠNH",
                    headline = "Nhiệt độ thấp nhất hạ xuống ${minTemp.toInt()}°C. Trời rét buốt về đêm và sáng sớm.",
                    severity = AlertSeverity.INFO,
                    iconEmoji = "🧣",
                    timeInfo = "Đêm & sáng sớm",
                    safetyTips = listOf(
                        "Mặc ấm nhiều lớp, đặc biệt giữ ấm phần cổ, ngực, bàn tay và bàn chân.",
                        "Tránh tắm muộn bằng nước lạnh, cẩn thận nguy cơ đột quỵ do lạnh.",
                        "Ăn uống thức ăn ấm nóng để tăng nhiệt lượng cho cơ thể."
                    )
                )
            )
        }

        return alerts
    }
}
