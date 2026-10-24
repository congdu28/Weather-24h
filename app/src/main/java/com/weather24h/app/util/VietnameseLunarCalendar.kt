package com.weather24h.app.util

import java.time.LocalDate
import java.util.Calendar

/**
 * Loại sự kiện trong lịch
 */
enum class CalendarEventType {
    LUNAR_FESTIVAL,   // Lễ Tết theo Âm lịch
    SOLAR_HOLIDAY,    // Ngày lễ / kỷ niệm theo Dương lịch
    BUDDHIST,         // Ngày vía, lễ Phật giáo
    SOLAR_TERM,       // Ngày bắt đầu tiết khí
    MOON_PHASE        // Rằm / Mùng Một
}

/**
 * Một sự kiện (ngày lễ, ngày vía, tiết khí...) rơi vào một ngày cụ thể
 */
data class CalendarEvent(
    val name: String,
    val emoji: String,
    val type: CalendarEventType,
    /** Ngày lễ lớn / nghỉ lễ chính thức → được ưu tiên hiển thị và gửi thông báo */
    val isMajor: Boolean = false,
    /** Ngày nghỉ lễ chính thức theo Bộ luật Lao động */
    val isPublicHoliday: Boolean = false,
    val description: String = ""
) {
    val displayName: String get() = "$emoji $name"
}

/**
 * Dữ liệu ngày Âm lịch Việt Nam chuẩn xác
 */
data class LunarDate(
    val day: Int,
    val month: Int,
    val year: Int,
    val isLeapMonth: Boolean,
    val canDay: String,
    val chiDay: String,
    val canMonth: String,
    val chiMonth: String,
    val canYear: String,
    val chiYear: String,
    val solarTerm: String,
    val isAuspiciousDay: Boolean,
    val auspiciousDayType: String,
    val auspiciousHours: List<String>,
    val festival: String? = null,
    /** Số ngày của tháng Âm lịch hiện tại (29 = tháng thiếu, 30 = tháng đủ) */
    val monthLength: Int = 30,
    /** Toàn bộ sự kiện của ngày (Âm + Dương + ngày vía + tiết khí + Rằm/Mùng Một) */
    val events: List<CalendarEvent> = emptyList(),
    /** Số ngày Julius của ngày Dương lịch tương ứng */
    val julianDay: Int = 0,
    /** Ngày Dương lịch tương ứng */
    val solarDay: Int = 0,
    val solarMonth: Int = 0,
    val solarYear: Int = 0
) {
    val dayCanChi: String get() = "$canDay $chiDay"
    val monthCanChi: String get() = "$canMonth $chiMonth"
    val yearCanChi: String get() = "$canYear $chiYear"
    val fullCanChi: String get() = "Năm $yearCanChi • Tháng $monthCanChi • Ngày $dayCanChi"
    val formattedLunarShort: String get() = "$day/$month${if (isLeapMonth) " (N)" else ""} ÂL"
    val formattedLunarMedium: String get() = "Ngày $day/$month${if (isLeapMonth) " (Nhuận)" else ""} ÂL ($dayCanChi)"
    val formattedLunarFull: String get() = "Ngày $day tháng $month${if (isLeapMonth) " nhuận" else ""} năm $yearCanChi"
    val isFullMonth: Boolean get() = monthLength >= 30
    val monthName: String get() = VietnameseLunarCalendar.lunarMonthName(month) + if (isLeapMonth) " (nhuận)" else ""
    val solarDate: LocalDate? get() = if (solarYear > 0) LocalDate.of(solarYear, solarMonth, solarDay) else null
}

/**
 * Thuật toán Âm lịch Việt Nam (Múi giờ GMT+7 / Kinh tuyến 105° Đông)
 * Dựa trên thuật toán thiên văn học chính thống của TS. Hồ Ngọc Đức (Đại học Leipzig).
 * Tính toán hoàn toàn ngoại tuyến 100%, không cần kết nối mạng.
 */
object VietnameseLunarCalendar {

    private const val TIME_ZONE = 7.0

    val CAN = listOf("Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý")
    val CHI = listOf("Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi")

    /** 24 tiết khí, bắt đầu từ Xuân Phân (kinh độ Mặt Trời 0°), mỗi tiết 15° */
    val SOLAR_TERMS = listOf(
        "Xuân Phân", "Thanh Minh", "Cốc Vũ", "Lập Hạ", "Tiểu Mãn", "Mang Chủng",
        "Hạ Chí", "Tiểu Thử", "Đại Thử", "Lập Thu", "Xử Thử", "Bạch Lộ",
        "Thu Phân", "Hàn Lộ", "Sương Giáng", "Lập Đông", "Tiểu Tuyết", "Đại Tuyết",
        "Đông Chí", "Tiểu Hàn", "Đại Hàn", "Lập Xuân", "Vũ Thủy", "Kinh Trập"
    )

    // Bảng 12 sao Hoàng đạo / Hắc đạo (Thanh Long, Minh Đường...) theo Chi của tháng và Chi của ngày
    private val YELLOW_PATH_TYPES = listOf(
        "Thanh Long (Hoàng đạo)",
        "Minh Đường (Hoàng đạo)",
        "Thiên Hình (Hắc đạo)",
        "Chu Tước (Hắc đạo)",
        "Kim Quỹ (Hoàng đạo)",
        "Kim Đường (Hoàng đạo)",
        "Bạch Hổ (Hắc đạo)",
        "Ngọc Đường (Hoàng đạo)",
        "Thiên Lao (Hắc đạo)",
        "Huyền Vũ (Hắc đạo)",
        "Tư Mệnh (Hoàng đạo)",
        "Câu Trận (Hắc đạo)"
    )

    /** Khung giờ của 12 canh giờ (Tý..Hợi) */
    val HOUR_RANGES = listOf(
        "23-1h", "1-3h", "3-5h", "5-7h", "7-9h", "9-11h",
        "11-13h", "13-15h", "15-17h", "17-19h", "19-21h", "21-23h"
    )

    fun lunarMonthName(month: Int): String = when (month) {
        1 -> "Tháng Giêng"
        11 -> "Tháng Một"
        12 -> "Tháng Chạp"
        else -> "Tháng $month"
    }

    fun getTodayLunar(): LunarDate {
        val cal = Calendar.getInstance()
        return getLunarDate(
            cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.YEAR)
        )
    }

    fun getLunarDate(date: LocalDate): LunarDate = getLunarDate(date.dayOfMonth, date.monthValue, date.year)

    fun getLunarDateFromIso(isoDate: String): LunarDate {
        return try {
            val parts = isoDate.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].substring(0, 2).toInt()
            getLunarDate(day, month, year)
        } catch (_: Exception) {
            getTodayLunar()
        }
    }

    fun getLunarDate(dd: Int, mm: Int, yy: Int): LunarDate {
        val lunarRaw = convertSolar2Lunar(dd, mm, yy, TIME_ZONE)
        val lDay = lunarRaw[0]
        val lMonth = lunarRaw[1]
        val lYear = lunarRaw[2]
        val lLeap = lunarRaw[3] == 1
        val monthLength = lunarRaw[4]

        val jd = jdFromDate(dd, mm, yy)

        // Can Chi Năm
        val canYear = CAN[(lYear + 6) % 10]
        val chiYear = CHI[(lYear + 8) % 12]

        // Can Chi Ngày
        val canDay = CAN[(jd + 9) % 10]
        val chiDay = CHI[(jd + 1) % 12]

        // Can Chi Tháng (tháng nhuận dùng chung Can Chi với tháng chính)
        val canMonth = CAN[(lYear * 12 + lMonth + 3) % 10]
        val chiMonth = CHI[(lMonth + 1) % 12]

        // Tiết khí: xét tại thời điểm cuối ngày (00:00 ngày hôm sau) để ngày chuyển tiết mang tên tiết mới
        val termIdx = getSolarTermIndex(jd)
        val solarTerm = SOLAR_TERMS[termIdx]

        // Hoàng đạo / Hắc đạo
        val (isAuspicious, auspiciousType) = calculateAuspiciousDay(chiMonth, chiDay)

        // Giờ hoàng đạo
        val auspiciousHours = calculateAuspiciousHours(chiDay)

        // Sự kiện & lễ hội
        val events = buildEvents(dd, mm, lDay, lMonth, lLeap, monthLength, jd)
        val festival = pickPrimaryFestival(events)

        return LunarDate(
            day = lDay,
            month = lMonth,
            year = lYear,
            isLeapMonth = lLeap,
            canDay = canDay,
            chiDay = chiDay,
            canMonth = canMonth,
            chiMonth = chiMonth,
            canYear = canYear,
            chiYear = chiYear,
            solarTerm = solarTerm,
            isAuspiciousDay = isAuspicious,
            auspiciousDayType = auspiciousType,
            auspiciousHours = auspiciousHours,
            festival = festival,
            monthLength = monthLength,
            events = events,
            julianDay = jd,
            solarDay = dd,
            solarMonth = mm,
            solarYear = yy
        )
    }

    /** Chỉ số tiết khí (0 = Xuân Phân) của ngày có số Julius [jd] */
    fun getSolarTermIndex(jd: Int): Int {
        val sunLongitude = getSunLongitude((jd + 1).toDouble(), TIME_ZONE)
        return ((sunLongitude / 15.0).toInt()) % 24
    }

    /**
     * Chi của tháng theo Tiết khí (tháng Dần bắt đầu từ Lập Xuân 315°, tháng Mão từ Kinh Trập 345°...;
     * mỗi tháng tiết khí bắt đầu ở một tiết lẻ (Lập Xuân, Kinh Trập, Thanh Minh, Lập Hạ, Mang Chủng,
     * Tiểu Thử, Lập Thu, Bạch Lộ, Hàn Lộ, Lập Đông, Đại Tuyết, Tiểu Hàn = 285°)).
     * Dùng để tính 12 Trực theo đúng phép lịch truyền thống.
     */
    fun getSolarMonthChiIndex(jd: Int): Int {
        val sunLongitude = getSunLongitude((jd + 1).toDouble(), TIME_ZONE)
        val sector = (((sunLongitude - 315.0 + 360.0) % 360.0) / 30.0).toInt() // 0 = Dần (Lập Xuân 315°..345°), 10 = Tý (Đại Tuyết 255°..285°), 11 = Sửu (Tiểu Hàn 285°..315°)
        // Lưu ý: 01/01/2025 là giữa Đông Chí (270°) và Tiểu Hàn (285°), nên theo Tiết khí vẫn thuộc tháng Tý;
        // còn tính theo tháng Âm lịch (tháng 12 ÂL = tháng Sửu) thì Ngọ trừ Sửu = 5 (Chấp).
        return (sector + 2) % 12
    }

    private fun calculateAuspiciousDay(chiMonth: String, chiDay: String): Pair<Boolean, String> {
        val monthChiIdx = CHI.indexOf(chiMonth).coerceAtLeast(0)
        val dayChiIdx = CHI.indexOf(chiDay).coerceAtLeast(0)

        // Sao Thanh Long khởi từ:
        // Tháng Dần (2), Thân (8): Tý | Tháng Mão (3), Dậu (9): Dần | Tháng Thìn (4), Tuất (10): Thìn
        // Tháng Tỵ (5), Hợi (11): Ngọ | Tháng Tý (0), Ngọ (6): Thân | Tháng Sửu (1), Mùi (7): Tuất
        val startChiIdx = when (monthChiIdx) {
            2, 8 -> 0
            3, 9 -> 2
            4, 10 -> 4
            5, 11 -> 6
            0, 6 -> 8
            1, 7 -> 10
            else -> 0
        }

        val step = (dayChiIdx - startChiIdx + 12) % 12
        val type = YELLOW_PATH_TYPES[step]
        val isAuspicious = type.contains("Hoàng đạo")
        return Pair(isAuspicious, type)
    }

    /** Danh sách chỉ số canh giờ hoàng đạo (0 = Tý .. 11 = Hợi) theo Chi ngày */
    fun auspiciousHourIndices(dayChiIdx: Int): List<Int> = when (dayChiIdx % 6) {
        0 -> listOf(0, 1, 3, 6, 8, 9)      // Tý, Ngọ
        1 -> listOf(2, 3, 5, 8, 10, 11)    // Sửu, Mùi
        2 -> listOf(0, 1, 4, 5, 7, 10)     // Dần, Thân
        3 -> listOf(0, 2, 3, 6, 7, 9)      // Mão, Dậu
        4 -> listOf(2, 4, 5, 8, 9, 11)     // Thìn, Tuất
        else -> listOf(1, 4, 6, 7, 10, 11) // Tỵ, Hợi
    }

    private fun calculateAuspiciousHours(chiDay: String): List<String> {
        val dayChiIdx = CHI.indexOf(chiDay).coerceAtLeast(0)
        return auspiciousHourIndices(dayChiIdx).map { "${CHI[it]} (${HOUR_RANGES[it]})" }
    }

    // ==========================================
    // NGÀY LỄ, NGÀY VÍA & SỰ KIỆN
    // ==========================================

    private fun buildEvents(
        sDay: Int, sMonth: Int,
        lDay: Int, lMonth: Int, isLeap: Boolean, monthLength: Int,
        jd: Int
    ): List<CalendarEvent> {
        val list = mutableListOf<CalendarEvent>()

        // 1. Lễ Tết Âm lịch (không áp dụng cho tháng nhuận)
        if (!isLeap) {
            lunarFestival(lDay, lMonth, monthLength)?.let { list.add(it) }
            buddhistDay(lDay, lMonth)?.let { list.add(it) }
        }

        // 2. Ngày lễ Dương lịch
        solarHoliday(sDay, sMonth)?.let { list.add(it) }

        // 3. Ngày bắt đầu tiết khí
        val termToday = getSolarTermIndex(jd)
        val termYesterday = getSolarTermIndex(jd - 1)
        if (termToday != termYesterday) {
            val name = SOLAR_TERMS[termToday]
            if (name == "Thanh Minh") {
                list.add(CalendarEvent("Tiết Thanh Minh (Tảo mộ)", "🌿", CalendarEventType.SOLAR_TERM, isMajor = true,
                    description = "Thời điểm con cháu đi tảo mộ, sửa sang mộ phần tổ tiên."))
            } else {
                list.add(CalendarEvent("Bắt đầu tiết $name", "☀️", CalendarEventType.SOLAR_TERM,
                    description = solarTermDescription(name)))
            }
        }

        // 4. Rằm / Mùng Một (luôn đặt cuối cùng, mức ưu tiên thấp nhất)
        if (lDay == 15) list.add(CalendarEvent("Ngày Rằm (Vọng)", "🌕", CalendarEventType.MOON_PHASE,
            description = "Ngày trăng tròn, nhiều gia đình thắp hương, ăn chay, đi lễ chùa."))
        if (lDay == 1) list.add(CalendarEvent("Ngày Mùng Một (Sóc)", "🌑", CalendarEventType.MOON_PHASE,
            description = "Ngày đầu tháng Âm lịch, thắp hương cầu an, khởi đầu tháng mới."))

        return list
    }

    private fun lunarFestival(d: Int, m: Int, monthLength: Int): CalendarEvent? {
        val t = CalendarEventType.LUNAR_FESTIVAL
        return when {
            d == 1 && m == 1 -> CalendarEvent("Tết Nguyên Đán (Mùng 1)", "🌸", t, true, true, "Ngày đầu năm mới Âm lịch – lễ Tết lớn nhất của người Việt.")
            d == 2 && m == 1 -> CalendarEvent("Mùng 2 Tết", "🌸", t, true, true, "Mùng 1 Tết cha, mùng 2 Tết mẹ, mùng 3 Tết thầy.")
            d == 3 && m == 1 -> CalendarEvent("Mùng 3 Tết", "🌸", t, true, true, "Ngày Tết thầy, hóa vàng tiễn ông bà ở nhiều địa phương.")
            d == 10 && m == 1 -> CalendarEvent("Vía Thần Tài", "💰", t, true, false, "Ngày cúng Thần Tài cầu may mắn, tài lộc cả năm.")
            d == 15 && m == 1 -> CalendarEvent("Tết Nguyên Tiêu (Rằm tháng Giêng)", "🏮", t, true, false, "Rằm Thượng Nguyên – \"Lễ Phật quanh năm không bằng Rằm tháng Giêng\".")
            d == 3 && m == 3 -> CalendarEvent("Tết Hàn Thực", "🥟", t, true, false, "Ngày làm bánh trôi, bánh chay dâng cúng tổ tiên.")
            d == 10 && m == 3 -> CalendarEvent("Giỗ Tổ Hùng Vương", "👑", t, true, true, "Ngày tưởng nhớ các Vua Hùng – nghỉ lễ chính thức.")
            d == 15 && m == 4 -> CalendarEvent("Lễ Phật Đản", "🪷", t, true, false, "Kỷ niệm ngày Đức Phật Thích Ca đản sinh.")
            d == 5 && m == 5 -> CalendarEvent("Tết Đoan Ngọ", "🌾", t, true, false, "Tết diệt sâu bọ, ăn rượu nếp, hoa quả đầu mùa.")
            d == 15 && m == 7 -> CalendarEvent("Lễ Vu Lan & Xá Tội Vong Nhân", "🏮", t, true, false, "Rằm Trung Nguyên – mùa báo hiếu cha mẹ, cúng cô hồn.")
            d == 15 && m == 8 -> CalendarEvent("Tết Trung Thu", "🥮", t, true, false, "Tết đoàn viên, rước đèn phá cỗ cho thiếu nhi.")
            d == 9 && m == 9 -> CalendarEvent("Tết Trùng Cửu", "🍁", t, false, false, "Tết Trùng Dương, lên cao ngắm cảnh, cầu sức khỏe.")
            d == 10 && m == 10 -> CalendarEvent("Tết Trùng Thập (Cơm mới)", "🌾", t, false, false, "Tết cơm mới, tạ ơn trời đất sau mùa gặt.")
            d == 15 && m == 10 -> CalendarEvent("Tết Hạ Nguyên", "🌕", t, false, false, "Rằm tháng Mười – lễ cúng cơm mới ở nhiều nơi.")
            d == 23 && m == 12 -> CalendarEvent("Tết Ông Công Ông Táo", "🐟", t, true, false, "Tiễn Táo Quân về chầu trời, thả cá chép.")
            d == monthLength && m == 12 -> CalendarEvent("Tất Niên – Đêm Giao Thừa", "🎆", t, true, true, "Ngày cuối cùng của năm Âm lịch, cúng tất niên và đón giao thừa.")
            else -> null
        }
    }

    private fun buddhistDay(d: Int, m: Int): CalendarEvent? {
        val t = CalendarEventType.BUDDHIST
        return when {
            d == 19 && m == 2 -> CalendarEvent("Vía Quan Âm (Đản sinh)", "🙏", t, false, false, "Ngày vía Bồ Tát Quán Thế Âm đản sinh.")
            d == 19 && m == 6 -> CalendarEvent("Vía Quan Âm (Thành đạo)", "🙏", t, false, false, "Ngày vía Bồ Tát Quán Thế Âm thành đạo.")
            d == 19 && m == 9 -> CalendarEvent("Vía Quan Âm (Xuất gia)", "🙏", t, false, false, "Ngày vía Bồ Tát Quán Thế Âm xuất gia.")
            d == 8 && m == 12 -> CalendarEvent("Phật Thích Ca thành đạo", "🪷", t, false, false, "Ngày Đức Phật Thích Ca thành đạo.")
            else -> null
        }
    }

    private fun solarHoliday(d: Int, m: Int): CalendarEvent? {
        val t = CalendarEventType.SOLAR_HOLIDAY
        return when (m * 100 + d) {
            101 -> CalendarEvent("Tết Dương lịch", "🎉", t, true, true, "Ngày đầu năm mới Dương lịch – nghỉ lễ chính thức.")
            109 -> CalendarEvent("Ngày Học sinh – Sinh viên", "🎓", t)
            203 -> CalendarEvent("Thành lập Đảng CSVN", "🚩", t)
            214 -> CalendarEvent("Lễ Tình nhân (Valentine)", "💝", t, true)
            227 -> CalendarEvent("Ngày Thầy thuốc Việt Nam", "🩺", t)
            308 -> CalendarEvent("Quốc tế Phụ nữ", "💐", t, true)
            326 -> CalendarEvent("Thành lập Đoàn TNCS Hồ Chí Minh", "🔰", t)
            401 -> CalendarEvent("Ngày Cá tháng Tư", "🐟", t)
            430 -> CalendarEvent("Giải phóng miền Nam", "🇻🇳", t, true, true, "Ngày thống nhất đất nước – nghỉ lễ chính thức.")
            501 -> CalendarEvent("Quốc tế Lao động", "🛠️", t, true, true, "Nghỉ lễ chính thức.")
            507 -> CalendarEvent("Chiến thắng Điện Biên Phủ", "🎖️", t)
            519 -> CalendarEvent("Ngày sinh Chủ tịch Hồ Chí Minh", "🌺", t)
            601 -> CalendarEvent("Quốc tế Thiếu nhi", "🎈", t, true)
            628 -> CalendarEvent("Ngày Gia đình Việt Nam", "👨‍👩‍👧", t)
            727 -> CalendarEvent("Ngày Thương binh Liệt sĩ", "🕯️", t)
            819 -> CalendarEvent("Cách mạng tháng Tám", "🚩", t)
            902 -> CalendarEvent("Quốc khánh", "🇻🇳", t, true, true, "Quốc khánh nước CHXHCN Việt Nam – nghỉ lễ chính thức.")
            1010 -> CalendarEvent("Giải phóng Thủ đô", "🏛️", t)
            1020 -> CalendarEvent("Ngày Phụ nữ Việt Nam", "💐", t, true)
            1031 -> CalendarEvent("Halloween", "🎃", t)
            1120 -> CalendarEvent("Ngày Nhà giáo Việt Nam", "📚", t, true)
            1222 -> CalendarEvent("Thành lập Quân đội Nhân dân VN", "⭐", t)
            1224 -> CalendarEvent("Đêm Giáng sinh", "🎄", t, true)
            1225 -> CalendarEvent("Lễ Giáng sinh", "🎄", t, true)
            else -> null
        }
    }

    private fun solarTermDescription(name: String): String = when (name) {
        "Lập Xuân" -> "Bắt đầu mùa xuân."
        "Lập Hạ" -> "Bắt đầu mùa hè."
        "Lập Thu" -> "Bắt đầu mùa thu."
        "Lập Đông" -> "Bắt đầu mùa đông."
        "Xuân Phân" -> "Giữa xuân, ngày và đêm dài bằng nhau."
        "Thu Phân" -> "Giữa thu, ngày và đêm dài bằng nhau."
        "Hạ Chí" -> "Giữa hè, ngày dài nhất trong năm."
        "Đông Chí" -> "Giữa đông, đêm dài nhất trong năm."
        "Kinh Trập" -> "Sâu bọ thức giấc."
        "Cốc Vũ" -> "Mưa rào, thuận lợi cho trồng cấy."
        "Mang Chủng" -> "Lúa trổ bông, gieo hạt."
        "Tiểu Thử" -> "Nắng nóng nhẹ."
        "Đại Thử" -> "Nắng nóng gay gắt nhất."
        "Xử Thử" -> "Mưa ngâu, hết nóng."
        "Bạch Lộ" -> "Nắng nhạt, sương trắng xuất hiện."
        "Hàn Lộ" -> "Sương lạnh, trời mát mẻ."
        "Sương Giáng" -> "Sương mù xuất hiện."
        "Tiểu Tuyết" -> "Trời bắt đầu lạnh."
        "Đại Tuyết" -> "Lạnh nhiều."
        "Tiểu Hàn" -> "Rét nhẹ."
        "Đại Hàn" -> "Rét đậm nhất năm."
        "Vũ Thủy" -> "Mưa ẩm, mưa phùn."
        "Tiểu Mãn" -> "Lũ nhỏ, duối vàng."
        "Thanh Minh" -> "Trời trong sáng."
        else -> ""
    }

    private fun pickPrimaryFestival(events: List<CalendarEvent>): String? {
        if (events.isEmpty()) return null
        val ordered = events.sortedBy {
            when {
                it.type == CalendarEventType.LUNAR_FESTIVAL && it.isMajor -> 0
                it.type == CalendarEventType.SOLAR_HOLIDAY && it.isPublicHoliday -> 1
                it.type == CalendarEventType.LUNAR_FESTIVAL -> 2
                it.type == CalendarEventType.SOLAR_TERM && it.isMajor -> 3
                it.type == CalendarEventType.SOLAR_HOLIDAY && it.isMajor -> 4
                it.type == CalendarEventType.BUDDHIST -> 5
                it.type == CalendarEventType.MOON_PHASE -> 6
                it.type == CalendarEventType.SOLAR_HOLIDAY -> 7
                else -> 8 // Tiết khí thường không coi là lễ hội
            }
        }
        val first = ordered.first()
        if (first.type == CalendarEventType.SOLAR_TERM && !first.isMajor) return null
        return first.displayName
    }

    /**
     * Tính đếm ngược ngày đến Rằm hoặc Mùng Một tiếp theo
     * @param monthLength Số ngày thực tế của tháng Âm lịch hiện tại (29 hoặc 30)
     */
    fun getNextMoonEventCountdown(currentLunarDay: Int, monthLength: Int = 30): Pair<String, Int> {
        return if (currentLunarDay < 15) {
            Pair("ngày tới Ngày Rằm (15 ÂL)", 15 - currentLunarDay)
        } else {
            Pair("ngày tới Mùng Một tháng sau", (monthLength - currentLunarDay) + 1)
        }
    }

    fun getNextMoonEventCountdown(lunar: LunarDate): Pair<String, Int> =
        getNextMoonEventCountdown(lunar.day, lunar.monthLength)

    // ==========================================
    // ĐỔI NGÀY ÂM → DƯƠNG
    // ==========================================

    /**
     * Đổi ngày Âm lịch sang Dương lịch.
     * @return Ngày Dương lịch, hoặc null nếu ngày không tồn tại (ví dụ tháng nhuận không có trong năm đó, hay ngày 30 của tháng thiếu)
     */
    fun convertLunarToSolar(lunarDay: Int, lunarMonth: Int, lunarYear: Int, isLeap: Boolean = false): LocalDate? {
        if (lunarMonth !in 1..12 || lunarDay !in 1..30) return null
        val a11: Int
        val b11: Int
        if (lunarMonth < 11) {
            a11 = getLunarMonth11(lunarYear - 1, TIME_ZONE)
            b11 = getLunarMonth11(lunarYear, TIME_ZONE)
        } else {
            a11 = getLunarMonth11(lunarYear, TIME_ZONE)
            b11 = getLunarMonth11(lunarYear + 1, TIME_ZONE)
        }
        val k = Math.floor(0.5 + (a11 - 2415021.076998695) / 29.530588853).toInt()
        var off = lunarMonth - 11
        if (off < 0) off += 12
        if (b11 - a11 > 365) {
            val leapOff = getLeapMonthOffset(a11, TIME_ZONE)
            var leapMonth = leapOff - 2
            if (leapMonth <= 0) leapMonth += 12
            if (isLeap && lunarMonth != leapMonth) {
                return null
            } else if (isLeap || off >= leapOff) {
                off += 1
            }
        } else if (isLeap) {
            return null
        }
        val monthStart = getNewMoonDay(k + off, TIME_ZONE)
        val nextMonthStart = getNewMoonDay(k + off + 1, TIME_ZONE)
        if (lunarDay > nextMonthStart - monthStart) return null
        return jdToLocalDate(monthStart + lunarDay - 1)
    }

    /** Tháng nhuận của năm Âm lịch [lunarYear] (0 nếu không có) */
    fun getLeapMonth(lunarYear: Int): Int {
        // Chu kỳ [tháng 11 năm trước → tháng 11 năm nay] chứa tháng 1..10 của năm Âm lịch
        leapMonthInCycle(lunarYear - 1)?.let { if (it in 1..10) return it }
        // Chu kỳ [tháng 11 năm nay → tháng 11 năm sau] chứa tháng 11, 12 của năm Âm lịch
        leapMonthInCycle(lunarYear)?.let { if (it == 11 || it == 12) return it }
        return 0
    }

    /** Tháng nhuận (nếu có) trong chu kỳ bắt đầu từ tháng 11 Âm lịch của năm Dương [yy] */
    private fun leapMonthInCycle(yy: Int): Int? {
        val a11 = getLunarMonth11(yy, TIME_ZONE)
        val b11 = getLunarMonth11(yy + 1, TIME_ZONE)
        if (b11 - a11 <= 365) return null
        val leapOff = getLeapMonthOffset(a11, TIME_ZONE)
        var leapMonth = leapOff - 2
        if (leapMonth <= 0) leapMonth += 12
        return leapMonth
    }

    fun jdToLocalDate(jd: Int): LocalDate = LocalDate.ofEpochDay((jd - 2440588).toLong())

    fun jdFromLocalDate(date: LocalDate): Int = jdFromDate(date.dayOfMonth, date.monthValue, date.year)

    // ==========================================
    // CÁC HÀM THIÊN VĂN THUẦN TÚY (ASTRONOMY)
    // ==========================================

    private fun jdFromDate(dd: Int, mm: Int, yy: Int): Int {
        val a = (14 - mm) / 12
        val y = yy + 4800 - a
        val m = mm + 12 * a - 3
        var jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
        if (jd < 2299161) {
            jd = dd + (153 * m + 2) / 5 + 365 * y + y / 4 - 32083
        }
        return jd
    }

    private fun getSunLongitude(dayNumber: Double, timeZone: Double): Double {
        val t = (dayNumber - 0.5 - timeZone / 24.0 - 2451545.0) / 36525.0
        val t2 = t * t
        val dr = Math.PI / 180.0
        val l0 = 280.46645 + 36000.76983 * t + 0.0003032 * t2
        val m = 357.52910 + 35999.05030 * t - 0.0001559 * t2 - 0.00000048 * t * t2
        val c = (1.914600 - 0.004817 * t - 0.000014 * t2) * Math.sin(dr * m) +
                (0.019993 - 0.000101 * t) * Math.sin(dr * 2 * m) +
                0.000290 * Math.sin(dr * 3 * m)
        val theta = l0 + c
        val omega = 125.04 - 1934.136 * t
        val lambda = theta - 0.00569 - 0.00478 * Math.sin(dr * omega)
        var lambdaDeg = lambda % 360.0
        if (lambdaDeg < 0.0) lambdaDeg += 360.0
        return lambdaDeg
    }

    private fun getNewMoonDay(k: Int, timeZone: Double): Int {
        val t = k / 1236.85
        val t2 = t * t
        val t3 = t2 * t
        val dr = Math.PI / 180.0
        val jd = 2415020.75933 + 29.53058868 * k + 0.0001178 * t2 - 0.000000155 * t3
        val m = 359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3
        val mprime = 306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3
        val f = 21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3
        val c1 = (0.1734 - 0.000393 * t) * Math.sin(dr * m) + 0.0021 * Math.sin(dr * 2 * m)
        val c2 = -0.4068 * Math.sin(dr * mprime) + 0.0161 * Math.sin(dr * 2 * mprime)
        val c3 = -0.0004 * Math.sin(dr * 3 * mprime)
        val c4 = 0.0104 * Math.sin(dr * 2 * f) - 0.0051 * Math.sin(dr * (m + mprime))
        val c5 = -0.0074 * Math.sin(dr * (m - mprime)) + 0.0004 * Math.sin(dr * (2 * f + m))
        val c6 = -0.0004 * Math.sin(dr * (2 * f - m)) - 0.0006 * Math.sin(dr * (2 * f + mprime))
        val c7 = 0.0010 * Math.sin(dr * (2 * f - mprime)) + 0.0005 * Math.sin(dr * (2 * mprime + m))
        val deltaT = if (t < -11.0) {
            0.001 + 0.000839 * t + 0.0002261 * t2 - 0.00000845 * t3 - 0.000000081 * t * t3
        } else {
            -0.000278 + 0.000265 * t + 0.000262 * t2
        }
        val jdn = jd + c1 + c2 + c3 + c4 + c5 + c6 + c7 - deltaT
        return Math.floor(jdn + 0.5 + timeZone / 24.0).toInt()
    }

    private fun getLunarMonth11(yy: Int, timeZone: Double): Int {
        val off = jdFromDate(31, 12, yy) - 2415021
        val k = Math.floor(off / 29.530588853).toInt()
        var nm = getNewMoonDay(k, timeZone)
        val sunLong = getSunLongitude(nm.toDouble(), timeZone)
        if (sunLong >= 270.0) {
            nm = getNewMoonDay(k - 1, timeZone)
        }
        return nm
    }

    /**
     * Xác định vị trí tháng nhuận (tính từ tháng 11 Âm lịch): tháng nhuận là tháng đầu tiên
     * sau tháng 11 không chứa Trung khí. Bắt đầu so sánh từ tháng liền sau tháng 11.
     */
    private fun getLeapMonthOffset(a11: Int, timeZone: Double): Int {
        val k = Math.floor((a11 - 2415021.076998695) / 29.530588853 + 0.5).toInt()
        var i = 1
        var arc = (getSunLongitude(getNewMoonDay(k + i, timeZone).toDouble(), timeZone) / 30.0).toInt()
        var last: Int
        do {
            last = arc
            i++
            arc = (getSunLongitude(getNewMoonDay(k + i, timeZone).toDouble(), timeZone) / 30.0).toInt()
        } while (arc != last && i < 14)
        return i - 1
    }

    /** @return [ngày, tháng, năm, nhuận(0/1), số ngày của tháng Âm] */
    private fun convertSolar2Lunar(dd: Int, mm: Int, yy: Int, timeZone: Double): IntArray {
        val dayNumber = jdFromDate(dd, mm, yy)
        val k = Math.floor((dayNumber - 2415021.076998695) / 29.530588853).toInt()
        var monthStart = getNewMoonDay(k + 1, timeZone)
        var nextMonthStart: Int
        if (monthStart > dayNumber) {
            nextMonthStart = monthStart
            monthStart = getNewMoonDay(k, timeZone)
        } else {
            nextMonthStart = getNewMoonDay(k + 2, timeZone)
        }
        var a11 = getLunarMonth11(yy, timeZone)
        var b11 = a11
        var lYear: Int
        if (a11 >= monthStart) {
            lYear = yy
            a11 = getLunarMonth11(yy - 1, timeZone)
        } else {
            lYear = yy + 1
            b11 = getLunarMonth11(yy + 1, timeZone)
        }
        val lDay = dayNumber - monthStart + 1
        val diff = (monthStart - a11) / 29
        var lLeap = 0
        var lMonth = diff + 11
        if (b11 - a11 > 365) {
            val leapMonthDiff = getLeapMonthOffset(a11, timeZone)
            if (diff >= leapMonthDiff) {
                lMonth = diff + 10
                if (diff == leapMonthDiff) {
                    lLeap = 1
                }
            }
        }
        if (lMonth > 12) {
            lMonth -= 12
        }
        if (lMonth >= 11 && diff < 4) {
            lYear -= 1
        }
        val monthLength = (nextMonthStart - monthStart).coerceIn(29, 30)
        return intArrayOf(lDay, lMonth, lYear, lLeap, monthLength)
    }

    // ==========================================
    // TIỆN ÍCH LỊCH THÁNG & SỰ KIỆN LỄ HỘI
    // ==========================================

    data class MonthDayInfo(
        val solarDay: Int,
        val solarMonth: Int,
        val solarYear: Int,
        val dayOfWeek: Int, // 1: Thứ Hai .. 7: Chủ Nhật
        val lunarDate: LunarDate,
        val isToday: Boolean
    )

    /**
     * Tạo dữ liệu cho toàn bộ các ngày trong tháng Dương lịch
     */
    fun getMonthDays(year: Int, month: Int): List<MonthDayInfo> {
        val daysInMonth = java.time.YearMonth.of(year, month).lengthOfMonth()
        val today = LocalDate.now()
        val result = mutableListOf<MonthDayInfo>()
        for (d in 1..daysInMonth) {
            val date = LocalDate.of(year, month, d)
            val lunar = getLunarDate(d, month, year)
            result.add(
                MonthDayInfo(
                    solarDay = d,
                    solarMonth = month,
                    solarYear = year,
                    dayOfWeek = date.dayOfWeek.value,
                    lunarDate = lunar,
                    isToday = date == today
                )
            )
        }
        return result
    }

    /** Sự kiện sắp tới (kèm ngày Dương lịch & số ngày còn lại) */
    data class UpcomingEvent(
        val date: LocalDate,
        val lunar: LunarDate,
        val event: CalendarEvent,
        val daysLeft: Long
    )

    /**
     * Liệt kê các ngày lễ / sự kiện trong [days] ngày tới (tính cả hôm nay)
     * @param includeMoonPhases Có bao gồm Rằm / Mùng Một hay không
     * @param includeMinor Có bao gồm các ngày kỷ niệm nhỏ & tiết khí hay không
     */
    fun getUpcomingEvents(
        from: LocalDate = LocalDate.now(),
        days: Int = 365,
        includeMoonPhases: Boolean = true,
        includeMinor: Boolean = true
    ): List<UpcomingEvent> {
        val result = mutableListOf<UpcomingEvent>()
        for (i in 0 until days) {
            val date = from.plusDays(i.toLong())
            val lunar = getLunarDate(date)
            lunar.events.forEach { ev ->
                val keep = when (ev.type) {
                    CalendarEventType.MOON_PHASE -> includeMoonPhases
                    CalendarEventType.SOLAR_TERM -> includeMinor || ev.isMajor
                    else -> includeMinor || ev.isMajor || ev.isPublicHoliday
                }
                if (keep) result.add(UpcomingEvent(date, lunar, ev, i.toLong()))
            }
        }
        return result
    }

    private fun isNotifiableEvent(ev: CalendarEvent): Boolean =
        ev.type != CalendarEventType.MOON_PHASE &&
            (ev.isMajor || ev.isPublicHoliday || ev.type == CalendarEventType.LUNAR_FESTIVAL || ev.type == CalendarEventType.BUDDHIST)

    /**
     * Kiểm tra sự kiện lễ hội hoặc Rằm/Mùng Một đang đến gần (hôm nay hoặc ngày mai)
     */
    fun getApproachingFestivalEvent(currentLocalDate: LocalDate = LocalDate.now()): Pair<String, String>? {
        val todayLunar = getLunarDate(currentLocalDate)
        val tomorrow = currentLocalDate.plusDays(1)
        val tomorrowLunar = getLunarDate(tomorrow)

        // 1. Nếu hôm nay có lễ hội
        todayLunar.events.firstOrNull { isNotifiableEvent(it) }?.let { ev ->
            return Pair("Hôm nay: ${ev.displayName}", "${todayLunar.formattedLunarFull} (${todayLunar.dayCanChi}) • ${todayLunar.solarTerm}")
        }

        // 2. Nếu ngày mai có lễ hội
        tomorrowLunar.events.firstOrNull { isNotifiableEvent(it) }?.let { ev ->
            return Pair("Ngày mai: ${ev.displayName}", "Ngày mai là ${tomorrowLunar.formattedLunarFull} (${tomorrowLunar.dayCanChi})")
        }

        // 3. Nếu ngày mai là Rằm (hôm nay là 14 ÂL)
        if (tomorrowLunar.day == 15) {
            return Pair("Ngày mai là Ngày Rằm (15 ÂL)", "Rằm ${tomorrowLunar.monthName} ÂL (${tomorrowLunar.dayCanChi}) - ${tomorrowLunar.auspiciousDayType}")
        }

        // 4. Nếu hôm nay là Rằm
        if (todayLunar.day == 15) {
            return Pair("Hôm nay là Ngày Rằm (15 ÂL) 🌕", "${todayLunar.formattedLunarFull} (${todayLunar.dayCanChi}) - ${todayLunar.auspiciousDayType}")
        }

        // 5. Nếu ngày mai là Mùng Một
        if (tomorrowLunar.day == 1) {
            return Pair("Ngày mai là Mùng Một đầu tháng mới 🌑", "Mùng 1 ${tomorrowLunar.monthName} ÂL (${tomorrowLunar.dayCanChi}) - Chúc bạn tháng mới an lành!")
        }

        // 6. Nếu hôm nay là Mùng Một
        if (todayLunar.day == 1) {
            return Pair("Hôm nay là Mùng Một đầu tháng 🌑", "Mùng 1 ${todayLunar.monthName} ÂL (${todayLunar.dayCanChi}) - Khởi đầu tháng mới may mắn, hanh thông!")
        }

        return null
    }
}
