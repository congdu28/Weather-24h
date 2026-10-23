package com.weather24h.app.util

import java.time.LocalDate
import java.util.Calendar

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
    val festival: String? = null
) {
    val dayCanChi: String get() = "$canDay $chiDay"
    val monthCanChi: String get() = "$canMonth $chiMonth"
    val yearCanChi: String get() = "$canYear $chiYear"
    val fullCanChi: String get() = "Năm $yearCanChi • Tháng $monthCanChi • Ngày $dayCanChi"
    val formattedLunarShort: String get() = "$day/$month${if (isLeapMonth) " (N)" else ""} ÂL"
    val formattedLunarMedium: String get() = "Ngày $day/$month${if (isLeapMonth) " (Nhuận)" else ""} ÂL ($dayCanChi)"
    val formattedLunarFull: String get() = "Ngày $day tháng $month${if (isLeapMonth) " nhuận" else ""} năm $yearCanChi"
}

/**
 * Thuật toán Âm lịch Việt Nam (Múi giờ GMT+7 / Kinh tuyến 105° Đông)
 * Dựa trên thuật toán thiên văn học chính thống của TS. Hồ Ngọc Đức (Đại học Leipzig).
 * Tính toán hoàn toàn ngoại tuyến 100%, không cần kết nối mạng.
 */
object VietnameseLunarCalendar {

    private const val TIME_ZONE = 7.0

    private val CAN = listOf("Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý")
    private val CHI = listOf("Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi")

    private val SOLAR_TERMS = listOf(
        "Xuân Phân", "Thanh Minh", "Cốc Vũ", "Lập Hạ", "Tiểu Mãn", "Mang Chủng",
        "Hạ Chí", "Tiểu Thử", "Đại Thử", "Lập Thu", "Xử Thử", "Bạch Lộ",
        "Thu Phân", "Hàn Lộ", "Sương Giáng", "Lập Đông", "Tiểu Tuyết", "Đại Tuyết",
        "Đông Chí", "Tiểu Hàn", "Đại Hàn", "Lập Xuân", "Vũ Thủy", "Kinh Trập"
    )

    // Bảng 12 Trực / Hoàng Đạo theo Chi của tháng và Chi của ngày
    // Trả về Tên Hoàng đạo/Hắc đạo và cờ isAuspicious
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

    fun getTodayLunar(): LunarDate {
        val cal = Calendar.getInstance()
        return getLunarDate(
            cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.YEAR)
        )
    }

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

        val jd = jdFromDate(dd, mm, yy)

        // Can Chi Năm
        val canYear = CAN[(lYear + 6) % 10]
        val chiYear = CHI[(lYear + 8) % 12]

        // Can Chi Ngày
        val canDay = CAN[(jd + 9) % 10]
        val chiDay = CHI[(jd + 1) % 12]

        // Can Chi Tháng
        val canMonth = CAN[(lYear * 12 + lMonth + 3) % 10]
        val chiMonth = CHI[(lMonth + 1) % 12]

        // Tiết khí
        val sunLongitude = getSunLongitude(jd.toDouble(), TIME_ZONE)
        val termIdx = ((sunLongitude / 15.0).toInt()) % 24
        val solarTerm = SOLAR_TERMS[termIdx]

        // Hoàng đạo / Hắc đạo
        val (isAuspicious, auspiciousType) = calculateAuspiciousDay(chiMonth, chiDay)

        // Giờ hoàng đạo
        val auspiciousHours = calculateAuspiciousHours(chiDay)

        // Lễ hội truyền thống
        val festival = getFestival(lDay, lMonth)

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
            festival = festival
        )
    }

    private fun calculateAuspiciousDay(chiMonth: String, chiDay: String): Pair<Boolean, String> {
        val monthChiIdx = CHI.indexOf(chiMonth).coerceAtLeast(0)
        val dayChiIdx = CHI.indexOf(chiDay).coerceAtLeast(0)

        // Thứ tự sao xuất phát từ Chi của tháng
        // Tháng Dần (2), Thân (8): Tý là khởi đầu
        // Tháng Mão (3), Dậu (9): Dần là khởi đầu
        // Tháng Thìn (4), Tuất (10): Thìn là khởi đầu
        // Tháng Tỵ (5), Hợi (11): Ngọ là khởi đầu
        // Tháng Tý (0), Ngọ (6): Thân là khởi đầu
        // Tháng Sửu (1), Mùi (7): Tuất là khởi đầu
        val startChiIdx = when (monthChiIdx) {
            2, 8 -> 0   // Dần, Thân -> Tý
            3, 9 -> 2   // Mão, Dậu -> Dần
            4, 10 -> 4  // Thìn, Tuất -> Thìn
            5, 11 -> 6  // Tỵ, Hợi -> Ngọ
            0, 6 -> 8   // Tý, Ngọ -> Thân
            1, 7 -> 10  // Sửu, Mùi -> Tuất
            else -> 0
        }

        val step = (dayChiIdx - startChiIdx + 12) % 12
        val type = YELLOW_PATH_TYPES[step]
        val isAuspicious = type.contains("Hoàng đạo")
        return Pair(isAuspicious, type)
    }

    private fun calculateAuspiciousHours(chiDay: String): List<String> {
        val dayChiIdx = CHI.indexOf(chiDay).coerceAtLeast(0)
        return when (dayChiIdx % 6) {
            0 -> listOf("Tý (23-1h)", "Sửu (1-3h)", "Mão (5-7h)", "Ngọ (11-13h)", "Thân (15-17h)", "Dậu (17-19h)") // Tý, Ngọ
            1 -> listOf("Dần (3-5h)", "Mão (5-7h)", "Tỵ (9-11h)", "Thân (15-17h)", "Tuất (19-21h)", "Hợi (21-23h)") // Sửu, Mùi
            2 -> listOf("Tý (23-1h)", "Sửu (1-3h)", "Thìn (7-9h)", "Tỵ (9-11h)", "Mùi (13-15h)", "Tuất (19-21h)") // Dần, Thân
            3 -> listOf("Tý (23-1h)", "Dần (3-5h)", "Mão (5-7h)", "Ngọ (11-13h)", "Mùi (13-15h)", "Dậu (17-19h)") // Mão, Dậu
            4 -> listOf("Dần (3-5h)", "Thìn (7-9h)", "Tỵ (9-11h)", "Thân (15-17h)", "Dậu (17-19h)", "Hợi (21-23h)") // Thìn, Tuất
            else -> listOf("Sửu (1-3h)", "Thìn (7-9h)", "Ngọ (11-13h)", "Mùi (13-15h)", "Tuất (19-21h)", "Hợi (21-23h)") // Tỵ, Hợi
        }
    }

    private fun getFestival(lDay: Int, lMonth: Int): String? {
        return when {
            lDay == 1 && lMonth == 1 -> "🌸 Tết Nguyên Đán (Mùng 1)"
            lDay == 2 && lMonth == 1 -> "🌸 Mùng 2 Tết"
            lDay == 3 && lMonth == 1 -> "🌸 Mùng 3 Tết"
            lDay == 15 && lMonth == 1 -> "🏮 Tết Nguyên Tiêu (Rằm tháng Giêng)"
            lDay == 3 && lMonth == 3 -> "🥟 Tết Hàn Thực"
            lDay == 10 && lMonth == 3 -> "👑 Giỗ Tổ Hùng Vương (10/3 ÂL)"
            lDay == 15 && lMonth == 4 -> "🪷 Lễ Phật Đản (Rằm tháng 4)"
            lDay == 5 && lMonth == 5 -> "🌾 Tết Đoan Ngọ (5/5 ÂL)"
            lDay == 15 && lMonth == 7 -> "🏮 Lễ Vu Lan & Xá Tội Vong Nhân"
            lDay == 15 && lMonth == 8 -> "🥮 Tết Trung Thu (Rằm tháng 8)"
            lDay == 9 && lMonth == 9 -> "🍁 Tết Trùng Cửu"
            lDay == 10 && lMonth == 10 -> "🌾 Tết Trùng Thập (Cơm mới)"
            lDay == 23 && lMonth == 12 -> "🐟 Tết Ông Công Ông Táo (23 tháng Chạp)"
            lDay >= 29 && lMonth == 12 -> "🎆 Đêm Giao Thừa / Tất Niên"
            lDay == 15 -> "🌕 Ngày Rằm (Vọng)"
            lDay == 1 -> "🌑 Ngày Mùng Một (Sóc)"
            else -> null
        }
    }

    /**
     * Tính đếm ngược ngày đến Rằm hoặc Mùng Một tiếp theo
     */
    fun getNextMoonEventCountdown(currentLunarDay: Int): Pair<String, Int> {
        return if (currentLunarDay < 15) {
            val daysLeft = 15 - currentLunarDay
            Pair("ngày tới Ngày Rằm (15 ÂL)", daysLeft)
        } else {
            // Giả định tháng có 29 hoặc 30 ngày
            val daysLeft = (30 - currentLunarDay) + 1
            Pair("ngày tới Mùng Một tháng sau", daysLeft)
        }
    }

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
        val deltaT = if (t < -4.0) 0.00117 * (t + 4.0) * (t + 4.0) else 0.0
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

    private fun getLeapMonthOffset(a11: Int, timeZone: Double): Int {
        var k = Math.floor((a11 - 2415021.076998695) / 29.530588853 + 0.5).toInt()
        var i = 1
        var arc = (getSunLongitude(getNewMoonDay(k, timeZone).toDouble(), timeZone) / 30.0).toInt()
        var last: Int
        do {
            last = arc
            k++
            arc = (getSunLongitude(getNewMoonDay(k, timeZone).toDouble(), timeZone) / 30.0).toInt()
            i++
        } while (arc != last && i < 14)
        return i - 1
    }

    private fun convertSolar2Lunar(dd: Int, mm: Int, yy: Int, timeZone: Double): IntArray {
        val dayNumber = jdFromDate(dd, mm, yy)
        val k = Math.floor((dayNumber - 2415021.076998695) / 29.530588853).toInt()
        var monthStart = getNewMoonDay(k + 1, timeZone)
        if (monthStart > dayNumber) {
            monthStart = getNewMoonDay(k, timeZone)
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
        return intArrayOf(lDay, lMonth, lYear, lLeap)
    }
}
