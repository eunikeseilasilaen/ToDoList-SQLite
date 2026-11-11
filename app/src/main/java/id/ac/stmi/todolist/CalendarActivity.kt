
package id.ac.stmi.todolist

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import java.util.Calendar
import java.util.Locale

class CalendarActivity : Activity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var tvSelectedDate: TextView
    private lateinit var tvEmptyCalendar: TextView
    private lateinit var lvCalendarTasks: ListView
    private lateinit var taskAdapter: ArrayAdapter<String>

    private var selectedDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        ThemeHelper.apply(this)

        dbHelper = DBHelper(this)

        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        val btnBack = findViewById<Button>(R.id.btnCalendarBack)

        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        tvEmptyCalendar = findViewById(R.id.tvEmptyCalendar)
        lvCalendarTasks = findViewById(R.id.lvCalendarTasks)

        val darkMode = ThemeHelper.isDark(this)

        // Warna dasar kalender.
        calendarView.setBackgroundColor(
            if (darkMode) ThemeHelper.DARK_SURFACE
            else android.graphics.Color.WHITE
        )

        // Warna angka tanggal.
        calendarView.setFocusedMonthDateColor(
            if (darkMode) 0xFFF8FAFC.toInt()
            else 0xFF203653.toInt()
        )

        calendarView.setUnfocusedMonthDateColor(
            if (darkMode) 0xFF64748B.toInt()
            else 0xFFADB8C8.toInt()
        )

        // Warna tanggal yang dipilih.
        calendarView.setSelectedWeekBackgroundColor(
            if (darkMode) 0xFF315D9B.toInt()
            else 0xFFDCE8FA.toInt()
        )

        calendarView.setWeekNumberColor(
            if (darkMode) 0xFFB8C5D8.toInt()
            else 0xFF71809A.toInt()
        )

        calendarView.setDateTextAppearance(
            if (darkMode) R.style.CalendarDateTextDark
            else R.style.CalendarDateTextLight
        )

        calendarView.setWeekDayTextAppearance(
            if (darkMode) R.style.CalendarWeekTextDark
            else R.style.CalendarWeekTextLight
        )

        // Mengubah warna tulisan bulan dan tombol panah kalender.
        calendarView.post {

            val headerColor = if (darkMode) {
                0xFFF8FAFC.toInt()
            } else {
                0xFF203653.toInt()
            }

            val monthNameId = resources.getIdentifier(
                "month_name",
                "id",
                "android"
            )

            val previousButtonId = resources.getIdentifier(
                "prev",
                "id",
                "android"
            )

            val nextButtonId = resources.getIdentifier(
                "next",
                "id",
                "android"
            )

            calendarView.findViewById<View>(monthNameId)?.let { header ->
                if (header is TextView) {
                    header.setTextColor(headerColor)
                }
            }

            listOf(previousButtonId, nextButtonId).forEach { viewId ->

                calendarView.findViewById<View>(viewId)?.let { arrow ->

                    if (arrow is ImageView) {
                        arrow.setColorFilter(headerColor)
                    } else if (arrow is TextView) {
                        arrow.setTextColor(headerColor)
                    }
                }
            }
        }

        // Adapter daftar tugas dengan warna sesuai tema.
        taskAdapter = object : ArrayAdapter<String>(
            this,
            android.R.layout.simple_list_item_1,
            ArrayList<String>()
        ) {

            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {

                val row = super.getView(position, convertView, parent)

                val text = row.findViewById<TextView>(
                    android.R.id.text1
                )

                text.setTextColor(
                    if (ThemeHelper.isDark(this@CalendarActivity)) {
                        0xFFF8FAFC.toInt()
                    } else {
                        0xFF203653.toInt()
                    }
                )

                text.textSize = 14f
                text.setPadding(16, 16, 16, 16)

                row.setBackgroundColor(
                    if (ThemeHelper.isDark(this@CalendarActivity)) {
                        ThemeHelper.DARK_SURFACE
                    } else {
                        android.graphics.Color.WHITE
                    }
                )

                return row
            }
        }

        lvCalendarTasks.adapter = taskAdapter

        lvCalendarTasks.setBackgroundColor(
            if (darkMode) ThemeHelper.DARK_BACKGROUND
            else ThemeHelper.LIGHT_BACKGROUND
        )

        btnBack.setOnClickListener {
            finish()
        }

        calendarView.setOnDateChangeListener {
                _, year, month, dayOfMonth ->

            selectedDate = String.format(
                Locale.getDefault(),
                "%02d/%02d/%04d",
                dayOfMonth,
                month + 1,
                year
            )

            showTasksForSelectedDate()
        }

        // Tampilkan tanggal hari ini saat halaman dibuka.
        val today = Calendar.getInstance()

        selectedDate = String.format(
            Locale.getDefault(),
            "%02d/%02d/%04d",
            today.get(Calendar.DAY_OF_MONTH),
            today.get(Calendar.MONTH) + 1,
            today.get(Calendar.YEAR)
        )

        showTasksForSelectedDate()
    }

    private fun showTasksForSelectedDate() {

        tvSelectedDate.text = "Tasks on $selectedDate"

        val allTasks = dbHelper.getAllTaskDetails()

        val filteredTasks = allTasks.filter {
            it.dueDate == selectedDate
        }

        val taskDescriptions = filteredTasks.map { task ->

            val status = if (task.isCompleted) {
                "✓ Selesai"
            } else {
                "○ Belum selesai"
            }

            "${task.task}\nKategori: ${task.category}\n$status"
        }

        taskAdapter.clear()
        taskAdapter.addAll(taskDescriptions)
        taskAdapter.notifyDataSetChanged()

        tvEmptyCalendar.visibility = if (filteredTasks.isEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }

        lvCalendarTasks.visibility = if (filteredTasks.isEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()

        if (::dbHelper.isInitialized && ::taskAdapter.isInitialized) {
            showTasksForSelectedDate()
        }
    }

    override fun onDestroy() {
        if (::dbHelper.isInitialized) {
            dbHelper.close()
        }

        super.onDestroy()
    }
}
