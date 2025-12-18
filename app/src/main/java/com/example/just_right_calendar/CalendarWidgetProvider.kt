package com.example.just_right_calendar

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        Log.d(TAG, "onUpdate start ids=${appWidgetIds.joinToString()}")
        appWidgetIds.forEach { id ->
            try {
                updateWidget(context, appWidgetManager, id)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update widget id=$id", e)
            }
        }
        Log.d(TAG, "onUpdate end")
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle,
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        Log.d(TAG, "onAppWidgetOptionsChanged id=$appWidgetId options=$newOptions")
        try {
            updateWidget(context, appWidgetManager, appWidgetId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update widget on options change", e)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
    ) {
        Log.d(TAG, "updateWidget start id=$appWidgetId")
        try {
            val monthOffset = loadMonthOffset(context, appWidgetId)
            val remoteViews = buildRemoteViews(context, appWidgetId, monthOffset)
            if (remoteViews != null) {
                appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
                Log.d(TAG, "updateWidget success id=$appWidgetId")
            } else {
                Log.e(TAG, "RemoteViews could not be built; skipping update for id=$appWidgetId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during updateAppWidget id=$appWidgetId", e)
        }
        Log.d(TAG, "updateWidget end id=$appWidgetId")
    }

    private fun buildRemoteViews(context: Context, appWidgetId: Int, monthOffset: Int): RemoteViews? {
        Log.d(TAG, "buildRemoteViews start")
        CalendarRepository.initialize(context.applicationContext)
        val packageName = context.packageName
        val numberIds = resolveIds(context, NUMBER_ID_PREFIX, NUMBER_ID_SUFFIX)
        val markIds = resolveIds(context, MARK_ID_PREFIX, MARK_ID_SUFFIX)
        val topAreaIds = resolveIds(context, TOP_ID_PREFIX, TOP_ID_SUFFIX)
        val bottomAreaIds = resolveIds(context, BOTTOM_ID_PREFIX, BOTTOM_ID_SUFFIX)

        if (numberIds.isEmpty() || markIds.isEmpty() || topAreaIds.isEmpty() || bottomAreaIds.isEmpty()) {
            Log.e(TAG, "Day view ids could not be resolved")
            return null
        }

        val calendar = Calendar.getInstance().apply { add(Calendar.MONTH, monthOffset) }
        val displayMonth = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
        val yearMonth = YearMonth.of(displayMonth.get(Calendar.YEAR), displayMonth.get(Calendar.MONTH) + 1)
        val holidays = JapaneseHolidayCalculator.holidaysForMonth(yearMonth).keys

        val daysInMonth = displayMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        val startOffset = ((displayMonth.get(Calendar.DAY_OF_WEEK) + 5) % 7)

        val views = RemoteViews(packageName, R.layout.widget_calendar)
        val monthLabel = SimpleDateFormat("yyyy/MM", Locale.getDefault()).format(displayMonth.time)
        views.setTextViewText(R.id.widgetMonthLabel, monthLabel)

        val launchIntent = Intent(context, MainActivity::class.java)
        val launchPendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        views.setOnClickPendingIntent(R.id.widgetRoot, launchPendingIntent)

        repeat(42) { index ->
            val numberId = numberIds.getOrNull(index) ?: return@repeat
            val markId = markIds.getOrNull(index) ?: return@repeat
            val topAreaId = topAreaIds.getOrNull(index) ?: return@repeat
            val bottomAreaId = bottomAreaIds.getOrNull(index) ?: return@repeat

            val dayNumber = index - startOffset + 1
            if (dayNumber in 1..daysInMonth) {
                val date = yearMonth.atDay(dayNumber)
                val dayOfWeek = date.dayOfWeek
                val isUserHoliday = CalendarRepository.isUserHoliday(date)
                val isHoliday = isUserHoliday || holidays.contains(date)
                val isToday = date == LocalDate.now()
                val topColor = when {
                    isHoliday -> R.color.calendar_holiday_bg
                    dayOfWeek == DayOfWeek.SUNDAY -> R.color.calendar_holiday_bg
                    dayOfWeek == DayOfWeek.SATURDAY -> R.color.calendar_saturday_bg
                    else -> R.color.widget_day_bg
                }
                val bottomColor = if (isToday) {
                    R.color.widget_today_bg
                } else {
                    R.color.widget_day_bg
                }
                val textColor = when {
                    isHoliday || dayOfWeek == DayOfWeek.SUNDAY -> R.color.calendar_sunday_text
                    else -> R.color.widget_text_primary
                }
                val marksText = CalendarRepository.getMarks(date).joinToString("") { it.symbol }

                views.setTextViewText(numberId, dayNumber.toString())
                views.setTextColor(numberId, context.getColor(textColor))
                views.setTextViewText(markId, marksText)
                views.setTextColor(markId, context.getColor(R.color.calendar_mark_text))
                views.setInt(topAreaId, "setBackgroundColor", context.getColor(topColor))
                views.setInt(bottomAreaId, "setBackgroundColor", context.getColor(bottomColor))
            } else {
                views.setTextViewText(numberId, "")
                views.setTextViewText(markId, "")
                views.setInt(topAreaId, "setBackgroundColor", context.getColor(R.color.widget_day_bg))
                views.setInt(bottomAreaId, "setBackgroundColor", context.getColor(R.color.widget_day_bg))
            }
        }

        Log.d(TAG, "buildRemoteViews end")
        return views
    }

    private fun resolveIds(context: Context, prefix: String, suffix: String): List<Int> {
        val packageName = context.packageName
        return (1..42).mapNotNull { index ->
            val id = context.resources.getIdentifier("$prefix$index$suffix", "id", packageName)
            if (id == 0) {
                Log.w(TAG, "Resource id not found for $prefix$index$suffix")
                null
            } else {
                id
            }
        }
    }

    private fun handleMonthChange(context: Context, intent: Intent) {
        val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            Log.w(TAG, "Received month change without widget id")
            return
        }
        val delta = when (intent.action) {
            ACTION_PREVIOUS_MONTH -> -1
            ACTION_NEXT_MONTH -> 1
            else -> 0
        }
        val manager = AppWidgetManager.getInstance(context)
        val currentOffset = loadMonthOffset(context, widgetId)
        val newOffset = currentOffset + delta
        saveMonthOffset(context, widgetId, newOffset)
        updateWidget(context, manager, widgetId)
    }

    private fun loadMonthOffset(context: Context, appWidgetId: Int): Int {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return preferences.getInt(monthOffsetKey(appWidgetId), 0)
    }

    private fun saveMonthOffset(context: Context, appWidgetId: Int, offset: Int) {
        val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        preferences.edit().putInt(monthOffsetKey(appWidgetId), offset).apply()
    }

    private fun monthOffsetKey(appWidgetId: Int): String = "${KEY_MONTH_OFFSET}_$appWidgetId"

    companion object {
        private const val TAG = "CalendarWidgetProvider"
        private const val NUMBER_ID_PREFIX = "day"
        private const val NUMBER_ID_SUFFIX = "Number"
        private const val MARK_ID_PREFIX = "day"
        private const val MARK_ID_SUFFIX = "Mark"
        private const val TOP_ID_PREFIX = "day"
        private const val TOP_ID_SUFFIX = "TopArea"
        private const val BOTTOM_ID_PREFIX = "day"
        private const val BOTTOM_ID_SUFFIX = "BottomArea"
        private const val PREFS_NAME = "calendar_widget_state"
        private const val KEY_MONTH_OFFSET = "month_offset"
        private const val ACTION_PREVIOUS_MONTH = "com.example.just_right_calendar.action.PREV_MONTH"
        private const val ACTION_NEXT_MONTH = "com.example.just_right_calendar.action.NEXT_MONTH"
    }
}
