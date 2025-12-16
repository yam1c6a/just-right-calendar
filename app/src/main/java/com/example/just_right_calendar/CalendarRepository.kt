package com.example.just_right_calendar

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate

object CalendarRepository {
    private const val PREF_NAME = "calendar_prefs"
    private const val MARK_KEY_PREFIX = "marks_"

    private lateinit var prefs: SharedPreferences

    fun initialize(context: Context) {
        if (!::prefs.isInitialized) {
            prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }

    fun getMarks(date: LocalDate): Set<MarkType> {
        val raw = prefs.getString(marksKey(date), null) ?: return emptySet()
        return raw.split(',')
            .mapNotNull { MarkType.fromString(it.trim()) }
            .toSet()
    }

    fun saveMarks(date: LocalDate, marks: Set<MarkType>) {
        if (!::prefs.isInitialized) throw IllegalStateException("CalendarRepository is not initialized")
        val editor = prefs.edit()
        if (marks.isEmpty()) {
            editor.remove(marksKey(date))
        } else {
            editor.putString(marksKey(date), marks.joinToString(",") { it.name })
        }
        editor.apply()
    }

    private fun marksKey(date: LocalDate): String = "$MARK_KEY_PREFIX${date}"
}
