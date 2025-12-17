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
import kotlin.math.abs

class CalendarWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_PREV_MONTH -> handleMonthChange(context, intent, -1)
            ACTION_NEXT_MONTH -> handleMonthChange(context, intent, 1)
            ACTION_TODAY -> handleMonthChange(context, intent, null)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            clearYearMonth(context, appWidgetId)
        }
    }

    private fun handleMonthChange(context: Context, intent: Intent, deltaMonths: Long?) {
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        val manager = AppWidgetManager.getInstance(context)
        val currentMonth = loadYearMonth(context, appWidgetId)
        val updatedMonth = deltaMonths?.let { currentMonth.plusMonths(it) } ?: YearMonth.now()
        saveYearMonth(context, appWidgetId, updatedMonth)
        updateWidget(context, manager, appWidgetId)
    }

    companion object {
        private const val PREF_NAME = "calendar_widget_prefs"
        private const val PREF_MONTH_PREFIX = "widget_month_"

        const val ACTION_PREV_MONTH = "com.example.just_right_calendar.widget.PREV_MONTH"
        const val ACTION_NEXT_MONTH = "com.example.just_right_calendar.widget.NEXT_MONTH"
        const val ACTION_TODAY = "com.example.just_right_calendar.widget.TODAY"

        private const val REQUEST_PREV = 1
        private const val REQUEST_NEXT = 2
        private const val REQUEST_TODAY = 3

        fun requestUpdate(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, CalendarWidgetProvider::class.java))
            for (id in ids) {
                updateWidget(context, manager, id)
            }
        }

        fun loadYearMonth(context: Context, appWidgetId: Int): YearMonth {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val stored = prefs.getString("$PREF_MONTH_PREFIX$appWidgetId", null)
            return stored?.let { YearMonth.parse(it) } ?: YearMonth.now()
        }

        fun saveYearMonth(context: Context, appWidgetId: Int, yearMonth: YearMonth) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString("$PREF_MONTH_PREFIX$appWidgetId", yearMonth.toString()).apply()
        }

        fun clearYearMonth(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().remove("$PREF_MONTH_PREFIX$appWidgetId").apply()
        }

        private fun buildIdArray(context: Context, suffix: String = ""): IntArray {
            val resources = context.resources
            val packageName = context.packageName
            return IntArray(42) { index ->
                resources.getIdentifier("day${index + 1}$suffix", "id", packageName)
            }
        }

        private fun createActionPendingIntent(
            context: Context,
            appWidgetId: Int,
            action: String,
            requestCodeOffset: Int
        ): PendingIntent {
            val intent = Intent(context, CalendarWidgetProvider::class.java).apply {
                this.action = action
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            return PendingIntent.getBroadcast(
                context,
                appWidgetId * 10 + requestCodeOffset,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            CalendarRepository.initialize(context.applicationContext)

            val dayContainerIds = buildIdArray(context)
            val dayTopIds = buildIdArray(context, "Top")
            val dayBottomIds = buildIdArray(context, "Bottom")
            val dayNumberIds = buildIdArray(context, "Number")
            val dayMarkIds = buildIdArray(context, "Mark")

            val views = RemoteViews(context.packageName, R.layout.widget_calendar)
            val yearMonth = loadYearMonth(context, appWidgetId)
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

            views.setOnClickPendingIntent(
                R.id.widgetPrevButton,
                createActionPendingIntent(context, appWidgetId, ACTION_PREV_MONTH, REQUEST_PREV)
            )
            views.setOnClickPendingIntent(
                R.id.widgetNextButton,
                createActionPendingIntent(context, appWidgetId, ACTION_NEXT_MONTH, REQUEST_NEXT)
            )
            views.setOnClickPendingIntent(
                R.id.widgetTodayButton,
                createActionPendingIntent(context, appWidgetId, ACTION_TODAY, REQUEST_TODAY)
            )

            for (index in dayContainerIds.indices) {
                val dayNumber = index - leadingEmpty + 1
                val containerId = dayContainerIds[index]
                val topId = dayTopIds[index]
                val bottomId = dayBottomIds[index]
                val numberId = dayNumberIds[index]
                val markId = dayMarkIds[index]

                if (dayNumber in 1..yearMonth.lengthOfMonth()) {
                    val date = yearMonth.atDay(dayNumber)
                    views.setViewVisibility(containerId, View.VISIBLE)
                    views.setTextViewText(numberId, dayNumber.toString())

                    val isUserHoliday = CalendarRepository.isUserHoliday(date)
                    val isHoliday = isUserHoliday || holidays.containsKey(date) || date.dayOfWeek == DayOfWeek.SUNDAY
                    val isSaturday = date.dayOfWeek == DayOfWeek.SATURDAY
                    val topColor = when {
                        isHoliday -> ContextCompat.getColor(context, R.color.calendar_holiday_bg)
                        isSaturday -> ContextCompat.getColor(context, R.color.calendar_saturday_bg)
                        else -> ContextCompat.getColor(context, R.color.calendar_default_day_bg)
                    }
                    views.setInt(topId, "setBackgroundColor", topColor)

                    val bottomColor = if (date == today) {
                        ContextCompat.getColor(context, R.color.calendar_today_bg)
                    } else {
                        ContextCompat.getColor(context, R.color.calendar_default_day_bg)
                    }
                    views.setInt(bottomId, "setBackgroundColor", bottomColor)

                    val marks = CalendarRepository.getMarks(date)
                    val markSymbol = marks.firstOrNull()?.symbol.orEmpty()
                    views.setTextViewText(markId, markSymbol)
                    views.setViewVisibility(markId, if (markSymbol.isEmpty()) View.GONE else View.VISIBLE)
                    views.setTextColor(numberId, ContextCompat.getColor(context, R.color.text_primary))

                    val detailRequestCode = appWidgetId * 100000 + (abs(date.hashCode()) % 100000)
                    val detailIntent = Intent(context, DayDetailActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra(DayDetailActivity.EXTRA_DATE, date.toString())
                        putExtra("date", date.toString())
                    }
                    val pendingDetail = PendingIntent.getActivity(
                        context,
                        detailRequestCode,
                        detailIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(containerId, pendingDetail)
                } else {
                    views.setViewVisibility(containerId, View.INVISIBLE)
                    views.setTextViewText(numberId, "")
                    views.setTextViewText(markId, "")
                    views.setViewVisibility(markId, View.GONE)
                    views.setOnClickPendingIntent(containerId, openMainIntent)
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
