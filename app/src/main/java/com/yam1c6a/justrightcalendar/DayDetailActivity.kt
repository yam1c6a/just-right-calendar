package com.yam1c6a.justrightcalendar

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class DayDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_day_detail)

        CalendarRepository.initialize(applicationContext)

        val date = intent.getStringExtra(EXTRA_DATE)?.let(LocalDate::parse) ?: run {
            finish()
            return
        }

        val dateLabel: TextView = findViewById(R.id.dateLabel)
        val dayTypeLabel: TextView = findViewById(R.id.dayTypeLabel)
        val markCircle: CheckBox = findViewById(R.id.markCircle)
        val markCheck: CheckBox = findViewById(R.id.markCheck)
        val markStar: CheckBox = findViewById(R.id.markStar)
        val holidayToggle: SwitchCompat = findViewById(R.id.holidayToggle)
        val saveButton: Button = findViewById(R.id.saveButton)

        val formatter = DateTimeFormatter.ofPattern("yyyy年M月d日")
        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.JAPAN)
        dateLabel.text = "${date.format(formatter)} (${dayOfWeek})"

        val marks = CalendarRepository.getMarks(date)
        markCircle.isChecked = marks.contains(MarkType.CIRCLE)
        markCheck.isChecked = marks.contains(MarkType.CHECK)
        markStar.isChecked = marks.contains(MarkType.STAR)

        val holidayName = JapaneseHolidayCalculator.holidaysForMonth(YearMonth.of(date.year, date.month))[date]
        val isUserHoliday = CalendarRepository.isUserHoliday(date)
        holidayToggle.isChecked = isUserHoliday

        dayTypeLabel.text = when {
            isUserHoliday -> getString(R.string.user_holiday_label)
            holidayName != null -> holidayName
            else -> dayOfWeek
        }

        saveButton.setOnClickListener {
            val selectedMarks = mutableSetOf<MarkType>()
            if (markCircle.isChecked) selectedMarks.add(MarkType.CIRCLE)
            if (markCheck.isChecked) selectedMarks.add(MarkType.CHECK)
            if (markStar.isChecked) selectedMarks.add(MarkType.STAR)

            CalendarRepository.saveMarks(date, selectedMarks)
            CalendarRepository.saveHoliday(date, holidayToggle.isChecked)

            finish()
        }
    }

    companion object {
        const val EXTRA_DATE = "extra_date"
    }
}
