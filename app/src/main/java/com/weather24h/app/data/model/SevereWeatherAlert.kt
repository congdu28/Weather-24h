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
        val thunderHour = hourly.take(8).firstOrNull { it.weatherCode in listOf(95, 96, 99) }
        val isCurrentThunder = current.weatherCode in listOf(95, 96, 99)
        if (isCurrentThunder || thunderHour != null) {
            val timeText = if (isCurrentThunder) "Đang diễn ra" else "Dự báo khoảng ${thunderHour?.hourLabel ?: "sắp tới"}"
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
        val heavyRainHour = hourly.take(8).firstOrNull {
            it.precipitationProbability >= 70 || it.weatherCode in listOf(65, 82)
        }
        val isCurrentHeavyRain = current.precipitation >= 5.0 || current.weatherCode in listOf(65, 82)
        if ((isCurrentHeavyRain || heavyRainHour != null) && alerts.none { it.id == "alert_thunder" }) {
            val timeText = if (isCurrentHeavyRain) "Đang có mưa lớn" else "Dự báo mưa to lúc ${heavyRainHour?.hourLabel ?: "sắp tới"} (xác suất ${heavyRainHour?.precipitationProbability ?: 75}%)"
            alerts.add(
                SevereWeatherAlert(
                    id = "alert_heavy_rain",
                    title = "CẢNH BÁO MƯA LỚN DIỆN RỘNG",
                    headline = "Mưa to kéo dài có thể gây trơn trượt và ngập úng các tuyến đường trũng thấp.",
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

        // 3. CẢNH BÁO TIA CỰC TÍM (UV) NGUY HẠI (UV >= 8.0)
        val maxUv = maxOf(current.uvIndex, daily?.uvIndexMax ?: 0.0)
        if (maxUv >= 8.0) {
            val isExtreme = maxUv >= 11.0
            alerts.add(
                SevereWeatherAlert(
                    id = "alert_uv",
                    title = if (isExtreme) "BỨC XẠ UV CỰC ĐỘ (MỨC TÍM)" else "CẢNH BÁO TIA UV NGUY HẠI",
                    headline = "Chỉ số UV đạt đỉnh ${kotlin.math.round(maxUv * 10) / 10.0}, bức xạ mặt trời có thể gây cháy nắng và tổn thương mắt nhanh chóng.",
                    severity = if (isExtreme) AlertSeverity.DANGER else AlertSeverity.WARNING,
                    iconEmoji = "☀️",
                    timeInfo = "Đỉnh điểm từ 10:30 đến 14:30",
                    safetyTips = listOf(
                        "Bôi kem chống nắng phổ rộng SPF 50+ trước khi ra ngoài 20 phút.",
                        "Mặc áo chống nắng sợi dày, đội mũ rộng vành và đeo kính râm chống tia UV.",
                        "Tìm bóng râm trú mát khi ở ngoài trời vào buổi trưa."
                    )
                )
            )
        }

        // 4. CẢNH BÁO NẮNG NÓNG GAY GẮT / SỐC NHIỆT (Temp >= 37°C hoặc FeelsLike >= 40°C)
        val maxTemp = weather.maxTemp24h
        if (maxTemp >= 37.0 || current.feelsLike >= 40.0) {
            alerts.add(
                SevereWeatherAlert(
                    id = "alert_heat",
                    title = "CẢNH BÁO NẮNG NÓNG GAY GẮT",
                    headline = "Nhiệt độ cảm giác thật lên đến ${current.feelsLike.toInt()}°C. Nguy cơ say nắng, kiệt sức vì nhiệt cao.",
                    severity = AlertSeverity.WARNING,
                    iconEmoji = "🌡️",
                    timeInfo = "Thời điểm ban ngày",
                    safetyTips = listOf(
                        "Uống nhiều nước từng ngụm nhỏ, bổ sung oresol hoặc nước dừa / điện giải.",
                        "Hạn chế hoạt động thể lực nặng ngoài trời vào khung giờ nắng gắt.",
                        "Tránh thay đổi nhiệt độ đột ngột từ phòng điều hòa lạnh ra ngoài trời nắng."
                    )
                )
            )
        }

        // 6. CẢNH BÁO RÉT ĐẬM / RÉT HẠI (Min temp <= 13°C)
        val minTemp = weather.minTemp24h
        if (minTemp <= 13.0 && minTemp > 0.0) {
            alerts.add(
                SevereWeatherAlert(
                    id = "alert_cold",
                    title = "CẢNH BÁO RÉT ĐẬM & GIÓ LẠNH",
                    headline = "Nhiệt độ thấp nhất hạ xuống ${minTemp.toInt()}°C. Trời rét buốt về đêm và sáng sớm.",
                    severity = AlertSeverity.INFO,
                    iconEmoji = "🧣",
                    timeInfo = "Đêm và sáng sớm",
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
