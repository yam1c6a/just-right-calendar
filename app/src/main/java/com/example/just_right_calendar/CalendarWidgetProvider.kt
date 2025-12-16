package com.example.just_right_calendar

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
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

        private val markIds = intArrayOf(
            R.id.mark1, R.id.mark2, R.id.mark3, R.id.mark4, R.id.mark5, R.id.mark6, R.id.mark7,
            R.id.mark8, R.id.mark9, R.id.mark10, R.id.mark11, R.id.mark12, R.id.mark13, R.id.mark14,
            R.id.mark15, R.id.mark16, R.id.mark17, R.id.mark18, R.id.mark19, R.id.mark20, R.id.mark21,
            R.id.mark22, R.id.mark23, R.id.mark24, R.id.mark25, R.id.mark26, R.id.mark27, R.id.mark28,
            R.id.mark29, R.id.mark30, R.id.mark31, R.id.mark32, R.id.mark33, R.id.mark34, R.id.mark35,
            R.id.mark36, R.id.mark37, R.id.mark38, R.id.mark39, R.id.mark40, R.id.mark41, R.id.mark42
        )

        private val todayBarIds = intArrayOf(
            R.id.todayBar1, R.id.todayBar2, R.id.todayBar3, R.id.todayBar4, R.id.todayBar5, R.id.todayBar6, R.id.todayBar7,
            R.id.todayBar8, R.id.todayBar9, R.id.todayBar10, R.id.todayBar11, R.id.todayBar12, R.id.todayBar13, R.id.todayBar14,
            R.id.todayBar15, R.id.todayBar16, R.id.todayBar17, R.id.todayBar18, R.id.todayBar19, R.id.todayBar20, R.id.todayBar21,
            R.id.todayBar22, R.id.todayBar23, R.id.todayBar24, R.id.todayBar25, R.id.todayBar26, R.id.todayBar27, R.id.todayBar28,
            R.id.todayBar29, R.id.todayBar30, R.id.todayBar31, R.id.todayBar32, R.id.todayBar33, R.id.todayBar34, R.id.todayBar35,
            R.id.todayBar36, R.id.todayBar37, R.id.todayBar38, R.id.todayBar39, R.id.todayBar40, R.id.todayBar41, R.id.todayBar42
        )

        private val topAreaIds = intArrayOf(
            R.id.dayTopArea1, R.id.dayTopArea2, R.id.dayTopArea3, R.id.dayTopArea4, R.id.dayTopArea5, R.id.dayTopArea6, R.id.dayTopArea7,
            R.id.dayTopArea8, R.id.dayTopArea9, R.id.dayTopArea10, R.id.dayTopArea11, R.id.dayTopArea12, R.id.dayTopArea13, R.id.dayTopArea14,
            R.id.dayTopArea15, R.id.dayTopArea16, R.id.dayTopArea17, R.id.dayTopArea18, R.id.dayTopArea19, R.id.dayTopArea20, R.id.dayTopArea21,
            R.id.dayTopArea22, R.id.dayTopArea23, R.id.dayTopArea24, R.id.dayTopArea25, R.id.dayTopArea26, R.id.dayTopArea27, R.id.dayTopArea28,
            R.id.dayTopArea29, R.id.dayTopArea30, R.id.dayTopArea31, R.id.dayTopArea32, R.id.dayTopArea33, R.id.dayTopArea34, R.id.dayTopArea35,
            R.id.dayTopArea36, R.id.dayTopArea37, R.id.dayTopArea38, R.id.dayTopArea39, R.id.dayTopArea40, R.id.dayTopArea41, R.id.dayTopArea42
        )

        private val cellIds = intArrayOf(
            R.id.cell1, R.id.cell2, R.id.cell3, R.id.cell4, R.id.cell5, R.id.cell6, R.id.cell7,
            R.id.cell8, R.id.cell9, R.id.cell10, R.id.cell11, R.id.cell12, R.id.cell13, R.id.cell14,
            R.id.cell15, R.id.cell16, R.id.cell17, R.id.cell18, R.id.cell19, R.id.cell20, R.id.cell21,
            R.id.cell22, R.id.cell23, R.id.cell24, R.id.cell25, R.id.cell26, R.id.cell27, R.id.cell28,
            R.id.cell29, R.id.cell30, R.id.cell31, R.id.cell32, R.id.cell33, R.id.cell34, R.id.cell35,
            R.id.cell36, R.id.cell37, R.id.cell38, R.id.cell39, R.id.cell40, R.id.cell41, R.id.cell42
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
                val markViewId = markIds[index]
                val barViewId = todayBarIds[index]
                val topAreaId = topAreaIds[index]
                val cellId = cellIds[index]

                if (dayNumber in 1..yearMonth.lengthOfMonth()) {
                    val date = yearMonth.atDay(dayNumber)
                    views.setTextViewText(dayViewId, dayNumber.toString())

                    val isUserHoliday = CalendarRepository.isUserHoliday(date)
                    val isHoliday = isUserHoliday || holidays.containsKey(date) || date.dayOfWeek == DayOfWeek.SUNDAY
                    val isSaturday = date.dayOfWeek == DayOfWeek.SATURDAY
                    val topColor = when {
                        isHoliday -> ContextCompat.getColor(context, R.color.holiday_red)
                        isSaturday -> ContextCompat.getColor(context, R.color.saturday_blue)
                        else -> ContextCompat.getColor(context, R.color.white)
                    }
                    views.setInt(topAreaId, "setBackgroundColor", topColor)

                    val marks = CalendarRepository.getMarks(date)
                    val markSymbol = marks.firstOrNull()?.symbol.orEmpty()
                    views.setTextViewText(markViewId, markSymbol)
                    views.setViewVisibility(markViewId, if (markSymbol.isEmpty()) View.GONE else View.VISIBLE)

                    val showToday = date == today
                    views.setViewVisibility(barViewId, if (showToday) View.VISIBLE else View.GONE)

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
                    views.setOnClickPendingIntent(cellId, pendingDetail)
                } else {
                    views.setTextViewText(dayViewId, "")
                    views.setViewVisibility(markViewId, View.GONE)
                    views.setViewVisibility(barViewId, View.GONE)
                    views.setInt(topAreaId, "setBackgroundColor", ContextCompat.getColor(context, R.color.white))
                    views.setOnClickPendingIntent(cellId, openMainIntent)
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
