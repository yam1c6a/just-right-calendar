package com.example.just_right_calendar

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject
import java.time.LocalDate

object CalendarRepository {
    private const val PREF_NAME = "calendar_prefs"
    private const val MEMO_KEY = "memos"
    private const val CUSTOM_HOLIDAY_KEY = "custom_holidays"

    private lateinit var prefs: SharedPreferences

    fun initialize(context: Context) {
        if (!::prefs.isInitialized) {
            prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }
    }

    fun getMemo(date: LocalDate): String? {
        val map = readStringMap(MEMO_KEY)
        val value = map[date.toString()]
        return value?.takeIf { it.isNotBlank() }
    }

    fun saveMemo(date: LocalDate, memo: String?) {
        val map = readStringMap(MEMO_KEY)
        val key = date.toString()
        if (memo.isNullOrBlank()) {
            map.remove(key)
        } else {
            map[key] = memo
        }
        writeStringMap(MEMO_KEY, map)
    }

    fun getCustomHoliday(date: LocalDate): String? {
        val map = readStringMap(CUSTOM_HOLIDAY_KEY)
        return map[date.toString()]
    }

    fun setCustomHoliday(date: LocalDate, enabled: Boolean, name: String?) {
        val map = readStringMap(CUSTOM_HOLIDAY_KEY)
        val key = date.toString()
        if (enabled) {
            map[key] = name?.trim() ?: ""
        } else {
            map.remove(key)
        }
        writeStringMap(CUSTOM_HOLIDAY_KEY, map)
    }

    private fun readStringMap(key: String): MutableMap<String, String> {
        if (!::prefs.isInitialized) throw IllegalStateException("CalendarRepository is not initialized")
        val raw = prefs.getString(key, "{}") ?: "{}"
        val json = JSONObject(raw)
        val map = mutableMapOf<String, String>()
        json.keys().forEachRemaining { map[it] = json.optString(it) }
        return map
    }

    private fun writeStringMap(key: String, map: Map<String, String>) {
        if (!::prefs.isInitialized) throw IllegalStateException("CalendarRepository is not initialized")
        val json = JSONObject()
        map.forEach { (k, v) -> json.put(k, v) }
        prefs.edit().putString(key, json.toString()).apply()
    }
}
