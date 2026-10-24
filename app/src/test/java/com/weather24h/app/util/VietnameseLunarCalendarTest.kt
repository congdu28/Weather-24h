package com.weather24h.app.util

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class VietnameseLunarCalendarTest {

    @Test
    fun testTetNguyenDan2024() {
        val lunar = VietnameseLunarCalendar.getLunarDate(10, 2, 2024)
        assertEquals(1, lunar.day)
        assertEquals(1, lunar.month)
        assertEquals(2024, lunar.year)
        assertEquals("Giáp", lunar.canYear)
        assertEquals("Thìn", lunar.chiYear)
        assertFalse(lunar.isLeapMonth)
        assertTrue(lunar.festival?.contains("Tết Nguyên Đán") == true)
        assertEquals("1/1 ÂL", lunar.formattedLunarShort)
    }

    @Test
    fun testTetNguyenDan2025() {
        val lunar = VietnameseLunarCalendar.getLunarDate(29, 1, 2025)
        assertEquals(1, lunar.day)
        assertEquals(1, lunar.month)
        assertEquals(2025, lunar.year)
        assertEquals("Ất", lunar.canYear)
        assertEquals("Tỵ", lunar.chiYear)
        assertFalse(lunar.isLeapMonth)
        assertTrue(lunar.festival?.contains("Tết Nguyên Đán") == true)
    }

    @Test
    fun testTetNguyenDan2026() {
        val lunar = VietnameseLunarCalendar.getLunarDate(17, 2, 2026)
        assertEquals(1, lunar.day)
        assertEquals(1, lunar.month)
        assertEquals(2026, lunar.year)
        assertEquals("Bính", lunar.canYear)
        assertEquals("Ngọ", lunar.chiYear)
        assertFalse(lunar.isLeapMonth)
        assertTrue(lunar.festival?.contains("Tết Nguyên Đán") == true)
    }

    @Test
    fun testGiaoThua30vs29MonthLength() {
        // Năm Quý Mão (cuối 2023 ÂL): tháng Chạp đủ 30 ngày -> 29 Chạp (08/02/2024) KHÔNG phải Giao Thừa, 30 Chạp (09/02/2024) mới là Giao Thừa
        val day29Chap2023 = VietnameseLunarCalendar.getLunarDate(8, 2, 2024)
        val day30Chap2023 = VietnameseLunarCalendar.getLunarDate(9, 2, 2024)
        assertEquals(29, day29Chap2023.day)
        assertEquals(30, day29Chap2023.monthLength)
        assertFalse(day29Chap2023.festival?.contains("Giao Thừa") == true)
        assertEquals(30, day30Chap2023.day)
        assertTrue(day30Chap2023.festival?.contains("Giao Thừa") == true)

        // Năm Giáp Thìn (cuối 2024 ÂL): tháng Chạp thiếu 29 ngày -> 29 Chạp (28/01/2025) chính là Giao Thừa
        val day29Chap2024 = VietnameseLunarCalendar.getLunarDate(28, 1, 2025)
        assertEquals(29, day29Chap2024.day)
        assertEquals(29, day29Chap2024.monthLength)
        assertTrue(day29Chap2024.festival?.contains("Giao Thừa") == true)
    }

    @Test
    fun testLeapMonth2023And2025() {
        // Năm 2023 Quý Mão nhuận tháng 2, năm 2025 Ất Tỵ nhuận tháng 6
        assertEquals(2, VietnameseLunarCalendar.getLeapMonth(2023))
        assertEquals(0, VietnameseLunarCalendar.getLeapMonth(2024))
        assertEquals(6, VietnameseLunarCalendar.getLeapMonth(2025))
    }

    @Test
    fun testRoundTripSolarLunarConversion() {
        var date = LocalDate.of(2023, 1, 1)
        val end = LocalDate.of(2027, 12, 31)
        while (!date.isAfter(end)) {
            val l = VietnameseLunarCalendar.getLunarDate(date)
            val back = VietnameseLunarCalendar.convertLunarToSolar(l.day, l.month, l.year, l.isLeapMonth)
            assertEquals("Round-trip failed at $date ($l)", date, back)
            date = date.plusDays(1)
        }
    }

    @Test
    fun testMidAutumnFestival2024() {
        val lunar = VietnameseLunarCalendar.getLunarDate(17, 9, 2024)
        assertEquals(15, lunar.day)
        assertEquals(8, lunar.month)
        assertEquals("Giáp Thìn", lunar.yearCanChi)
        assertTrue(lunar.festival?.contains("Tết Trung Thu") == true)
    }

    @Test
    fun testAlmanac2025Jan1Reference() {
        // 01/01/2025 Dương lịch = 02/12/2024 ÂL, ngày Canh Ngọ, Trực Chấp, Sao Sâm, giờ Tý là Đại An
        val almanac = LunarAlmanac.getDayAlmanac(LocalDate.of(2025, 1, 1))
        assertEquals("Canh Ngọ", almanac.lunar.dayCanChi)
        assertEquals("Chấp", almanac.truc.name)
        assertEquals("Sâm", almanac.mansion.name)
        assertEquals("Lộ Bàng Thổ", almanac.napAm)
        assertEquals(CompassDirection.NORTH_WEST, almanac.hyThan)
        assertEquals(CompassDirection.SOUTH_WEST, almanac.taiThan)
        assertEquals("Đại An", almanac.hours.first().lyThuanPhong.name)
    }

    @Test
    fun testPersonalAndGoodDayFinder() {
        val profile = LunarAlmanac.getPersonalProfile(1996) // Bính Tý - Giản Hạ Thủy
        assertEquals("Bính Tý", profile.canChi)
        assertEquals("Giản Hạ Thủy", profile.napAm)
        assertEquals(FiveElement.THUY, profile.element)

        val profile2002 = LunarAlmanac.getPersonalProfile(2002) // Nhâm Ngọ - Dương Liễu Mộc
        assertEquals("Nhâm Ngọ", profile2002.canChi)
        assertEquals("Dương Liễu Mộc", profile2002.napAm)
        assertEquals(FiveElement.MOC, profile2002.element)
        assertEquals(FiveElement.THUY, profile2002.canElement)
        assertEquals(FiveElement.HOA, profile2002.chiElement)
        assertTrue(profile2002.isYang)
        assertEquals("Dương (Nhâm Thủy)", profile2002.yinYangLabel)

        val goodDays = LunarAlmanac.findGoodDays(
            activity = AlmanacActivity.OPENING,
            from = LocalDate.of(2026, 10, 24),
            days = 60,
            profile = profile
        )
        assertTrue(goodDays.isNotEmpty())
        goodDays.forEach {
            assertEquals(ActivityVerdict.GOOD, it.almanac.verdictOf(AlmanacActivity.OPENING))
            assertNotEquals(ActivityVerdict.BAD, it.personal?.verdict)
        }
    }

    @Test
    fun testAuspiciousHoursAndSolarTerm() {
        val lunar = VietnameseLunarCalendar.getLunarDate(23, 10, 2026)
        assertEquals(6, lunar.auspiciousHours.size)
        assertTrue(lunar.solarTerm.isNotEmpty())
        assertTrue(lunar.auspiciousDayType.contains("Hoàng đạo") || lunar.auspiciousDayType.contains("Hắc đạo"))
    }

    @Test
    fun testIsoDateParsing() {
        val lunar = VietnameseLunarCalendar.getLunarDateFromIso("2024-02-10")
        assertEquals(1, lunar.day)
        assertEquals(1, lunar.month)
        assertEquals(2024, lunar.year)
    }

    @Test
    fun testMoonEventCountdown() {
        val (desc1, days1) = VietnameseLunarCalendar.getNextMoonEventCountdown(10, 30)
        assertEquals(5, days1)
        assertTrue(desc1.contains("Rằm"))

        val (desc2, days2) = VietnameseLunarCalendar.getNextMoonEventCountdown(25, 29)
        assertEquals(5, days2)
        assertTrue(desc2.contains("Mùng Một"))
    }

    @Test
    fun testGetMonthDays() {
        val days = VietnameseLunarCalendar.getMonthDays(2026, 10)
        assertEquals(31, days.size)
        assertEquals(1, days.first().solarDay)
        assertEquals(31, days.last().solarDay)
    }

    @Test
    fun testApproachingFestivalEvent() {
        val eventEve = VietnameseLunarCalendar.getApproachingFestivalEvent(LocalDate.of(2024, 2, 9))
        assertNotNull(eventEve)
        assertTrue(eventEve!!.first.contains("Giao Thừa") || eventEve.first.contains("Tất Niên"))

        val eventTet = VietnameseLunarCalendar.getApproachingFestivalEvent(LocalDate.of(2024, 2, 10))
        assertNotNull(eventTet)
        assertTrue(eventTet!!.first.contains("Tết Nguyên Đán"))

        val eventRam = VietnameseLunarCalendar.getApproachingFestivalEvent(LocalDate.of(2024, 2, 23))
        assertNotNull(eventRam)
        assertTrue(eventRam!!.first.contains("Ngày mai") || eventRam.first.contains("Nguyên Tiêu"))

        val eventMidAutumn = VietnameseLunarCalendar.getApproachingFestivalEvent(LocalDate.of(2024, 9, 17))
        assertNotNull(eventMidAutumn)
        assertTrue(eventMidAutumn!!.first.contains("Trung Thu"))
    }
}
