package com.yam1c6a.justrightcalendar

import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class JapaneseHolidayCalculatorTest {
    @Test
    fun `coming of age day 2026 is detected`() {
        val holidays = JapaneseHolidayCalculator.holidaysForMonth(YearMonth.of(2026, 1))
        assertTrue(holidays.containsKey(LocalDate.of(2026, 1, 12)))
    }
}
