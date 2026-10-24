package com.weather24h.app.util

import java.time.LocalDate

/**
 * ==========================================================================================
 *  LỊCH VẠN NIÊN – Bộ máy tra cứu ngày tốt xấu (100% ngoại tuyến)
 * ------------------------------------------------------------------------------------------
 *  Tổng hợp theo các phép lịch truyền thống phổ biến tại Việt Nam (Ngọc Hạp Thông Thư,
 *  Hiệp Kỷ Biện Phương): 12 Trực, Nhị thập bát tú, Ngũ hành nạp âm, hướng xuất hành
 *  (Hỷ Thần, Tài Thần, Hạc Thần), giờ Hoàng đạo, giờ xuất hành Lý Thuần Phong, các ngày kỵ
 *  (Tam Nương, Nguyệt Kỵ, Dương Công Kỵ Nhật, Sát Chủ, Thọ Tử, Vãng Vong) và cát tinh
 *  (Thiên Đức, Nguyệt Đức, Thiên Hỷ).
 *  Thông tin mang tính tham khảo theo văn hoá truyền thống.
 * ==========================================================================================
 */

/** Các việc thường được xem ngày */
enum class AlmanacActivity(val label: String, val emoji: String) {
    WEDDING("Cưới hỏi", "💍"),
    OPENING("Khai trương", "🏪"),
    CONSTRUCTION("Động thổ, xây dựng", "🏗️"),
    MOVING("Nhập trạch, chuyển nhà", "🏠"),
    TRAVEL("Xuất hành, đi xa", "🧳"),
    CONTRACT("Ký kết, giao dịch", "🤝"),
    WEALTH("Cầu tài, cầu lộc", "💰"),
    BUY("Mua xe, tài sản lớn", "🚗"),
    STUDY("Nhập học, thi cử", "🎓"),
    WORSHIP("Cúng lễ, cầu phúc", "🙏"),
    MEDICAL("Khám chữa bệnh", "💊"),
    REPAIR("Sửa nhà, tu tạo", "🔨"),
    BURIAL("An táng, cải táng", "🕯️")
}

enum class ActivityVerdict { GOOD, NEUTRAL, BAD }

enum class DayLevel(val label: String, val stars: Int) {
    EXCELLENT("Rất tốt", 5),
    GOOD("Tốt", 4),
    NORMAL("Bình thường", 3),
    BAD("Xấu", 2),
    VERY_BAD("Rất xấu", 1)
}

enum class FiveElement(val label: String, val emoji: String) {
    KIM("Kim", "⚪"), THUY("Thủy", "💧"), MOC("Mộc", "🌳"), HOA("Hỏa", "🔥"), THO("Thổ", "🟤");

    /** this sinh other */
    fun generates(other: FiveElement): Boolean = (ordinal + 1) % 5 == other.ordinal
    /** this khắc other */
    fun overcomes(other: FiveElement): Boolean = (ordinal + 2) % 5 == other.ordinal
}

enum class CompassDirection(val label: String, val angle: Float) {
    NORTH("Chính Bắc", 0f),
    NORTH_EAST("Đông Bắc", 45f),
    EAST("Chính Đông", 90f),
    SOUTH_EAST("Đông Nam", 135f),
    SOUTH("Chính Nam", 180f),
    SOUTH_WEST("Tây Nam", 225f),
    WEST("Chính Tây", 270f),
    NORTH_WEST("Tây Bắc", 315f)
}

data class TrucInfo(
    val name: String,
    val score: Int,
    val goodText: String,
    val badText: String,
    val good: Set<AlmanacActivity>,
    val bad: Set<AlmanacActivity>
) {
    val fullName: String get() = "Trực $name"
}

data class MansionInfo(
    val name: String,
    val fullName: String,
    val isGood: Boolean,
    val goodText: String,
    val badText: String,
    val good: Set<AlmanacActivity>,
    val bad: Set<AlmanacActivity>
)

/** Cát tinh hoặc ngày kỵ */
data class SpecialDay(
    val name: String,
    val isGood: Boolean,
    val description: String,
    val affects: Set<AlmanacActivity>,
    val weight: Int
)

data class LyThuanPhongInfo(
    val name: String,
    val isGood: Boolean,
    val description: String
)

data class HourInfo(
    val chiIndex: Int,
    val chi: String,
    val range: String,
    val startHour: Int,
    val isHoangDao: Boolean,
    val lyThuanPhong: LyThuanPhongInfo
) {
    /** Giờ thật sự tốt: vừa Hoàng đạo vừa thuộc giờ xuất hành tốt */
    val isBest: Boolean get() = isHoangDao && lyThuanPhong.isGood

    fun containsHour(hour: Int): Boolean =
        if (chiIndex == 0) hour == 23 || hour == 0 else hour == startHour || hour == startHour + 1
}

data class DayAlmanac(
    val date: LocalDate,
    val lunar: LunarDate,
    val truc: TrucInfo,
    val mansion: MansionInfo,
    val napAm: String,
    val napAmElement: FiveElement,
    val goodStars: List<SpecialDay>,
    val badDays: List<SpecialDay>,
    val score: Int,
    val level: DayLevel,
    val activityPoints: Map<AlmanacActivity, Int>,
    val hyThan: CompassDirection,
    val taiThan: CompassDirection,
    /** null = Hạc Thần đang ở trên trời, không cản hướng nào */
    val hacThan: CompassDirection?,
    val hours: List<HourInfo>,
    val clashChi: Int,
    val harmChi: Int,
    val punishChi: List<Int>,
    val harmonyChi: List<Int>,
    val summary: String
) {
    fun verdictOf(activity: AlmanacActivity): ActivityVerdict {
        val p = activityPoints[activity] ?: 0
        return when {
            p >= 2 -> ActivityVerdict.GOOD
            p <= -1 -> ActivityVerdict.BAD
            else -> ActivityVerdict.NEUTRAL
        }
    }

    val goodActivities: List<AlmanacActivity>
        get() = AlmanacActivity.values().filter { verdictOf(it) == ActivityVerdict.GOOD }
            .sortedByDescending { activityPoints[it] ?: 0 }

    val badActivities: List<AlmanacActivity>
        get() = AlmanacActivity.values().filter { verdictOf(it) == ActivityVerdict.BAD }
            .sortedBy { activityPoints[it] ?: 0 }

    val bestHours: List<HourInfo> get() = hours.filter { it.isBest }

    val clashAgesText: String
        get() {
            val jd = if (lunar.julianDay > 0) lunar.julianDay else VietnameseLunarCalendar.jdFromLocalDate(date)
            val canIdx = (jd + 9) % 10
            val c1 = "${VietnameseLunarCalendar.CAN[(canIdx + 4) % 10]} ${VietnameseLunarCalendar.CHI[clashChi]}"
            val c2 = "${VietnameseLunarCalendar.CAN[(canIdx + 6) % 10]} ${VietnameseLunarCalendar.CHI[clashChi]}"
            return "Tuổi ${VietnameseLunarCalendar.CHI[clashChi]} ($c1, $c2)"
        }

    val isYangDay: Boolean
        get() {
            val jd = if (lunar.julianDay > 0) lunar.julianDay else VietnameseLunarCalendar.jdFromLocalDate(date)
            return ((jd + 9) % 10) % 2 == 0
        }

    val yinYangLabel: String get() = if (isYangDay) "Ngày Dương" else "Ngày Âm"
}

/** Thông tin cá nhân theo năm sinh Âm lịch */
data class PersonalProfile(
    val birthYear: Int,
    val canIndex: Int,
    val chiIndex: Int,
    val napAm: String,
    val element: FiveElement
) {
    val canChi: String get() = "${VietnameseLunarCalendar.CAN[canIndex]} ${VietnameseLunarCalendar.CHI[chiIndex]}"
    val zodiacAnimal: String get() = LunarAlmanac.ZODIAC_ANIMALS[chiIndex]
    val isYang: Boolean get() = canIndex % 2 == 0
    val yinYangLabel: String get() = if (isYang) "Dương (${VietnameseLunarCalendar.CAN[canIndex]} ${LunarAlmanac.elementOfCan(canIndex).label})" else "Âm (${VietnameseLunarCalendar.CAN[canIndex]} ${LunarAlmanac.elementOfCan(canIndex).label})"
    val canElement: FiveElement get() = LunarAlmanac.elementOfCan(canIndex)
    val chiElement: FiveElement get() = LunarAlmanac.elementOfChi(chiIndex)

    /** Màu bản mệnh & màu tương sinh (hợp) */
    val luckyColors: String
        get() = when (element) {
            FiveElement.KIM -> "Trắng, xám, bạc (bản mệnh) • Vàng, nâu đất (tương sinh)"
            FiveElement.THUY -> "Đen, xanh dương (bản mệnh) • Trắng, xám, bạc (tương sinh)"
            FiveElement.MOC -> "Xanh lá (bản mệnh) • Đen, xanh dương (tương sinh)"
            FiveElement.HOA -> "Đỏ, hồng, tím (bản mệnh) • Xanh lá (tương sinh)"
            FiveElement.THO -> "Vàng, nâu đất (bản mệnh) • Đỏ, hồng, tím (tương sinh)"
        }

    /** Màu khắc mệnh nên hạn chế */
    val unluckyColors: String
        get() = when (element) {
            FiveElement.KIM -> "Đỏ, hồng, tím"
            FiveElement.THUY -> "Vàng, nâu đất"
            FiveElement.MOC -> "Trắng, xám, bạc"
            FiveElement.HOA -> "Đen, xanh dương"
            FiveElement.THO -> "Xanh lá"
        }
}

data class PersonalDayMatch(
    val verdict: ActivityVerdict,
    val points: Int,
    val notes: List<String>
) {
    val label: String
        get() = when (verdict) {
            ActivityVerdict.GOOD -> "Hợp tuổi"
            ActivityVerdict.NEUTRAL -> "Bình hòa"
            ActivityVerdict.BAD -> "Xung tuổi"
        }
}

data class GoodDayResult(
    val almanac: DayAlmanac,
    val activity: AlmanacActivity,
    val points: Int,
    val rankScore: Int,
    val personal: PersonalDayMatch?
)

object LunarAlmanac {

    private val CAN = VietnameseLunarCalendar.CAN
    private val CHI = VietnameseLunarCalendar.CHI

    val ZODIAC_ANIMALS = listOf("Chuột", "Trâu", "Hổ", "Mèo", "Rồng", "Rắn", "Ngựa", "Dê", "Khỉ", "Gà", "Chó", "Lợn")

    // ------------------------------------------------------------------
    // 12 TRỰC (Kiến trừ thập nhị khách)
    // ------------------------------------------------------------------
    val TRUC_LIST: List<TrucInfo> = listOf(
        TrucInfo("Kiến", 0,
            "Xuất hành, cưới hỏi, cầu tài, nhập học, nhậm chức",
            "Động thổ, đào giếng, an táng, sửa kho",
            setOf(AlmanacActivity.TRAVEL, AlmanacActivity.WEDDING, AlmanacActivity.WEALTH, AlmanacActivity.STUDY, AlmanacActivity.CONTRACT),
            setOf(AlmanacActivity.CONSTRUCTION, AlmanacActivity.BURIAL, AlmanacActivity.REPAIR)),
        TrucInfo("Trừ", 1,
            "Cúng giải hạn, chữa bệnh, dọn dẹp, loại bỏ cái cũ",
            "Cưới hỏi, xuất hành, khai trương, ký kết, mua sắm lớn",
            setOf(AlmanacActivity.MEDICAL, AlmanacActivity.WORSHIP, AlmanacActivity.REPAIR),
            setOf(AlmanacActivity.WEDDING, AlmanacActivity.TRAVEL, AlmanacActivity.OPENING, AlmanacActivity.CONTRACT, AlmanacActivity.BUY)),
        TrucInfo("Mãn", 1,
            "Cầu tài, cầu phúc, cúng tế, khai trương, xuất hành, mua sắm",
            "Uống thuốc, chữa bệnh, an táng, kiện tụng, nhậm chức",
            setOf(AlmanacActivity.WEALTH, AlmanacActivity.WORSHIP, AlmanacActivity.OPENING, AlmanacActivity.TRAVEL, AlmanacActivity.BUY),
            setOf(AlmanacActivity.MEDICAL, AlmanacActivity.BURIAL)),
        TrucInfo("Bình", 0,
            "Việc thông thường, sửa sang nhà cửa, đường sá, cúng lễ, cưới hỏi",
            "Đào mương, kiện tụng, tranh chấp",
            setOf(AlmanacActivity.REPAIR, AlmanacActivity.WEDDING, AlmanacActivity.WORSHIP),
            emptySet()),
        TrucInfo("Định", 2,
            "Cưới hỏi, nhập trạch, khai trương, ký kết, cầu tài, mua bán, xây dựng",
            "Kiện tụng, đi xa, chữa bệnh",
            setOf(AlmanacActivity.WEDDING, AlmanacActivity.MOVING, AlmanacActivity.OPENING, AlmanacActivity.CONTRACT, AlmanacActivity.WEALTH, AlmanacActivity.BUY, AlmanacActivity.CONSTRUCTION),
            setOf(AlmanacActivity.TRAVEL, AlmanacActivity.MEDICAL)),
        TrucInfo("Chấp", 1,
            "Lập khế ước, giao dịch, động thổ, xây đắp, tu sửa, chữa bệnh",
            "Chuyển nhà, đi xa, khai trương, xuất tiền của",
            setOf(AlmanacActivity.CONSTRUCTION, AlmanacActivity.REPAIR, AlmanacActivity.CONTRACT, AlmanacActivity.MEDICAL),
            setOf(AlmanacActivity.MOVING, AlmanacActivity.TRAVEL, AlmanacActivity.OPENING, AlmanacActivity.BUY)),
        TrucInfo("Phá", -2,
            "Phá dỡ nhà cũ, chữa bệnh, trừ tà",
            "Cưới hỏi, khai trương, ký kết, chuyển nhà, xuất hành, xây dựng",
            setOf(AlmanacActivity.MEDICAL, AlmanacActivity.REPAIR),
            setOf(AlmanacActivity.WEDDING, AlmanacActivity.OPENING, AlmanacActivity.CONTRACT, AlmanacActivity.MOVING, AlmanacActivity.TRAVEL, AlmanacActivity.CONSTRUCTION, AlmanacActivity.BUY)),
        TrucInfo("Nguy", -2,
            "Cúng lễ, tĩnh dưỡng",
            "Xuất hành, leo cao, đi thuyền, cưới hỏi, khai trương, xây dựng",
            setOf(AlmanacActivity.WORSHIP),
            setOf(AlmanacActivity.TRAVEL, AlmanacActivity.WEDDING, AlmanacActivity.OPENING, AlmanacActivity.CONSTRUCTION, AlmanacActivity.MOVING, AlmanacActivity.BUY)),
        TrucInfo("Thành", 2,
            "Khai trương, cưới hỏi, nhập học, xây dựng, nhập trạch, ký kết, cầu tài",
            "Kiện tụng, tranh chấp",
            setOf(AlmanacActivity.WEDDING, AlmanacActivity.OPENING, AlmanacActivity.STUDY, AlmanacActivity.CONSTRUCTION, AlmanacActivity.MOVING, AlmanacActivity.CONTRACT, AlmanacActivity.TRAVEL, AlmanacActivity.WEALTH, AlmanacActivity.BUY, AlmanacActivity.WORSHIP),
            emptySet()),
        TrucInfo("Thu", 0,
            "Thu hoạch, thu nợ, nhập kho, mua hàng, cầu tài",
            "Khai trương, an táng, xuất hành đi xa",
            setOf(AlmanacActivity.WEALTH, AlmanacActivity.BUY),
            setOf(AlmanacActivity.OPENING, AlmanacActivity.BURIAL, AlmanacActivity.TRAVEL)),
        TrucInfo("Khai", 2,
            "Khai trương, nhập học, xuất hành, cưới hỏi, động thổ, nhập trạch, nhậm chức",
            "An táng, chôn cất",
            setOf(AlmanacActivity.OPENING, AlmanacActivity.STUDY, AlmanacActivity.TRAVEL, AlmanacActivity.WEDDING, AlmanacActivity.CONSTRUCTION, AlmanacActivity.MOVING, AlmanacActivity.CONTRACT, AlmanacActivity.WEALTH),
            setOf(AlmanacActivity.BURIAL)),
        TrucInfo("Bế", -1,
            "Đắp đê, lấp hố, xây tường, an táng",
            "Khai trương, xuất hành, cưới hỏi, chữa bệnh, nhậm chức, chuyển nhà",
            setOf(AlmanacActivity.BURIAL, AlmanacActivity.REPAIR),
            setOf(AlmanacActivity.OPENING, AlmanacActivity.TRAVEL, AlmanacActivity.WEDDING, AlmanacActivity.MEDICAL, AlmanacActivity.STUDY, AlmanacActivity.MOVING))
    )

    // ------------------------------------------------------------------
    // NHỊ THẬP BÁT TÚ (28 sao) – chu kỳ cố định theo số ngày Julius
    // Mốc: 01/01/2025 (JD 2460677) là sao Sâm  →  index = (JD + 11) mod 28
    // ------------------------------------------------------------------
    private fun m(name: String, full: String, good: Boolean, goodText: String, badText: String,
                  g: Set<AlmanacActivity>, b: Set<AlmanacActivity>) = MansionInfo(name, full, good, goodText, badText, g, b)

    private val W = AlmanacActivity.WEDDING
    private val O = AlmanacActivity.OPENING
    private val C = AlmanacActivity.CONSTRUCTION
    private val MV = AlmanacActivity.MOVING
    private val T = AlmanacActivity.TRAVEL
    private val K = AlmanacActivity.CONTRACT
    private val WE = AlmanacActivity.WEALTH
    private val BU = AlmanacActivity.BUY
    private val S = AlmanacActivity.STUDY
    private val WO = AlmanacActivity.WORSHIP
    private val ME = AlmanacActivity.MEDICAL
    private val R = AlmanacActivity.REPAIR
    private val BR = AlmanacActivity.BURIAL

    val MANSIONS: List<MansionInfo> = listOf(
        m("Giác", "Giác Mộc Giao", true, "Cưới hỏi, xây dựng, thi cử, khai trương, xuất hành", "An táng, sửa mộ", setOf(W, C, S, O, T), setOf(BR)),
        m("Cang", "Cang Kim Long", false, "Cắt may, trồng trọt", "Cưới hỏi, xây dựng, an táng, khai trương", emptySet(), setOf(W, C, BR, O, MV)),
        m("Đê", "Đê Thổ Lạc", false, "Không có việc lớn nào hợp", "Khởi công, cưới hỏi, đi thuyền, an táng, khai trương", emptySet(), setOf(C, W, T, BR, O)),
        m("Phòng", "Phòng Nhật Thố", true, "Khởi công, cưới hỏi, xuất hành, mua đất, an táng, nhập trạch", "Không kỵ việc lớn", setOf(C, W, T, BU, BR, MV, O), emptySet()),
        m("Tâm", "Tâm Nguyệt Hồ", false, "Không có việc lớn nào hợp", "Khởi công, cưới hỏi, kiện tụng, an táng, ký kết", emptySet(), setOf(C, W, BR, O, K)),
        m("Vĩ", "Vĩ Hỏa Hổ", true, "Khởi công, cưới hỏi, xây dựng, an táng, khai trương", "Đóng giường, may áo", setOf(C, W, BR, O, R), emptySet()),
        m("Cơ", "Cơ Thủy Báo", true, "Khởi tạo, xây dựng, an táng, khai trương, cưới hỏi", "Đóng giường, đi thuyền", setOf(C, BR, O, W, R), emptySet()),
        m("Đẩu", "Đẩu Mộc Giải", true, "Khởi tạo, xây dựng, cưới hỏi, khai trương, may mặc, xuất hành", "Không kỵ việc lớn", setOf(C, W, O, BR, R, T), emptySet()),
        m("Ngưu", "Ngưu Kim Ngưu", false, "Đi thuyền, cắt may", "Khởi công, cưới hỏi, xây dựng, khai trương, chuyển nhà", emptySet(), setOf(C, W, O, T, MV)),
        m("Nữ", "Nữ Thổ Bức", false, "Kết màn, may áo", "Cưới hỏi, khởi công, an táng, khai trương, kiện tụng", emptySet(), setOf(W, C, BR, O)),
        m("Hư", "Hư Nhật Thử", false, "Không có việc lớn nào hợp", "Khởi công, cưới hỏi, khai trương, chuyển nhà", emptySet(), setOf(C, W, O, MV)),
        m("Nguy", "Nguy Nguyệt Yến", false, "Lót giường, chặt cỏ", "Dựng nhà, cưới hỏi, an táng, đi thuyền", emptySet(), setOf(C, W, BR, T)),
        m("Thất", "Thất Hỏa Trư", true, "Khởi công, cưới hỏi, xây dựng, an táng, khai trương, nhập trạch", "Không kỵ việc lớn", setOf(C, W, BR, O, MV, R), emptySet()),
        m("Bích", "Bích Thủy Du", true, "Khởi tạo, cưới hỏi, xây dựng, khai trương, an táng, xuất hành", "Không kỵ việc lớn", setOf(C, W, O, BR, T, MV), emptySet()),
        m("Khuê", "Khuê Mộc Lang", false, "Nhập học, cắt may, may mặc", "Khai trương, an táng, động thổ", setOf(S), setOf(O, BR, C)),
        m("Lâu", "Lâu Kim Cẩu", true, "Khởi công, cưới hỏi, xây dựng, khai trương, nhập trạch, ký kết", "Đóng giường", setOf(C, W, O, MV, K), emptySet()),
        m("Vị", "Vị Thổ Trĩ", true, "Khởi công, cưới hỏi, xây dựng, an táng, khai trương", "Đi thuyền", setOf(C, W, BR, O), emptySet()),
        m("Mão", "Mão Nhật Kê", false, "Xây cất nhỏ", "Khởi công, cưới hỏi, an táng, khai trương, chuyển nhà", emptySet(), setOf(C, W, BR, O, MV)),
        m("Tất", "Tất Nguyệt Ô", true, "Khởi công, cưới hỏi, an táng, xây dựng, chăn nuôi, mua sắm", "Đi thuyền", setOf(C, W, BR, O, BU), emptySet()),
        m("Chủy", "Chủy Hỏa Hầu", false, "Không có việc lớn nào hợp", "Khởi công, an táng, cưới hỏi, ký kết", emptySet(), setOf(C, BR, W, K)),
        m("Sâm", "Sâm Thủy Viên", true, "Khởi công, xây dựng, nhập học, xuất hành, kinh doanh, thi cử", "Cưới hỏi, an táng, đóng giường", setOf(C, S, T, WE, O), setOf(W, BR)),
        m("Tỉnh", "Tỉnh Mộc Hãn", true, "Khởi công, xây dựng, nhập học, đào giếng, xuất hành", "An táng, may áo", setOf(C, S, T, R), setOf(BR)),
        m("Quỷ", "Quỷ Kim Dương", false, "An táng, chặt cỏ", "Khởi công, cưới hỏi, xây dựng, khai trương, chuyển nhà", setOf(BR), setOf(C, W, O, MV)),
        m("Liễu", "Liễu Thổ Chương", false, "Không có việc lớn nào hợp", "Khởi công, cưới hỏi, an táng, khai trương", emptySet(), setOf(C, W, BR, O)),
        m("Tinh", "Tinh Nhật Mã", false, "Xây phòng mới", "Cưới hỏi, khai trương", setOf(C), setOf(W, O)),
        m("Trương", "Trương Nguyệt Lộc", true, "Khởi công, cưới hỏi, xây dựng, khai trương, nhập học, an táng", "Sửa thuyền", setOf(C, W, O, S, BR, MV), emptySet()),
        m("Dực", "Dực Hỏa Xà", false, "Cắt áo", "Cưới hỏi, xây dựng, an táng, khai trương", emptySet(), setOf(W, C, BR, O)),
        m("Chẩn", "Chẩn Thủy Dẫn", true, "Khởi công, cưới hỏi, xây dựng, nhập học, xuất hành, mua sắm", "Đi thuyền", setOf(C, W, S, T, BU, BR), emptySet())
    )

    // ------------------------------------------------------------------
    // 60 HOA GIÁP – NGŨ HÀNH NẠP ÂM (mỗi tên dùng cho 2 can chi liên tiếp)
    // ------------------------------------------------------------------
    private val NAP_AM = listOf(
        "Hải Trung Kim", "Lô Trung Hỏa", "Đại Lâm Mộc", "Lộ Bàng Thổ", "Kiếm Phong Kim",
        "Sơn Đầu Hỏa", "Giản Hạ Thủy", "Thành Đầu Thổ", "Bạch Lạp Kim", "Dương Liễu Mộc",
        "Tuyền Trung Thủy", "Ốc Thượng Thổ", "Tích Lịch Hỏa", "Tùng Bách Mộc", "Trường Lưu Thủy",
        "Sa Trung Kim", "Sơn Hạ Hỏa", "Bình Địa Mộc", "Bích Thượng Thổ", "Kim Bạch Kim",
        "Phú Đăng Hỏa", "Thiên Hà Thủy", "Đại Trạch Thổ", "Thoa Xuyến Kim", "Tang Đố Mộc",
        "Đại Khê Thủy", "Sa Trung Thổ", "Thiên Thượng Hỏa", "Thạch Lựu Mộc", "Đại Hải Thủy"
    )

    /** Vị trí trong 60 hoa giáp (0 = Giáp Tý) */
    fun sexagenaryIndex(canIdx: Int, chiIdx: Int): Int = ((6 * canIdx - 5 * chiIdx) % 60 + 60) % 60

    fun napAm(canIdx: Int, chiIdx: Int): String = NAP_AM[sexagenaryIndex(canIdx, chiIdx) / 2]

    fun elementOfNapAm(napAm: String): FiveElement = when {
        napAm.endsWith("Kim") -> FiveElement.KIM
        napAm.endsWith("Thủy") -> FiveElement.THUY
        napAm.endsWith("Mộc") -> FiveElement.MOC
        napAm.endsWith("Hỏa") -> FiveElement.HOA
        else -> FiveElement.THO
    }

    /** Ngũ hành của Thiên Can */
    fun elementOfCan(canIdx: Int): FiveElement = when (canIdx) {
        0, 1 -> FiveElement.MOC
        2, 3 -> FiveElement.HOA
        4, 5 -> FiveElement.THO
        6, 7 -> FiveElement.KIM
        else -> FiveElement.THUY
    }

    /** Ngũ hành của Địa Chi (Tý-Hợi Thủy, Dần-Mão Mộc, Tỵ-Ngọ Hỏa, Thân-Dậu Kim, Thìn-Tuất-Sửu-Mùi Thổ) */
    fun elementOfChi(chiIdx: Int): FiveElement = when (chiIdx) {
        2, 3 -> FiveElement.MOC
        5, 6 -> FiveElement.HOA
        8, 9 -> FiveElement.KIM
        11, 0 -> FiveElement.THUY
        else -> FiveElement.THO
    }

    /** Thiên Can Ngũ Hợp (Giáp-Kỷ, Ất-Canh, Bính-Tân, Đinh-Nhâm, Mậu-Quý) */
    fun isCanHarmony(a: Int, b: Int): Boolean = (a + 5) % 10 == b

    /** Thiên Can Tương Xung (Giáp-Canh, Ất-Tân, Bính-Nhâm, Đinh-Quý) */
    fun isCanClash(a: Int, b: Int): Boolean =
        (a % 2 == b % 2) && (((a + 6) % 10 == b) || ((b + 6) % 10 == a)) && (a % 5 != 2)

    // ------------------------------------------------------------------
    // HƯỚNG XUẤT HÀNH
    // ------------------------------------------------------------------
    fun hyThanDirection(canIdx: Int): CompassDirection = when (canIdx % 5) {
        0 -> CompassDirection.NORTH_EAST // Giáp, Kỷ
        1 -> CompassDirection.NORTH_WEST // Ất, Canh
        2 -> CompassDirection.SOUTH_WEST // Bính, Tân
        3 -> CompassDirection.SOUTH      // Đinh, Nhâm
        else -> CompassDirection.SOUTH_EAST // Mậu, Quý
    }

    fun taiThanDirection(canIdx: Int): CompassDirection = when (canIdx) {
        0, 1 -> CompassDirection.SOUTH_EAST
        2, 3 -> CompassDirection.EAST
        4 -> CompassDirection.NORTH
        5 -> CompassDirection.SOUTH
        6, 7 -> CompassDirection.SOUTH_WEST
        8 -> CompassDirection.WEST
        else -> CompassDirection.NORTH_WEST
    }

    /**
     * Hạc Thần (hung thần, nên tránh xuất hành về hướng này) – di chuyển theo chu kỳ 60 hoa giáp:
     * Kỷ Dậu→Giáp Dần: Đông Bắc • Ất Mão→Kỷ Mùi: Đông • Canh Thân→Ất Sửu: Đông Nam • Bính Dần→Canh Ngọ: Nam
     * Tân Mùi→Bính Tý: Tây Nam • Đinh Sửu→Tân Tỵ: Tây • Nhâm Ngọ→Đinh Hợi: Tây Bắc • Mậu Tý→Nhâm Thìn: Bắc
     * Quý Tỵ→Mậu Thân (16 ngày): Hạc Thần lên trời, không cản hướng nào.
     */
    fun hacThanDirection(canIdx: Int, chiIdx: Int): CompassDirection? = when (sexagenaryIndex(canIdx, chiIdx)) {
        in 45..50 -> CompassDirection.NORTH_EAST
        in 51..55 -> CompassDirection.EAST
        in 56..59, 0, 1 -> CompassDirection.SOUTH_EAST
        in 2..6 -> CompassDirection.SOUTH
        in 7..12 -> CompassDirection.SOUTH_WEST
        in 13..17 -> CompassDirection.WEST
        in 18..23 -> CompassDirection.NORTH_WEST
        in 24..28 -> CompassDirection.NORTH
        else -> null
    }

    // ------------------------------------------------------------------
    // GIỜ XUẤT HÀNH LÝ THUẦN PHONG
    // (Ngày Âm + Tháng Âm + Khắc giờ − 2) mod 6, với Khắc giờ = (hourChiIdx mod 6) + 1
    // (Tý/Ngọ = 1, Sửu/Mùi = 2, Dần/Thân = 3, Mão/Dậu = 4, Thìn/Tuất = 5, Tỵ/Hợi = 6)
    // ------------------------------------------------------------------
    val LY_THUAN_PHONG = listOf(
        LyThuanPhongInfo("Đại An", true, "Mọi việc tốt lành, cầu tài đi hướng Tây Nam, nhà cửa yên ổn, người xuất hành bình an."),
        LyThuanPhongInfo("Lưu Niên", false, "Việc khó thành, cầu tài mờ mịt, kiện cáo nên hoãn, người đi chưa có tin về."),
        LyThuanPhongInfo("Tốc Hỷ", true, "Tin vui sắp tới, cầu tài đi hướng Nam, gặp gỡ đối tác nhiều may mắn, người đi có tin về."),
        LyThuanPhongInfo("Xích Khẩu", false, "Dễ cãi cọ, thị phi, đề phòng hao tài; người đi nên hoãn lại, tránh tranh luận."),
        LyThuanPhongInfo("Tiểu Cát", true, "Rất tốt lành, buôn bán có lời, có tin mừng, người đi sắp về, mọi việc hòa hợp."),
        LyThuanPhongInfo("Không Vong", false, "Cầu tài không lợi, hay bị trái ý, ra đi dễ gặp trở ngại, nên cẩn trọng.")
    )

    fun lyThuanPhong(lunarDay: Int, lunarMonth: Int, hourChiIdx: Int): LyThuanPhongInfo {
        val khac = (hourChiIdx % 6) + 1
        return LY_THUAN_PHONG[((lunarDay + lunarMonth + khac - 3) % 6 + 6) % 6]
    }

    // ------------------------------------------------------------------
    // QUAN HỆ ĐỊA CHI
    // ------------------------------------------------------------------
    fun isClash(a: Int, b: Int) = (a - b + 12) % 12 == 6
    fun isHarm(a: Int, b: Int) = (a + b) % 12 == 7
    fun isSixHarmony(a: Int, b: Int) = (a + b) % 12 == 1
    fun isTripleHarmony(a: Int, b: Int) = a != b && a % 4 == b % 4

    private val PUNISH_GROUPS = listOf(setOf(0, 3), setOf(2, 5, 8), setOf(1, 10, 7))
    private val SELF_PUNISH = setOf(4, 6, 9, 11)
    private val BREAK_PAIRS = setOf(0 to 9, 9 to 0, 3 to 6, 6 to 3, 1 to 4, 4 to 1, 7 to 10, 10 to 7)

    fun isPunish(a: Int, b: Int): Boolean {
        if (a == b) return a in SELF_PUNISH
        return PUNISH_GROUPS.any { a in it && b in it }
    }

    /** Lục Phá Địa Chi (Tý-Dậu, Mão-Ngọ, Sửu-Thìn, Mùi-Tuất) */
    fun isBreak(a: Int, b: Int): Boolean = (a to b) in BREAK_PAIRS

    // ------------------------------------------------------------------
    // CÁT TINH & NGÀY KỴ (tính theo tháng Âm lịch)
    // ------------------------------------------------------------------
    private val ALL_MAJOR = AlmanacActivity.values().filter {
        it != AlmanacActivity.WORSHIP && it != AlmanacActivity.MEDICAL
    }.toSet()

    // Thiên Đức: T1 Đinh, T2 Thân, T3 Nhâm, T4 Tân, T5 Hợi, T6 Giáp, T7 Quý, T8 Dần, T9 Bính, T10 Ất, T11 Tỵ, T12 Canh
    private val THIEN_DUC = listOf("Đinh", "Thân", "Nhâm", "Tân", "Hợi", "Giáp", "Quý", "Dần", "Bính", "Ất", "Tỵ", "Canh")
    // Nguyệt Đức: tháng 1,5,9 Bính • 2,6,10 Giáp • 3,7,11 Nhâm • 4,8,12 Canh
    private val NGUYET_DUC = listOf("Bính", "Giáp", "Nhâm", "Canh")
    // Sát Chủ: T1 Tỵ, T2 Tý, T3 Mùi, T4 Mão, T5 Thân, T6 Tuất, T7 Hợi, T8 Sửu, T9 Ngọ, T10 Dậu, T11 Dần, T12 Thìn
    private val SAT_CHU = listOf(5, 0, 7, 3, 8, 10, 11, 1, 6, 9, 2, 4)
    // Thọ Tử: T1 Tuất, T2 Thìn, T3 Hợi, T4 Tỵ, T5 Tý, T6 Ngọ, T7 Sửu, T8 Mùi, T9 Dần, T10 Thân, T11 Mão, T12 Dậu
    private val THO_TU = listOf(10, 4, 11, 5, 0, 6, 1, 7, 2, 8, 3, 9)
    // Vãng Vong: T1 Dần, T2 Tỵ, T3 Thân, T4 Hợi, T5 Mão, T6 Ngọ, T7 Dậu, T8 Tý, T9 Thìn, T10 Mùi, T11 Tuất, T12 Sửu
    private val VANG_VONG = listOf(2, 5, 8, 11, 3, 6, 9, 0, 4, 7, 10, 1)
    // Dương Công Kỵ Nhật (13 ngày/năm)
    private val DUONG_CONG = mapOf(
        1 to listOf(13), 2 to listOf(11), 3 to listOf(9), 4 to listOf(7), 5 to listOf(5), 6 to listOf(3),
        7 to listOf(8, 29), 8 to listOf(27), 9 to listOf(25), 10 to listOf(23), 11 to listOf(21), 12 to listOf(19)
    )
    private val TAM_NUONG = setOf(3, 7, 13, 18, 22, 27)
    private val NGUYET_KY = setOf(5, 14, 23)

    private fun specialDays(lunar: LunarDate, canIdx: Int, chiIdx: Int): Pair<List<SpecialDay>, List<SpecialDay>> {
        val mIdx = (lunar.month - 1).coerceIn(0, 11)
        val good = mutableListOf<SpecialDay>()
        val bad = mutableListOf<SpecialDay>()

        val td = THIEN_DUC[mIdx]
        if (td == CAN[canIdx] || td == CHI[chiIdx]) {
            good.add(SpecialDay("Thiên Đức", true, "Cát tinh lớn, hóa giải hung sát, tốt cho mọi việc.", AlmanacActivity.values().toSet(), 6))
        }
        if (NGUYET_DUC[mIdx % 4] == CAN[canIdx]) {
            good.add(SpecialDay("Nguyệt Đức", true, "Cát tinh, trăm việc đều tốt, đặc biệt cầu phúc, xây dựng.", AlmanacActivity.values().toSet(), 6))
        }
        if ((mIdx + 10) % 12 == chiIdx) { // Thiên Hỷ = Chi tháng + 8 (tháng Giêng: Tuất)
            good.add(SpecialDay("Thiên Hỷ", true, "Sao vui mừng, tốt cho cưới hỏi, khai trương, giao dịch.",
                setOf(AlmanacActivity.WEDDING, AlmanacActivity.OPENING, AlmanacActivity.CONTRACT), 4))
        }

        if (lunar.day in TAM_NUONG) {
            bad.add(SpecialDay("Tam Nương", false, "Ngày Tam Nương: kỵ cưới hỏi, khai trương, xuất hành, động thổ, khởi sự việc lớn.",
                setOf(AlmanacActivity.WEDDING, AlmanacActivity.OPENING, AlmanacActivity.TRAVEL, AlmanacActivity.CONSTRUCTION, AlmanacActivity.MOVING, AlmanacActivity.CONTRACT), 10))
        }
        if (lunar.day in NGUYET_KY) {
            bad.add(SpecialDay("Nguyệt Kỵ", false, "\"Mùng năm, mười bốn, hai ba – Đi chơi cũng thiệt nữa là đi buôn\": kỵ xuất hành, khai trương, cưới hỏi.",
                setOf(AlmanacActivity.TRAVEL, AlmanacActivity.OPENING, AlmanacActivity.WEDDING, AlmanacActivity.MOVING, AlmanacActivity.CONTRACT, AlmanacActivity.WEALTH), 8))
        }
        if (!lunar.isLeapMonth && DUONG_CONG[lunar.month]?.contains(lunar.day) == true) {
            bad.add(SpecialDay("Dương Công Kỵ Nhật", false, "Một trong 13 ngày đại kỵ trong năm, trăm việc nên tránh.", ALL_MAJOR, 15))
        }
        if (SAT_CHU[mIdx] == chiIdx) {
            bad.add(SpecialDay("Sát Chủ", false, "Kỵ xây dựng, nhập trạch, cưới hỏi, an táng, khai trương.",
                setOf(AlmanacActivity.CONSTRUCTION, AlmanacActivity.MOVING, AlmanacActivity.WEDDING, AlmanacActivity.BURIAL, AlmanacActivity.OPENING), 10))
        }
        if (THO_TU[mIdx] == chiIdx) {
            bad.add(SpecialDay("Thọ Tử", false, "Ngày Thọ Tử: trăm sự đều kỵ, không nên khởi sự việc lớn.", ALL_MAJOR, 12))
        }
        if (VANG_VONG[mIdx] == chiIdx) {
            bad.add(SpecialDay("Vãng Vong", false, "Kỵ xuất hành, cưới hỏi, nhập trạch, khai trương, cầu tài.",
                setOf(AlmanacActivity.TRAVEL, AlmanacActivity.WEDDING, AlmanacActivity.MOVING, AlmanacActivity.OPENING, AlmanacActivity.WEALTH), 8))
        }
        return Pair(good, bad)
    }

    // ------------------------------------------------------------------
    // TÍNH TOÁN TỔNG HỢP CHO MỘT NGÀY
    // ------------------------------------------------------------------
    fun getDayAlmanac(date: LocalDate): DayAlmanac = getDayAlmanac(date, VietnameseLunarCalendar.getLunarDate(date))

    fun getDayAlmanac(date: LocalDate, lunar: LunarDate): DayAlmanac {
        val jd = if (lunar.julianDay > 0) lunar.julianDay else VietnameseLunarCalendar.jdFromLocalDate(date)
        val canIdx = (jd + 9) % 10
        val chiIdx = (jd + 1) % 12

        // 12 Trực: ngày mang Chi của tháng Âm lịch (Giêng=Dần..Chạp=Sửu) là ngày Trực Kiến
        val monthChi = (lunar.month + 1) % 12
        val truc = TRUC_LIST[(chiIdx - monthChi + 12) % 12]

        // 28 sao
        val mansion = MANSIONS[(jd + 11) % 28]

        // Nạp âm ngày
        val napAm = napAm(canIdx, chiIdx)
        val napAmElement = elementOfNapAm(napAm)

        val (goodStars, badDays) = specialDays(lunar, canIdx, chiIdx)

        // Điểm ngày tổng quát (0..100)
        var score = 50
        score += if (lunar.isAuspiciousDay) 12 else -8
        score += truc.score * 7
        score += if (mansion.isGood) 8 else -8
        goodStars.forEach { score += it.weight }
        badDays.forEach { score -= it.weight }
        score = score.coerceIn(0, 100)

        val level = when {
            score >= 75 -> DayLevel.EXCELLENT
            score >= 62 -> DayLevel.GOOD
            score >= 45 -> DayLevel.NORMAL
            score >= 30 -> DayLevel.BAD
            else -> DayLevel.VERY_BAD
        }

        // Điểm từng việc
        val points = AlmanacActivity.values().associateWith { act ->
            var p = 0
            if (act in truc.good) p += 2
            if (act in truc.bad) p -= 2
            if (act in mansion.good) p += 1
            if (act in mansion.bad) p -= 1
            p += if (lunar.isAuspiciousDay) 1 else -1
            goodStars.forEach { if (act in it.affects) p += 1 }
            badDays.forEach { if (act in it.affects) p -= 3 }
            p
        }

        // Giờ trong ngày
        val hdHours = VietnameseLunarCalendar.auspiciousHourIndices(chiIdx).toSet()
        val hours = (0 until 12).map { h ->
            HourInfo(
                chiIndex = h,
                chi = CHI[h],
                range = VietnameseLunarCalendar.HOUR_RANGES[h],
                startHour = if (h == 0) 23 else h * 2 - 1,
                isHoangDao = h in hdHours,
                lyThuanPhong = lyThuanPhong(lunar.day, lunar.month, h)
            )
        }

        val harmony = (0 until 12).filter { isSixHarmony(it, chiIdx) || isTripleHarmony(it, chiIdx) }
        val punish = (0 until 12).filter { it != chiIdx && isPunish(it, chiIdx) } +
            if (chiIdx in SELF_PUNISH) listOf(chiIdx) else emptyList()

        val almanacNoSummary = DayAlmanac(
            date = date,
            lunar = lunar,
            truc = truc,
            mansion = mansion,
            napAm = napAm,
            napAmElement = napAmElement,
            goodStars = goodStars,
            badDays = badDays,
            score = score,
            level = level,
            activityPoints = points,
            hyThan = hyThanDirection(canIdx),
            taiThan = taiThanDirection(canIdx),
            hacThan = hacThanDirection(canIdx, chiIdx),
            hours = hours,
            clashChi = (chiIdx + 6) % 12,
            harmChi = (19 - chiIdx) % 12,
            punishChi = punish,
            harmonyChi = harmony,
            summary = ""
        )
        return almanacNoSummary.copy(summary = buildSummary(almanacNoSummary))
    }

    private fun buildSummary(a: DayAlmanac): String {
        val sb = StringBuilder()
        val hd = a.lunar.auspiciousDayType.substringBefore(" (")
        sb.append(if (a.lunar.isAuspiciousDay) "Ngày Hoàng đạo ($hd)" else "Ngày Hắc đạo ($hd)")
        sb.append(", ${a.truc.fullName}, sao ${a.mansion.name} ${if (a.mansion.isGood) "tốt" else "xấu"}")
        if (a.badDays.isNotEmpty()) sb.append(", phạm ${a.badDays.joinToString(", ") { it.name }}")
        if (a.goodStars.isNotEmpty()) sb.append(", có ${a.goodStars.joinToString(", ") { it.name }}")
        sb.append(". ")
        val good = a.goodActivities.take(3)
        when (a.level) {
            DayLevel.EXCELLENT, DayLevel.GOOD -> {
                sb.append("Ngày thuận lợi")
                if (good.isNotEmpty()) sb.append(" cho ${good.joinToString(", ") { it.label.lowercase() }}")
                sb.append(".")
            }
            DayLevel.NORMAL -> {
                sb.append("Ngày bình thường, phù hợp việc thường nhật")
                if (good.isNotEmpty()) sb.append("; có thể ${good.joinToString(", ") { it.label.lowercase() }}")
                sb.append(".")
            }
            else -> sb.append("Nên hạn chế khởi sự việc lớn, ưu tiên việc nhỏ và cúng lễ cầu an.")
        }
        return sb.toString()
    }

    // ------------------------------------------------------------------
    // CÁ NHÂN HOÁ THEO NĂM SINH
    // ------------------------------------------------------------------
    fun getPersonalProfile(birthYear: Int): PersonalProfile {
        val canIdx = ((birthYear + 6) % 10 + 10) % 10
        val chiIdx = ((birthYear + 8) % 12 + 12) % 12
        val napAm = napAm(canIdx, chiIdx)
        return PersonalProfile(birthYear, canIdx, chiIdx, napAm, elementOfNapAm(napAm))
    }

    fun matchPersonalDay(profile: PersonalProfile, almanac: DayAlmanac): PersonalDayMatch {
        val jd = almanac.lunar.julianDay
        val dayCan = (jd + 9) % 10
        val dayChi = (jd + 1) % 12
        val notes = mutableListOf<String>()
        var points = 0
        val userCan = profile.canIndex
        val userChi = profile.chiIndex
        val animal = "tuổi ${CHI[userChi]} (${profile.canChi})"

        // 1. Quan hệ Địa Chi (Lục Xung, Lục Hợp, Tam Hợp, Tương Hình, Tương Hại, Lục Phá)
        if (isClash(userChi, dayChi)) {
            val canClash = elementOfCan(dayCan).overcomes(elementOfCan(userCan)) ||
                elementOfCan(userCan).overcomes(elementOfCan(dayCan))
            if (canClash) {
                points -= 5
                notes.add("⚠️ Thiên khắc địa xung: Ngày ${CAN[dayCan]} ${CHI[dayChi]} khắc trực diện $animal – đại kỵ khởi sự lớn")
            } else {
                points -= 3
                notes.add("⚠️ Địa chi lục xung: Ngày ${CHI[dayChi]} xung với tuổi ${CHI[userChi]}")
            }
        }
        if (isSixHarmony(userChi, dayChi)) {
            points += 2
            notes.add("✅ Địa chi lục hợp: Ngày ${CHI[dayChi]} nhị hợp với tuổi ${CHI[userChi]} (cát lợi)")
        }
        if (isTripleHarmony(userChi, dayChi)) {
            points += 2
            notes.add("✅ Địa chi tam hợp: Ngày ${CHI[dayChi]} tam hợp với tuổi ${CHI[userChi]} (quý nhân trợ giúp)")
        }
        if (isHarm(userChi, dayChi)) {
            points -= 1
            notes.add("⚠️ Địa chi tương hại: Ngày ${CHI[dayChi]} lục hại với tuổi ${CHI[userChi]}")
        }
        if (isPunish(userChi, dayChi)) {
            points -= 1
            notes.add("⚠️ Địa chi tương hình: Ngày ${CHI[dayChi]} phạm hình với tuổi ${CHI[userChi]}")
        }
        if (isBreak(userChi, dayChi)) {
            points -= 1
            notes.add("⚠️ Địa chi tương phá: Ngày ${CHI[dayChi]} lục phá với tuổi ${CHI[userChi]}")
        }

        // 2. Quan hệ Thiên Can (Ngũ Hợp, Tương Xung, Tương Sinh / Tương Khắc)
        val dayCanEl = elementOfCan(dayCan)
        val userCanEl = elementOfCan(userCan)
        when {
            isCanHarmony(dayCan, userCan) -> {
                points += 2
                notes.add("✅ Thiên can ngũ hợp: Can ngày ${CAN[dayCan]} hợp với can tuổi ${CAN[userCan]}")
            }
            isCanClash(dayCan, userCan) -> {
                points -= 1
                notes.add("⚠️ Thiên can tương xung: Can ngày ${CAN[dayCan]} (${dayCanEl.label}) xung can tuổi ${CAN[userCan]} (${userCanEl.label})")
            }
            dayCanEl.generates(userCanEl) -> {
                points += 1
                notes.add("✅ Thiên can tương sinh: Can ngày ${CAN[dayCan]} (${dayCanEl.label}) sinh can tuổi ${CAN[userCan]} (${userCanEl.label})")
            }
            dayCanEl.overcomes(userCanEl) && !isClash(userChi, dayChi) -> {
                points -= 1
                notes.add("⚠️ Thiên can ngày ${CAN[dayCan]} (${dayCanEl.label}) khắc can tuổi ${CAN[userCan]} (${userCanEl.label})")
            }
        }

        // 3. Quan hệ Ngũ Hành Nạp Âm & Âm Dương
        val dayEl = almanac.napAmElement
        val userEl = profile.element
        when {
            dayEl.generates(userEl) -> {
                points += 2
                notes.add("✅ Ngũ hành nạp âm: Ngày ${almanac.napAm} (${dayEl.label}) tương sinh bản mệnh ${profile.napAm} (${userEl.label}) – đại cát")
            }
            dayEl == userEl -> {
                points += 1
                notes.add("✅ Ngũ hành nạp âm: Ngày ${almanac.napAm} tỷ hòa cùng hành ${userEl.label} với bản mệnh – thuận lợi")
            }
            userEl.overcomes(dayEl) -> {
                points += 1
                notes.add("✅ Ngũ hành nạp âm: Bản mệnh ${userEl.label} chế khắc được hành ngày (${dayEl.label}) – chủ động, đắc tài")
            }
            dayEl.overcomes(userEl) -> {
                points -= 2
                notes.add("⚠️ Ngũ hành nạp âm: Ngày ${almanac.napAm} (${dayEl.label}) khắc bản mệnh ${profile.napAm} (${userEl.label}) – nên thận trọng")
            }
            else -> {
                notes.add("ℹ️ Ngũ hành nạp âm: Bản mệnh ${userEl.label} sinh xuất cho hành ngày (${dayEl.label}) – bình hòa, hao sức nhẹ")
            }
        }

        // 4. Cân bằng Âm Dương
        if (profile.isYang != almanac.isYangDay) {
            notes.add("☯️ Âm Dương giao hòa: Tuổi ${if (profile.isYang) "Dương" else "Âm"} gặp ${almanac.yinYangLabel} – cân bằng tốt")
        }

        val verdict = when {
            isClash(userChi, dayChi) -> ActivityVerdict.BAD
            points >= 2 -> ActivityVerdict.GOOD
            points <= -2 -> ActivityVerdict.BAD
            else -> ActivityVerdict.NEUTRAL
        }
        return PersonalDayMatch(verdict, points, notes)
    }

    // ------------------------------------------------------------------
    // TÌM NGÀY TỐT CHO MỘT VIỆC
    // ------------------------------------------------------------------
    fun findGoodDays(
        activity: AlmanacActivity,
        from: LocalDate = LocalDate.now(),
        days: Int = 60,
        profile: PersonalProfile? = null,
        limit: Int = 30
    ): List<GoodDayResult> {
        val results = mutableListOf<GoodDayResult>()
        for (i in 0 until days) {
            val date = from.plusDays(i.toLong())
            val almanac = getDayAlmanac(date)
            if (almanac.verdictOf(activity) != ActivityVerdict.GOOD) continue
            if (almanac.badDays.isNotEmpty() || almanac.score < 55) continue
            val personal = profile?.let { matchPersonalDay(it, almanac) }
            if (personal?.verdict == ActivityVerdict.BAD) continue
            val p = almanac.activityPoints[activity] ?: 0
            val rank = almanac.score + p * 6 + (personal?.points ?: 0) * 4
            results.add(GoodDayResult(almanac, activity, p, rank, personal))
        }
        return results.sortedWith(compareByDescending<GoodDayResult> { it.rankScore }.thenBy { it.almanac.date })
            .take(limit)
            .sortedBy { it.almanac.date }
    }

    /** Đánh giá ngắn gọn số sao (★★★★☆) */
    fun starsText(level: DayLevel): String = "★".repeat(level.stars) + "☆".repeat(5 - level.stars)
}
