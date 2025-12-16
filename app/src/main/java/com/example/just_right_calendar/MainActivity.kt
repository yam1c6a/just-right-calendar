package com.example.just_right_calendar

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class MainActivity : AppCompatActivity() {

    private lateinit var calendarGrid: GridLayout
    private lateinit var monthLabel: TextView
    private var currentYear: Int = YearMonth.now().year
    private var currentMonth: Int = YearMonth.now().monthValue
    private val today: LocalDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CalendarRepository.initialize(applicationContext)
        setContentView(R.layout.activity_main)

        calendarGrid = findViewById(R.id.calendarGrid)
        monthLabel = findViewById(R.id.monthLabel)

        findViewById<View>(R.id.prevButton).setOnClickListener {
            if (currentMonth == 1) {
                currentYear -= 1
                currentMonth = 12
            } else {
                currentMonth -= 1
            }
            renderCalendar()
        }

        findViewById<View>(R.id.nextButton).setOnClickListener {
            if (currentMonth == 12) {
                currentYear += 1
                currentMonth = 1
            } else {
                currentMonth += 1
            }
            renderCalendar()
        }

        findViewById<View>(R.id.todayButton).setOnClickListener {
            val now = YearMonth.now()
            currentYear = now.year
            currentMonth = now.monthValue
            renderCalendar()
        }

        renderCalendar()
    }

    override fun onResume() {
        super.onResume()
        renderCalendar()
    }

    private fun renderCalendar() {
        monthLabel.text = getString(R.string.month_format, currentYear, currentMonth)

        val currentYearMonth = YearMonth.of(currentYear, currentMonth)

        calendarGrid.removeAllViews()
        calendarGrid.columnCount = 7

        val firstDayOfMonth = currentYearMonth.atDay(1)
        val leadingEmpty = (firstDayOfMonth.dayOfWeek.value + 6) % 7
        val daysInMonth = currentYearMonth.lengthOfMonth()
        val totalCells = ((leadingEmpty + daysInMonth + 6) / 7) * 7
        calendarGrid.rowCount = totalCells / 7

        val holidays = JapaneseHolidayCalculator.holidaysForMonth(currentYearMonth)

        for (i in 0 until totalCells) {
            val dayNumber = i - leadingEmpty + 1
            if (dayNumber in 1..daysInMonth) {
                val date = currentYearMonth.atDay(dayNumber)
                val cell = createDayCell(date, holidays)
                calendarGrid.addView(cell)
            } else {
                val placeholder = createEmptyCell()
                calendarGrid.addView(placeholder)
            }
        }
    }

    private fun createEmptyCell(): View {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.day_cell, calendarGrid, false) as LinearLayout
        val params = GridLayout.LayoutParams().apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        }
        view.layoutParams = params
        view.visibility = View.INVISIBLE
        return view
    }

    private fun createDayCell(date: LocalDate, holidays: Map<LocalDate, String>): View {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.day_cell, calendarGrid, false) as LinearLayout
        val params = GridLayout.LayoutParams().apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        }
        view.layoutParams = params

        val topArea = view.findViewById<LinearLayout>(R.id.dayTopArea)
        val bottomArea = view.findViewById<LinearLayout>(R.id.dayBottomArea)
        val dayNumber = view.findViewById<TextView>(R.id.dayNumber)
        val memoText = view.findViewById<TextView>(R.id.memoText)

        dayNumber.text = date.dayOfMonth.toString()

        val isHoliday = holidays.containsKey(date) || CalendarRepository.getCustomHoliday(date) != null || date.dayOfWeek == DayOfWeek.SUNDAY
        val isSaturday = date.dayOfWeek == DayOfWeek.SATURDAY

        val topColor = when {
            isHoliday -> ContextCompat.getColor(this, R.color.holiday_red)
            isSaturday -> ContextCompat.getColor(this, R.color.saturday_blue)
            else -> Color.WHITE
        }
        topArea.setBackgroundColor(topColor)

        val bottomColor = if (date == today) ContextCompat.getColor(this, R.color.today_pink) else Color.WHITE
        bottomArea.setBackgroundColor(bottomColor)

        val memo = CalendarRepository.getMemo(date)
        memoText.text = memo ?: ""
        memoText.visibility = if (memo.isNullOrBlank()) View.GONE else View.VISIBLE

        view.setOnClickListener {
            val intent = Intent(this, DayDetailActivity::class.java).apply {
                putExtra(DayDetailActivity.EXTRA_DATE, date.toString())
            }
            startActivity(intent)
        }

        return view
    }
}
