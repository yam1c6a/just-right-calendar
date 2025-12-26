package com.yam1c6a.justrightcalendar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed; requesting widget update")
            val safeContext = context?.applicationContext ?: return
            CalendarWidgetProvider.requestWidgetUpdate(safeContext)
            CalendarWidgetProvider.scheduleMidnightUpdate(safeContext)
        }
    }

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }
}
