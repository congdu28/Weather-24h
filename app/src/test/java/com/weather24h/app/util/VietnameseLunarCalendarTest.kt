package com.weather24h.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VietnameseLunarCalendarTest {

    @Test
    fun testTetNguyenDan2024() {
        // Ngày 10/02/2024 Dương lịch là Mùng 1 Tết Giáp Thìn (1/1/2024 ÂL)
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
        // Ngày 29/01/2025 Dương lịch là Mùng 1 Tết Ất Tỵ (1/1/2025 ÂL)
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
        // Ngày 17/02/2026 Dương lịch là Mùng 1 Tết Bính Ngọ (1/1/2026 ÂL)
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
    fun testMidAutumnFestival2024() {
        // Rằm tháng 8 năm Giáp Thìn là ngày 17/09/2024 Dương lịch
        val lunar = VietnameseLunarCalendar.getLunarDate(17, 9, 2024)
        assertEquals(15, lunar.day)
        assertEquals(8, lunar.month)
        assertEquals("Giáp Thìn", lunar.yearCanChi)
        assertTrue(lunar.festival?.contains("Tết Trung Thu") == true)
    }

    @Test
    fun testAuspiciousHoursAndSolarTerm() {
        val lunar = VietnameseLunarCalendar.getLunarDate(23, 10, 2026)
        assertEquals(6, lunar.auspiciousHours.size)
        assertNotNull(lunar.solarTerm)
        assertTrue(lunar.solarTerm.isNotEmpty())
        assertNotNull(lunar.auspiciousDayType)
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
        val (desc1, days1) = VietnameseLunarCalendar.getNextMoonEventCountdown(10)
        assertEquals(5, days1)
        assertTrue(desc1.contains("Rằm"))

        val (desc2, days2) = VietnameseLunarCalendar.getNextMoonEventCountdown(25)
        assertEquals(6, days2)
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
        // Ngày 09/02/2024 là ngày áp Tết (hôm sau 10/02/2024 là Mùng 1 Tết Nguyên Đán Giáp Thìn)
        val event = VietnameseLunarCalendar.getApproachingFestivalEvent(java.time.LocalDate.of(2024, 2, 9))
        assertNotNull(event)
        assertTrue(event!!.first.contains("Tết") || event.first.contains("Mùng Một"))
    }
}
