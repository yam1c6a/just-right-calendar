package com.example.just_right_calendar

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.YearMonth

class DayDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DATE = "selected_date"
    }

    private lateinit var date: LocalDate
    private lateinit var memoInput: EditText
    private lateinit var customHolidaySwitch: Switch
    private lateinit var customHolidayName: EditText
    private lateinit var dayTypeLabel: TextView
    private var originalMemo: String = ""
    private var originalCustomHolidayEnabled: Boolean = false
    private var originalCustomHolidayName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CalendarRepository.initialize(applicationContext)
        setContentView(R.layout.activity_day_detail)

        val dateString = intent.getStringExtra(EXTRA_DATE)
        if (dateString.isNullOrBlank()) {
            finish()
            return
        }
        date = LocalDate.parse(dateString)

        val dateLabel = findViewById<TextView>(R.id.dateLabel)
        memoInput = findViewById(R.id.memoInput)
        customHolidaySwitch = findViewById(R.id.customHolidaySwitch)
        customHolidayName = findViewById(R.id.customHolidayName)
        dayTypeLabel = findViewById(R.id.dayTypeLabel)
        val saveButton = findViewById<Button>(R.id.saveButton)

        dateLabel.text = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

        val memo = CalendarRepository.getMemo(date)
        originalMemo = memo ?: ""
        memoInput.setText(memo)

        val customHoliday = CalendarRepository.getCustomHoliday(date)
        originalCustomHolidayEnabled = customHoliday != null
        originalCustomHolidayName = customHoliday ?: ""
        if (customHoliday != null) {
            customHolidaySwitch.isChecked = true
            customHolidayName.isEnabled = true
            customHolidayName.setText(customHoliday)
        }

        updateDayTypeLabel()

        onBackPressedDispatcher.addCallback(this) {
            handleBackNavigation()
        }

        customHolidaySwitch.setOnCheckedChangeListener { _, isChecked ->
            customHolidayName.isEnabled = isChecked
            if (!isChecked) {
                customHolidayName.text?.clear()
            }
            updateDayTypeLabel()
        }

        saveButton.setOnClickListener {
            CalendarRepository.saveMemo(date, memoInput.text?.toString())
            CalendarRepository.setCustomHoliday(date, customHolidaySwitch.isChecked, customHolidayName.text?.toString())
            finish()
        }
    }

    private fun handleBackNavigation() {
        if (hasUnsavedChanges()) {
            AlertDialog.Builder(this)
                .setTitle(R.string.discard_changes_title)
                .setMessage(R.string.discard_changes_message)
                .setPositiveButton(R.string.discard) { _, _ -> finish() }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        } else {
            finish()
        }
    }

    private fun hasUnsavedChanges(): Boolean {
        val currentMemo = memoInput.text?.toString() ?: ""
        val normalizedCurrentMemo = currentMemo.takeUnless { it.isBlank() } ?: ""
        val normalizedOriginalMemo = originalMemo.takeUnless { it.isBlank() } ?: ""

        val currentCustomHolidayEnabled = customHolidaySwitch.isChecked
        val currentCustomHolidayName = customHolidayName.text?.toString()?.trim() ?: ""
        val normalizedOriginalHolidayName = originalCustomHolidayName.trim()

        if (normalizedCurrentMemo != normalizedOriginalMemo) return true
        if (currentCustomHolidayEnabled != originalCustomHolidayEnabled) return true
        if (currentCustomHolidayEnabled && currentCustomHolidayName != normalizedOriginalHolidayName) return true

        return false
    }

    private fun updateDayTypeLabel() {
        val holidays = JapaneseHolidayCalculator.holidaysForMonth(YearMonth.of(date.year, date.monthValue))
        val nationalHoliday = holidays[date]
        val customHoliday = if (customHolidaySwitch.isChecked) customHolidayName.text?.toString() ?: "" else null

        val label = when {
            customHoliday != null -> if (customHoliday.isNotBlank()) "独自休み（$customHoliday）" else "独自休み"
            nationalHoliday != null -> "$nationalHoliday (祝日)"
            date.dayOfWeek == DayOfWeek.SUNDAY -> "日曜"
            date.dayOfWeek == DayOfWeek.SATURDAY -> "土曜"
            else -> "平日"
        }
        dayTypeLabel.text = label
    }
}
