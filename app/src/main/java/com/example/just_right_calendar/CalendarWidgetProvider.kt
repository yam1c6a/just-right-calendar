package com.example.just_right_calendar

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
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

    override fun onReceive(context: Context, intent: android.content.Intent) {
        Log.d(TAG, "onReceive start action=${intent.action}")
        try {
            super.onReceive(context, intent)
        } catch (e: Exception) {
            Log.e(TAG, "onReceive error", e)
            return
        }
        Log.d(TAG, "onReceive end action=${intent.action}")
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
            val remoteViews = buildRemoteViews(context)
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

    private fun buildRemoteViews(context: Context): RemoteViews? {
        Log.d(TAG, "buildRemoteViews start")
        val packageName = context.packageName
        val numberIds = resolveIds(context, NUMBER_ID_PREFIX, NUMBER_ID_SUFFIX)
        val markIds = resolveIds(context, MARK_ID_PREFIX, MARK_ID_SUFFIX)

        if (numberIds.isEmpty() || markIds.isEmpty()) {
            Log.e(TAG, "Day view ids could not be resolved")
            return null
        }

        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val displayMonth = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }

        val daysInMonth = displayMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        val startOffset = ((displayMonth.get(Calendar.DAY_OF_WEEK) + 5) % 7)

        val views = RemoteViews(packageName, R.layout.widget_calendar)
        val monthLabel = SimpleDateFormat("yyyy/MM", Locale.getDefault()).format(displayMonth.time)
        views.setTextViewText(R.id.widgetMonthLabel, monthLabel)

        repeat(42) { index ->
            val numberId = numberIds.getOrNull(index) ?: return@repeat
            val markId = markIds.getOrNull(index) ?: return@repeat

            val dayNumber = index - startOffset + 1
            if (dayNumber in 1..daysInMonth) {
                views.setTextViewText(numberId, dayNumber.toString())
                views.setTextViewText(markId, "")

                if (dayNumber == currentDay) {
                    views.setTextColor(numberId, context.getColor(R.color.widget_text_primary))
                }
            } else {
                views.setTextViewText(numberId, "")
                views.setTextViewText(markId, "")
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

    companion object {
        private const val TAG = "CalendarWidgetProvider"
        private const val NUMBER_ID_PREFIX = "day"
        private const val NUMBER_ID_SUFFIX = "Number"
        private const val MARK_ID_PREFIX = "day"
        private const val MARK_ID_SUFFIX = "Mark"
    }
}
