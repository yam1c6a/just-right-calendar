package com.example.just_right_calendar

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth

object JapaneseHolidayCalculator {
    fun holidaysForMonth(yearMonth: YearMonth): Map<LocalDate, String> {
        val base = generateYearHolidays(yearMonth.year)
        return base.filterKeys { it.month == yearMonth.month }
    }

    private fun generateYearHolidays(year: Int): Map<LocalDate, String> {
        val holidays = mutableMapOf<LocalDate, String>()

        holidays[LocalDate.of(year, Month.JANUARY, 1)] = "元日"
        holidays[nthWeekdayOfMonth(year, Month.JANUARY, DayOfWeek.MONDAY, 2)] = "成人の日"
        holidays[LocalDate.of(year, Month.FEBRUARY, 11)] = "建国記念の日"
        holidays[LocalDate.of(year, Month.FEBRUARY, 23)] = "天皇誕生日"
        holidays[LocalDate.of(year, Month.APRIL, 29)] = "昭和の日"
        holidays[LocalDate.of(year, Month.MAY, 3)] = "憲法記念日"
        holidays[LocalDate.of(year, Month.MAY, 4)] = "みどりの日"
        holidays[LocalDate.of(year, Month.MAY, 5)] = "こどもの日"
        holidays[nthWeekdayOfMonth(year, Month.JULY, DayOfWeek.MONDAY, 3)] = "海の日"
        holidays[LocalDate.of(year, Month.AUGUST, 11)] = "山の日"
        holidays[nthWeekdayOfMonth(year, Month.SEPTEMBER, DayOfWeek.MONDAY, 3)] = "敬老の日"
        holidays[LocalDate.of(year, Month.OCTOBER, 1).with(java.time.temporal.TemporalAdjusters.dayOfWeekInMonth(2, DayOfWeek.MONDAY))] = "スポーツの日"
        holidays[LocalDate.of(year, Month.NOVEMBER, 3)] = "文化の日"
        holidays[LocalDate.of(year, Month.NOVEMBER, 23)] = "勤労感謝の日"

        val vernal = calculateVernalEquinox(year)
        val autumnal = calculateAutumnalEquinox(year)
        holidays[vernal] = "春分の日"
        holidays[autumnal] = "秋分の日"

        addCitizensHoliday(holidays)
        addSubstituteHolidays(holidays)

        return holidays
    }

    private fun addSubstituteHolidays(holidays: MutableMap<LocalDate, String>) {
        val holidayDates = holidays.keys.sorted()
        for (date in holidayDates) {
            if (date.dayOfWeek == DayOfWeek.SUNDAY) {
                var substitute = date.plusDays(1)
                while (holidays.containsKey(substitute)) {
                    substitute = substitute.plusDays(1)
                }
                holidays[substitute] = "振替休日"
            }
        }
    }

    private fun addCitizensHoliday(holidays: MutableMap<LocalDate, String>) {
        val candidates = holidays.keys.map { it.plusDays(1) }
        for (candidate in candidates) {
            val previous = candidate.minusDays(1)
            val next = candidate.plusDays(1)
            if (holidays.containsKey(previous) && holidays.containsKey(next) && !holidays.containsKey(candidate) && candidate.dayOfWeek != DayOfWeek.SUNDAY) {
                holidays[candidate] = "国民の祝日"
            }
        }
    }

    private fun nthWeekdayOfMonth(year: Int, month: Month, dayOfWeek: DayOfWeek, nth: Int): LocalDate {
        var date = LocalDate.of(year, month, 1)
        while (date.dayOfWeek != dayOfWeek) {
            date = date.plusDays(1)
        }
        return date.plusWeeks((nth - 1).toLong())
    }

    private fun calculateVernalEquinox(year: Int): LocalDate {
        val day = (20.8431 + 0.242194 * (year - 1980) - kotlin.math.floor((year - 1980) / 4.0)).toInt()
        return LocalDate.of(year, Month.MARCH, day)
    }

    private fun calculateAutumnalEquinox(year: Int): LocalDate {
        val day = (23.2488 + 0.242194 * (year - 1980) - kotlin.math.floor((year - 1980) / 4.0)).toInt()
        return LocalDate.of(year, Month.SEPTEMBER, day)
    }
}
