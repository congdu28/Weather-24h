package com.weather24h.app.data.model

data class LifestyleItem(
    val id: String,
    val iconEmoji: String,
    val name: String,
    val status: String,
    val badgeColor: Long, // Color ARGB long e.g. 0xFF22C55E
    val shortTip: String,
    val detailedAdvice: String
)

object LifestyleCalculator {
    fun calculate(weather: WeatherData): List<LifestyleItem> {
        val current = weather.current
        val hourly = weather.hourly24h
        val daily = weather.daily7Days.firstOrNull()

        val rainProbNext12h = hourly.take(12).maxOfOrNull { it.precipitationProbability } ?: 0
        val humidity = current.humidity
        val temp = current.temperature
        val windSpeed = current.windSpeed
        val uv = maxOf(current.uvIndex, daily?.uvIndexMax ?: 0.0)
        val pm25 = weather.airQuality?.pm25 ?: 20.0
        val tempSwing = weather.maxTemp24h - weather.minTemp24h

        val items = mutableListOf<LifestyleItem>()

        // 1. PHƠI ĐỒ (Laundry Drying)
        val (laundryStatus, laundryColor, laundryTip, laundryDetail) = when {
            rainProbNext12h >= 50 -> Quadruple(
                "Không nên phơi",
                0xFFEF4444L, // Red
                "Khả năng có mưa $rainProbNext12h%",
                "Dự báo có mưa với xác suất lên tới $rainProbNext12h% trong 12 giờ tới. Quần áo dễ bị ẩm mốc nếu phơi ngoài trời."
            )
            humidity >= 85 -> Quadruple(
                "Lâu khô",
                0xFFF59E0BL, // Amber
                "Độ ẩm không khí cao ($humidity%)",
                "Độ ẩm không khí đạt $humidity%, thời gian khô quần áo kéo dài. Nên vắt kỹ và phơi nơi thông thoáng hoặc sấy khô."
            )
            humidity <= 60 && current.isDay -> Quadruple(
                "Rất nhanh khô",
                0xFF22C55EL, // Green
                "Nắng ráo & gió thoáng",
                "Thời tiết nắng ráo lý tưởng, độ ẩm $humidity% và gió ${windSpeed.toInt()} km/h. Đồ giặt sẽ khô thơm tho trong 2-3 giờ."
            )
            else -> Quadruple(
                "Khá nhanh",
                0xFF0EA5E9L, // Blue
                "Điều kiện phơi đồ tốt",
                "Không khí tương đối khô ráo, đồ giặt sẽ khô tự nhiên trong khoảng 4-5 giờ."
            )
        }
        items.add(
            LifestyleItem(
                id = "laundry",
                iconEmoji = "🧺",
                name = "Phơi quần áo",
                status = laundryStatus,
                badgeColor = laundryColor,
                shortTip = laundryTip,
                detailedAdvice = laundryDetail
            )
        )

        // 2. RỬA XE (Car Wash)
        val rainNext48h = weather.daily7Days.take(2).maxOfOrNull { it.precipitationProbability } ?: 0
        val (carStatus, carColor, carTip, carDetail) = when {
            rainNext48h >= 45 -> Quadruple(
                "Không nên rửa",
                0xFFEF4444L,
                "Sắp có mưa 1-2 ngày tới",
                "Dự báo có mưa trong vòng 48 giờ tới (xác suất $rainNext48h%). Xe sau khi rửa sẽ nhanh chóng bị dính nước mưa và bùn đất."
            )
            rainNext48h in 20..44 -> Quadruple(
                "Cân nhắc",
                0xFFF59E0BL,
                "Thời tiết tạm ổn",
                "Khả năng có mưa rải rác nhẹ. Nếu xe quá bụi bẩn bạn có thể rửa nhanh bên ngoài."
            )
            else -> Quadruple(
                "Rất thích hợp",
                0xFF22C55EL,
                "Trời tạnh ráo 2 ngày tới",
                "Thời tiết ổn định, khô ráo trong ít nhất 2 ngày tới. Đây là thời điểm tuyệt vời để rửa và chăm sóc xe sạch đẹp."
            )
        }
        items.add(
            LifestyleItem(
                id = "car_wash",
                iconEmoji = "🚗",
                name = "Rửa xe",
                status = carStatus,
                badgeColor = carColor,
                shortTip = carTip,
                detailedAdvice = carDetail
            )
        )

        // 3. THỂ THAO NGOÀI TRỜI (Outdoor Fitness)
        val (sportStatus, sportColor, sportTip, sportDetail) = when {
            rainProbNext12h >= 60 || current.precipitation > 0 -> Quadruple(
                "Tập trong nhà",
                0xFFEF4444L,
                "Cân nhắc mưa trong 12 giờ tới",
                "Nguồn dữ liệu báo có mưa hoặc xác suất mưa đạt $rainProbNext12h% ở một khung giờ trong 12 giờ tới. Kiểm tra dự báo đúng giờ tập; nếu đang mưa, ưu tiên tập trong nhà."
            )
            pm25 >= 55.4 -> Quadruple(
                "Hạn chế chạy",
                0xFFF59E0BL,
                "Chất lượng không khí kém",
                "Chỉ số bụi mịn PM2.5 cao (${pm25.toInt()} µg/m³). Hít thở sâu khi vận động mạnh ngoài trời có thể ảnh hưởng xấu đến phổi."
            )
            temp in 18.0..28.0 && current.feelsLike < 32.0 -> Quadruple(
                "Tuyệt vời",
                0xFF22C55EL,
                "Thời tiết mát mẻ lý tưởng",
                "Nhiệt độ ${temp.toInt()}°C vô cùng mát mẻ, dễ chịu. Rất thích hợp cho chạy bộ, đạp xe, bóng đá hoặc đi dạo công viên."
            )
            temp >= 35.0 || current.feelsLike >= 38.0 -> Quadruple(
                "Tránh nắng gắt",
                0xFFF59E0BL,
                "Nhiệt độ ngoài trời rất cao",
                "Nhiệt độ cảm giác ${current.feelsLike.toInt()}°C, nguy cơ mất nước và say nắng. Nên tập vào sáng sớm hoặc sau 17:30."
            )
            else -> Quadruple(
                "Khá tốt",
                0xFF0EA5E9L,
                "Thích hợp tập luyện",
                "Điều kiện thời tiết ổn định, phù hợp cho hầu hết các hoạt động thể thao thông thường."
            )
        }
        items.add(
            LifestyleItem(
                id = "sports",
                iconEmoji = "🏃",
                name = "Thể thao ngoài trời",
                status = sportStatus,
                badgeColor = sportColor,
                shortTip = sportTip,
                detailedAdvice = sportDetail
            )
        )

        // 4. NỒM ẨM & NẤM MỐC (Humidity & Moisture Risk)
        val (moistureStatus, moistureColor, moistureTip, moistureDetail) = when {
            humidity >= 85 && temp in 18.0..28.0 -> Quadruple(
                "Nồm ẩm cao",
                0xFFEF4444L,
                "Độ ẩm $humidity%, sàn dễ ướt",
                "Hiện tượng nồm ẩm đặc trưng đang diễn ra. Độ ẩm $humidity% kết hợp nhiệt độ sàn lạnh dễ gây đọng nước sàn nhà, mốc tường và vi khuẩn phát triển. Nên đóng kín cửa và bật chế độ Dry điều hòa."
            )
            humidity >= 75 -> Quadruple(
                "Ẩm ướt nhẹ",
                0xFFF59E0BL,
                "Độ ẩm không khí $humidity%",
                "Không khí ẩm, quần áo và chăn gối có thể hơi ẩm. Chú ý lau khô các khu vực thông gió kém."
            )
            humidity in 45..75 -> Quadruple(
                "Dễ chịu",
                0xFF22C55EL,
                "Độ ẩm lý tưởng ($humidity%)",
                "Độ ẩm ở mức cân bằng hoàn hảo cho da và đường hô hấp. Nhà cửa khô ráo, thoáng mát."
            )
            else -> Quadruple(
                "Khô hanh",
                0xFF0EA5E9L,
                "Độ ẩm thấp ($humidity%)",
                "Không khí khô hanh có thể gây khô da, nứt nẻ môi. Nên uống nhiều nước và dùng kem dưỡng ẩm."
            )
        }
        items.add(
            LifestyleItem(
                id = "moisture",
                iconEmoji = "💧",
                name = "Nguy cơ Nồm ẩm",
                status = moistureStatus,
                badgeColor = moistureColor,
                shortTip = moistureTip,
                detailedAdvice = moistureDetail
            )
        )

        // 5. BẢO VỆ DA & CHỐNG NẮNG (Skincare & UV)
        val roundedUv = kotlin.math.round(uv * 10) / 10.0
        val (uvStatus, uvColor, uvTip, uvDetail) = when {
            uv >= 10.0 -> Quadruple(
                "Mức tím cực cao",
                0xFFA855F7L, // Purple
                "Chỉ số UV đạt đỉnh $roundedUv",
                "Bức xạ tia cực tím ở mức tối đa nguy hiểm. Da có thể bị bỏng rát chỉ sau 15 phút tiếp xúc trực tiếp. Bắt buộc mặc áo chống nắng và bôi kem chống nắng SPF 50+."
            )
            uv >= 7.0 -> Quadruple(
                "Rất cao",
                0xFFEF4444L,
                "Chỉ số UV cao ($roundedUv)",
                "Tia cực tím mạnh gây lão hóa da và tổn thương mắt. Cần che chắn kỹ và đeo kính râm khi ra đường buổi trưa."
            )
            uv >= 3.0 -> Quadruple(
                "Trung bình",
                0xFFF59E0BL,
                "UV mức $roundedUv",
                "Bức xạ vừa phải. Nên bôi kem chống nắng nếu hoạt động ngoài trời lâu trên 30 phút."
            )
            else -> Quadruple(
                "An toàn",
                0xFF22C55EL,
                "UV thấp ($roundedUv)",
                "Bức xạ mặt trời yếu hoặc về đêm, an toàn cho làn da."
            )
        }
        items.add(
            LifestyleItem(
                id = "uv_care",
                iconEmoji = "🧴",
                name = "Chăm sóc Da & UV",
                status = uvStatus,
                badgeColor = uvColor,
                shortTip = uvTip,
                detailedAdvice = uvDetail
            )
        )

        // 6. CẢM LẠNH & SỐC NHIỆT (Cold / Heat Stroke Risk)
        val (fluStatus, fluColor, fluTip, fluDetail) = when {
            tempSwing >= 9.0 -> Quadruple(
                "Đáng chú ý",
                0xFFF59E0BL,
                "Chênh lệch ngày đêm ${tempSwing.toInt()}°C",
                "Nhiệt độ giữa trưa và đêm chênh lệch tới ${tempSwing.toInt()}°C. Cơ thể dễ bị viêm họng, cảm sốt nếu không thích ứng kịp. Hãy mang theo áo khoác nhẹ khi ra ngoài buổi tối."
            )
            temp <= 13.0 -> Quadruple(
                "Nguy cơ cảm lạnh",
                0xFF38BDF8L,
                "Trời rét (${temp.toInt()}°C)",
                "Trời rét sâu kết hợp gió lạnh làm tăng nguy cơ viêm phế quản và cảm lạnh. Giữ ấm cổ và ngực kỹ."
            )
            current.feelsLike >= 39.0 -> Quadruple(
                "Nguy cơ sốc nhiệt",
                0xFFEF4444L,
                "Oi bức đỉnh điểm (${current.feelsLike.toInt()}°C)",
                "Nhiệt độ cảm giác như lò lửa. Tránh đi từ phòng điều hòa lạnh ra trời nắng đột ngột để phòng ngừa sốc nhiệt."
            )
            else -> Quadruple(
                "Thấp",
                0xFF22C55EL,
                "Nhiệt độ ổn định",
                "Biên độ nhiệt trong ngày bình hòa, nguy cơ cảm lạnh hay sốc nhiệt rất thấp."
            )
        }
        items.add(
            LifestyleItem(
                id = "cold_flu",
                iconEmoji = "🤧",
                name = "Cảm cúm & Sốc nhiệt",
                status = fluStatus,
                badgeColor = fluColor,
                shortTip = fluTip,
                detailedAdvice = fluDetail
            )
        )

        return items
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
