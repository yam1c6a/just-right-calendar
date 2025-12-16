package com.example.just_right_calendar

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
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
        memoInput.setText(memo)

        val customHoliday = CalendarRepository.getCustomHoliday(date)
        if (customHoliday != null) {
            customHolidaySwitch.isChecked = true
            customHolidayName.isEnabled = true
            customHolidayName.setText(customHoliday)
        }

        updateDayTypeLabel()

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
