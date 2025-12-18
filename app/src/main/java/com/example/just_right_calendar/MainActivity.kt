package com.example.just_right_calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.min

class MainActivity : AppCompatActivity() {
    private lateinit var calendarGrid: GridLayout
    private lateinit var monthLabel: TextView
    private lateinit var prevButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var todayButton: Button

    private var currentMonth: YearMonth = YearMonth.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        CalendarRepository.initialize(applicationContext)

        calendarGrid = findViewById(R.id.calendarGrid)
        monthLabel = findViewById(R.id.monthLabel)
        prevButton = findViewById(R.id.prevButton)
        nextButton = findViewById(R.id.nextButton)
        todayButton = findViewById(R.id.todayButton)

        prevButton.setOnClickListener {
            currentMonth = currentMonth.minusMonths(1)
            renderCalendar()
        }

        nextButton.setOnClickListener {
            currentMonth = currentMonth.plusMonths(1)
            renderCalendar()
        }

        todayButton.setOnClickListener {
            currentMonth = YearMonth.now()
            renderCalendar()
        }

        renderCalendar()
    }

    private fun renderCalendar() {
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM")
        monthLabel.text = currentMonth.format(formatter)

        calendarGrid.removeAllViews()

        val firstDay = currentMonth.atDay(1)
        val daysInMonth = currentMonth.lengthOfMonth()
        val startOffset = ((firstDay.dayOfWeek.value + 6) % 7)

        repeat(42) { index ->
            val dayNumber = index - startOffset + 1
            val date = if (dayNumber in 1..daysInMonth) {
                currentMonth.atDay(dayNumber)
            } else {
                null
            }
            val dayView = createDayCell(date)
            calendarGrid.addView(dayView)
        }
    }

    private fun createDayCell(date: LocalDate?): View {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.day_cell, calendarGrid, false)

        val dayNumber = view.findViewById<TextView>(R.id.dayNumber)
        val markText = view.findViewById<TextView>(R.id.markText)
        val topArea = view.findViewById<LinearLayout>(R.id.dayTopArea)
        val bottomArea = view.findViewById<LinearLayout>(R.id.dayBottomArea)

        val horizontalPadding = dpToPx(32f)
        val cellWidthPx = (resources.displayMetrics.widthPixels - horizontalPadding) / 7f
        val params = GridLayout.LayoutParams().apply {
            width = 0
            height = (cellWidthPx * 1.5f).toInt()
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        }
        view.layoutParams = params

        if (date == null) {
            dayNumber.text = ""
            markText.text = ""
            topArea.setBackgroundColor(ContextCompat.getColor(this, R.color.calendar_default_day_bg))
            bottomArea.setBackgroundColor(ContextCompat.getColor(this, R.color.calendar_default_day_bg))
            return view
        }

        dayNumber.text = date.dayOfMonth.toString()

        val marks = CalendarRepository.getMarks(date)
        markText.text = marks.joinToString("") { it.symbol }

        val isToday = date == LocalDate.now()
        val isHoliday = CalendarRepository.isUserHoliday(date)
        val dayOfWeek = date.dayOfWeek

        val topColor = when {
            isHoliday -> R.color.calendar_holiday_bg
            dayOfWeek == DayOfWeek.SUNDAY -> R.color.calendar_holiday_bg
            dayOfWeek == DayOfWeek.SATURDAY -> R.color.calendar_saturday_bg
            else -> R.color.calendar_default_day_bg
        }
        val bottomColor = if (isToday) {
            R.color.calendar_today_bg
        } else {
            R.color.calendar_default_day_bg
        }
        val textColor = when (dayOfWeek) {
            DayOfWeek.SUNDAY -> R.color.calendar_sunday_text
            else -> R.color.text_primary
        }

        topArea.setBackgroundColor(ContextCompat.getColor(this, topColor))
        bottomArea.setBackgroundColor(ContextCompat.getColor(this, bottomColor))
        dayNumber.setTextColor(ContextCompat.getColor(this, textColor))

        return view
    }

    private fun dpToPx(dp: Float): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}
