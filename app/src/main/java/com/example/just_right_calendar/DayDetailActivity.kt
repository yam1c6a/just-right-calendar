package com.example.just_right_calendar

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class DayDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DATE = "selected_date"
    }

    private lateinit var date: LocalDate
    private lateinit var dayTypeLabel: TextView
    private lateinit var circleCheck: CheckBox
    private lateinit var checkCheck: CheckBox
    private lateinit var starCheck: CheckBox
    private var originalMarks: Set<MarkType> = emptySet()

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
        dayTypeLabel = findViewById(R.id.dayTypeLabel)
        circleCheck = findViewById(R.id.markCircle)
        checkCheck = findViewById(R.id.markCheck)
        starCheck = findViewById(R.id.markStar)
        val saveButton = findViewById<Button>(R.id.saveButton)

        dateLabel.text = date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

        val marks = CalendarRepository.getMarks(date)
        originalMarks = marks
        circleCheck.isChecked = marks.contains(MarkType.CIRCLE)
        checkCheck.isChecked = marks.contains(MarkType.CHECK)
        starCheck.isChecked = marks.contains(MarkType.STAR)

        updateDayTypeLabel()

        onBackPressedDispatcher.addCallback(this) {
            handleBackNavigation()
        }

        saveButton.setOnClickListener {
            CalendarRepository.saveMarks(date, collectSelectedMarks())
            finish()
        }
    }

    private fun collectSelectedMarks(): Set<MarkType> {
        val result = mutableSetOf<MarkType>()
        if (circleCheck.isChecked) result.add(MarkType.CIRCLE)
        if (checkCheck.isChecked) result.add(MarkType.CHECK)
        if (starCheck.isChecked) result.add(MarkType.STAR)
        return result
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
        val currentMarks = collectSelectedMarks()
        return currentMarks != originalMarks
    }

    private fun updateDayTypeLabel() {
        val holidays = JapaneseHolidayCalculator.holidaysForMonth(YearMonth.of(date.year, date.monthValue))
        val nationalHoliday = holidays[date]

        val label = when {
            nationalHoliday != null -> "$nationalHoliday (祝日)"
            date.dayOfWeek == DayOfWeek.SUNDAY -> "日曜"
            date.dayOfWeek == DayOfWeek.SATURDAY -> "土曜"
            else -> "平日"
        }
        dayTypeLabel.text = label
    }
}
