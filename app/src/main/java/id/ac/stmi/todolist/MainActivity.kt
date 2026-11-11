
package id.ac.stmi.todolist

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import java.util.Calendar
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var dbHelper: DBHelper
    private var appliedTheme = "Light Mode"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appliedTheme = getSharedPreferences(
            "AppSettings",
            MODE_PRIVATE
        ).getString("theme", "Light Mode") ?: "Light Mode"

        ThemeHelper.apply(this)

        dbHelper = DBHelper(this)

        findViewById<Button>(R.id.btnAddTask).setOnClickListener {
            showAddTaskDialog()
        }

        findViewById<Button>(R.id.btnOpenCalendar).setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        setupCategoryCards()
        updateDashboard()
    }

    private fun setupCategoryCards() {

        findViewById<View>(R.id.cardToday).setOnClickListener {
            openCategory("Today")
        }

        findViewById<View>(R.id.cardPlanned).setOnClickListener {
            openCategory("Planned")
        }

        findViewById<View>(R.id.cardPersonal).setOnClickListener {
            openCategory("Personal")
        }

        findViewById<View>(R.id.cardWork).setOnClickListener {
            openCategory("Work")
        }

        findViewById<View>(R.id.cardShopping).setOnClickListener {
            openCategory("Shopping")
        }
    }

    private fun openCategory(category: String) {

        val intent = Intent(
            this,
            TaskListActivity::class.java
        )

        intent.putExtra("category", category)
        startActivity(intent)
    }

    private fun updateDashboard() {

        val tasks = dbHelper.getAllTaskDetails()

        val total = tasks.size
        val completed = tasks.count { it.isCompleted }
        val pending = total - completed

        findViewById<TextView>(R.id.tvTotalTasks).text =
            "$total Tasks"

        findViewById<TextView>(R.id.tvCompletedTasks).text =
            completed.toString()

        findViewById<TextView>(R.id.tvPendingTasks).text =
            pending.toString()

        findViewById<TextView>(R.id.tvTodayCount).text =
            "${tasks.count { it.category == "Today" }} Tasks"

        findViewById<TextView>(R.id.tvPlannedCount).text =
            "${tasks.count { it.category == "Planned" }} Tasks"

        findViewById<TextView>(R.id.tvPersonalCount).text =
            "${tasks.count { it.category == "Personal" }} Tasks"

        findViewById<TextView>(R.id.tvWorkCount).text =
            "${tasks.count { it.category == "Work" }} Tasks"

        findViewById<TextView>(R.id.tvShoppingCount).text =
            "${tasks.count { it.category == "Shopping" }} Tasks"
    }

    private fun showAddTaskDialog() {

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 8)
        }

        val inputTask = EditText(this).apply {
            hint = "Contoh: Mengerjakan tugas kuliah"
            textSize = 14f
            setSingleLine(true)
        }

        val labelCategory = TextView(this).apply {
            text = "Pilih Kategori"
            textSize = 14f
            setPadding(0, 20, 0, 8)
        }

        val spinner = Spinner(this)

        val categories = arrayOf(
            "Today",
            "Planned",
            "Personal",
            "Work",
            "Shopping"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            categories
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinner.adapter = adapter

        val labelDate = TextView(this).apply {
            text = "Pilih Tanggal Kegiatan"
            textSize = 14f
            setPadding(0, 20, 0, 8)
        }

        var selectedDate = ""

        val dateButton = Button(this).apply {
            text = "Pilih Tanggal"
            textSize = 14f
        }

        dateButton.setOnClickListener {

            val calendar = Calendar.getInstance()

            val datePicker = DatePickerDialog(
                this,
                ThemeHelper.datePickerTheme(this),
                { _, year, month, dayOfMonth ->

                    selectedDate = String.format(
                        Locale.getDefault(),
                        "%02d/%02d/%04d",
                        dayOfMonth,
                        month + 1,
                        year
                    )

                    dateButton.text = selectedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePicker.show()
        }

        container.addView(
            inputTask,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        container.addView(labelCategory)
        container.addView(spinner)
        container.addView(labelDate)
        container.addView(dateButton)

        // Menerapkan warna Dark Mode pada komponen dialog
        ThemeHelper.applyViews(
            container,
            ThemeHelper.isDark(this)
        )

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Task")
            .setView(container)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", null)
            .create()

        dialog.setOnShowListener {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {

                    val task = inputTask.text.toString().trim()
                    val category = spinner.selectedItem.toString()

                    if (task.isEmpty()) {

                        inputTask.error =
                            "Nama kegiatan tidak boleh kosong"

                    } else {

                        val result = dbHelper.insertTask(
                            task,
                            category,
                            selectedDate
                        )

                        if (result != -1L) {

                            Toast.makeText(
                                this,
                                "Kegiatan berhasil disimpan!",
                                Toast.LENGTH_SHORT
                            ).show()

                            updateDashboard()
                            dialog.dismiss()

                        } else {

                            Toast.makeText(
                                this,
                                "Gagal menyimpan kegiatan",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
        }

        dialog.show()

        // Menerapkan warna Dark Mode pada AlertDialog
        ThemeHelper.applyDialog(dialog, this)
    }

    override fun onResume() {
        super.onResume()

        if (::dbHelper.isInitialized) {

            val currentTheme = getSharedPreferences(
                "AppSettings",
                MODE_PRIVATE
            ).getString("theme", "Light Mode") ?: "Light Mode"

            if (currentTheme != appliedTheme) {
                recreate()
                return
            }

            updateDashboard()
        }
    }

    override fun onDestroy() {

        if (::dbHelper.isInitialized) {
            dbHelper.close()
        }

        super.onDestroy()
    }
}
