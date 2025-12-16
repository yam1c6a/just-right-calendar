package com.example.just_right_calendar

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class MainActivity : AppCompatActivity() {

    private lateinit var calendarGrid: GridLayout
    private lateinit var monthLabel: TextView
    private lateinit var toolbar: MaterialToolbar
    private var currentYearMonth: YearMonth = YearMonth.now()
    private val today: LocalDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CalendarRepository.initialize(applicationContext)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.topAppBar)
        calendarGrid = findViewById(R.id.calendarGrid)
        monthLabel = findViewById(R.id.monthLabel)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.title = ""
        supportActionBar?.subtitle = ""

        findViewById<View>(R.id.prevButton).setOnClickListener {
            val message = "prev clicked: $currentYearMonth"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            Log.d("JRC", message)
            currentYearMonth = currentYearMonth.minusMonths(1)
            renderCalendar()
        }

        findViewById<View>(R.id.nextButton).setOnClickListener {
            val message = "next clicked: $currentYearMonth"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            Log.d("JRC", message)
            currentYearMonth = currentYearMonth.plusMonths(1)
            renderCalendar()
        }

        findViewById<View>(R.id.todayButton).setOnClickListener {
            currentYearMonth = YearMonth.now()
            renderCalendar()
        }

        renderCalendar()

        toolbar.title = ""
        toolbar.subtitle = ""
    }

    override fun onResume() {
        super.onResume()
        renderCalendar()
    }

    private fun renderCalendar() {
        val yearMonth = currentYearMonth
        monthLabel.text = getString(R.string.month_format, yearMonth.year, yearMonth.monthValue)

        calendarGrid.removeAllViews()
        calendarGrid.columnCount = 7

        val firstDayOfMonth = yearMonth.atDay(1)
        val leadingEmpty = (firstDayOfMonth.dayOfWeek.value + 6) % 7
        val daysInMonth = yearMonth.lengthOfMonth()
        val totalCells = ((leadingEmpty + daysInMonth + 6) / 7) * 7
        calendarGrid.rowCount = totalCells / 7

        val holidays = JapaneseHolidayCalculator.holidaysForMonth(yearMonth)

        for (i in 0 until totalCells) {
            val dayNumber = i - leadingEmpty + 1
            if (dayNumber in 1..daysInMonth) {
                val date = yearMonth.atDay(dayNumber)
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
        val view = inflater.inflate(R.layout.day_cell, calendarGrid, false)
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
        val view = inflater.inflate(R.layout.day_cell, calendarGrid, false)
        val params = GridLayout.LayoutParams().apply {
            width = 0
            height = GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        }
        view.layoutParams = params

        val topArea = view.findViewById<LinearLayout>(R.id.dayTopArea)
        val bottomArea = view.findViewById<LinearLayout>(R.id.dayBottomArea)
        val dayNumber = view.findViewById<TextView>(R.id.dayNumber)
        val markText = view.findViewById<TextView>(R.id.markText)

        val markParams = markText.layoutParams
        val bottomMargin = resources.getDimensionPixelSize(R.dimen.mark_bottom_margin)
        when (markParams) {
            is FrameLayout.LayoutParams -> {
                markParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                markParams.bottomMargin = bottomMargin
                markText.layoutParams = markParams
            }
            is ViewGroup.MarginLayoutParams -> {
                markParams.bottomMargin = bottomMargin
                markText.layoutParams = markParams
            }
        }
        val bottomPadding = resources.getDimensionPixelSize(R.dimen.mark_bottom_padding)
        markText.setPadding(markText.paddingLeft, markText.paddingTop, markText.paddingRight, bottomPadding)
        markText.gravity = Gravity.CENTER
        val markTextSize = resources.getDimension(R.dimen.mark_text_default_size)
        markText.setTextSize(TypedValue.COMPLEX_UNIT_PX, markTextSize)

        dayNumber.text = date.dayOfMonth.toString()

        val isUserHoliday = CalendarRepository.isUserHoliday(date)
        val isHoliday = isUserHoliday || holidays.containsKey(date) || date.dayOfWeek == DayOfWeek.SUNDAY
        val isSaturday = date.dayOfWeek == DayOfWeek.SATURDAY

        val topColor = when {
            isHoliday -> ContextCompat.getColor(this, R.color.holiday_red)
            isSaturday -> ContextCompat.getColor(this, R.color.saturday_blue)
            else -> Color.WHITE
        }
        topArea.setBackgroundColor(topColor)

        val bottomColor = if (date == today) ContextCompat.getColor(this, R.color.today_green) else Color.WHITE
        bottomArea.setBackgroundColor(bottomColor)

        val marks = CalendarRepository.getMarks(date)
        val markSymbol = marks.firstOrNull()?.symbol.orEmpty()
        markText.text = markSymbol
        markText.visibility = if (marks.isEmpty()) View.GONE else View.VISIBLE

        view.setOnClickListener {
            val intent = Intent(this, DayDetailActivity::class.java).apply {
                putExtra(DayDetailActivity.EXTRA_DATE, date.toString())
            }
            startActivity(intent)
        }

        return view
    }
}
