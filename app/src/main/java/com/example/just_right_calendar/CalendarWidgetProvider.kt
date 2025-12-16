package com.example.just_right_calendar

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class CalendarWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        private val dayIds = intArrayOf(
            R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6, R.id.day7,
            R.id.day8, R.id.day9, R.id.day10, R.id.day11, R.id.day12, R.id.day13, R.id.day14,
            R.id.day15, R.id.day16, R.id.day17, R.id.day18, R.id.day19, R.id.day20, R.id.day21,
            R.id.day22, R.id.day23, R.id.day24, R.id.day25, R.id.day26, R.id.day27, R.id.day28,
            R.id.day29, R.id.day30, R.id.day31, R.id.day32, R.id.day33, R.id.day34, R.id.day35,
            R.id.day36, R.id.day37, R.id.day38, R.id.day39, R.id.day40, R.id.day41, R.id.day42
        )

        fun requestUpdate(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, CalendarWidgetProvider::class.java))
            for (id in ids) {
                updateWidget(context, manager, id)
            }
        }

        private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            CalendarRepository.initialize(context.applicationContext)

            val views = RemoteViews(context.packageName, R.layout.widget_calendar)
            val yearMonth = YearMonth.now()
            val firstDayOfMonth = yearMonth.atDay(1)
            val leadingEmpty = (firstDayOfMonth.dayOfWeek.value + 6) % 7
            val today = LocalDate.now()

            views.setTextViewText(R.id.widgetMonthLabel, context.getString(R.string.month_format, yearMonth.year, yearMonth.monthValue))

            val holidays = JapaneseHolidayCalculator.holidaysForMonth(yearMonth)

            val openMainIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetRoot, openMainIntent)

            for (index in dayIds.indices) {
                val dayNumber = index - leadingEmpty + 1
                val dayViewId = dayIds[index]

                if (dayNumber in 1..yearMonth.lengthOfMonth()) {
                    val date = yearMonth.atDay(dayNumber)
                    views.setTextViewText(dayViewId, dayNumber.toString())

                    val isUserHoliday = CalendarRepository.isUserHoliday(date)
                    val isHoliday = isUserHoliday || holidays.containsKey(date) || date.dayOfWeek == DayOfWeek.SUNDAY
                    val isSaturday = date.dayOfWeek == DayOfWeek.SATURDAY
                    val textColor = when {
                        isHoliday -> ContextCompat.getColor(context, R.color.holiday_red)
                        isSaturday -> ContextCompat.getColor(context, R.color.saturday_blue)
                        else -> ContextCompat.getColor(context, R.color.text_primary)
                    }
                    views.setTextColor(dayViewId, textColor)

                    val backgroundColor = if (date == today) {
                        ContextCompat.getColor(context, R.color.today_green)
                    } else {
                        ContextCompat.getColor(context, R.color.white)
                    }
                    views.setInt(dayViewId, "setBackgroundColor", backgroundColor)

                    val detailIntent = Intent(context, DayDetailActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra(DayDetailActivity.EXTRA_DATE, date.toString())
                        putExtra("date", date.toString())
                    }
                    val pendingDetail = PendingIntent.getActivity(
                        context,
                        appWidgetId * 100 + index,
                        detailIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(dayViewId, pendingDetail)
                } else {
                    views.setTextViewText(dayViewId, "")
                    views.setTextColor(dayViewId, ContextCompat.getColor(context, R.color.text_primary))
                    views.setInt(dayViewId, "setBackgroundColor", ContextCompat.getColor(context, R.color.surface_background))
                    views.setOnClickPendingIntent(dayViewId, openMainIntent)
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
