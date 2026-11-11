
package id.ac.stmi.todolist

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import java.util.Calendar
import java.util.Locale

class TaskListActivity : Activity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var taskAdapter: TaskAdapter

    private val taskItems = ArrayList<TaskItem>()

    private lateinit var tvCount: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var category: String

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        ThemeHelper.apply(this)

        dbHelper = DBHelper(this)

        category = intent.getStringExtra("category") ?: "Personal"

        val tvTitle = findViewById<TextView>(R.id.tvCategoryTitle)
        tvCount = findViewById(R.id.tvTaskCount)
        tvEmpty = findViewById(R.id.tvEmpty)

        val lvTasks = findViewById<ListView>(R.id.lvCategoryTasks)
        val btnBack = findViewById<Button>(R.id.btnBack)

        tvTitle.text = category

        btnBack.setOnClickListener {
            finish()
        }

        taskAdapter = TaskAdapter()
        lvTasks.adapter = taskAdapter

        loadTasks()

        lvTasks.setOnItemClickListener { _, _, position, _ ->

            val task = taskItems[position]

            val dateText = if (task.dueDate.isEmpty()) {
                "Belum ditentukan"
            } else {
                task.dueDate
            }

            val statusText = if (task.isCompleted) {
                "Selesai"
            } else {
                "Belum selesai"
            }

            AlertDialog.Builder(this)
                .setTitle(task.task)
                .setMessage(
                    "Kategori: ${task.category}\n" +
                            "Tanggal: $dateText\n" +
                            "Status: $statusText"
                )
                .setPositiveButton("OK", null)
                .show()
        }
    }

    private fun loadTasks() {

        val tasks = dbHelper.getTasksByCategory(category)

        taskItems.clear()
        taskItems.addAll(tasks)

        taskAdapter.notifyDataSetChanged()

        tvCount.text = "${taskItems.size} kegiatan"

        tvEmpty.visibility = if (taskItems.isEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun showEditDialog(task: TaskItem) {

        val darkMode = ThemeHelper.isDark(this)

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(8), dp(24), dp(8))
        }

        val inputTask = EditText(this).apply {
            hint = "Nama kegiatan"
            setSingleLine(true)
            textSize = 14f
            setText(task.task)

            if (darkMode) {
                setTextColor(Color.WHITE)
                setHintTextColor(Color.LTGRAY)
            }
        }

        val labelCategory = TextView(this).apply {
            text = "Pilih Kategori"
            textSize = 14f
            setPadding(0, dp(20), 0, dp(8))
            setTextColor(
                if (darkMode) Color.WHITE else Color.rgb(32, 54, 83)
            )
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

        val selectedCategory = categories.indexOf(task.category)

        if (selectedCategory >= 0) {
            spinner.setSelection(selectedCategory)
        }

        val labelDate = TextView(this).apply {
            text = "Tanggal Kegiatan"
            textSize = 14f
            setPadding(0, dp(20), 0, dp(8))
            setTextColor(
                if (darkMode) Color.WHITE else Color.rgb(32, 54, 83)
            )
        }

        var selectedDate = task.dueDate

        val dateButton = Button(this).apply {
            text = if (selectedDate.isEmpty()) {
                "Pilih Tanggal"
            } else {
                selectedDate
            }

            textSize = 14f
            setTextColor(Color.WHITE)

            backgroundTintList = ColorStateList.valueOf(
                if (darkMode) {
                    Color.rgb(66, 109, 168)
                } else {
                    Color.rgb(49, 93, 155)
                }
            )
        }

        dateButton.setOnClickListener {

            val calendar = Calendar.getInstance()

            if (selectedDate.isNotEmpty()) {
                try {
                    val parts = selectedDate.split("/")

                    calendar.set(
                        parts[2].toInt(),
                        parts[1].toInt() - 1,
                        parts[0].toInt()
                    )
                } catch (e: Exception) {
                    // Gunakan tanggal hari ini.
                }
            }

            DatePickerDialog(
                this,
                ThemeHelper.datePickerTheme(this),
                { _, year, month, day ->

                    selectedDate = String.format(
                        Locale.getDefault(),
                        "%02d/%02d/%04d",
                        day,
                        month + 1,
                        year
                    )

                    dateButton.text = selectedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        container.addView(inputTask)
        container.addView(labelCategory)
        container.addView(spinner)
        container.addView(labelDate)
        container.addView(dateButton)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Edit Task")
            .setView(container)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", null)
            .create()

        dialog.setOnShowListener {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {

                    val newTask = inputTask.text.toString().trim()
                    val newCategory = spinner.selectedItem.toString()

                    if (newTask.isEmpty()) {

                        inputTask.error =
                            "Nama kegiatan tidak boleh kosong"

                    } else {

                        val result = dbHelper.updateTask(
                            task.id,
                            newTask,
                            newCategory,
                            selectedDate
                        )

                        if (result > 0) {

                            Toast.makeText(
                                this,
                                "Kegiatan berhasil diperbarui!",
                                Toast.LENGTH_SHORT
                            ).show()

                            loadTasks()
                            dialog.dismiss()

                        } else {

                            Toast.makeText(
                                this,
                                "Gagal memperbarui kegiatan",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
        }

        dialog.show()
    }

    private fun deleteTask(task: TaskItem) {

        AlertDialog.Builder(this)
            .setTitle("Hapus Kegiatan")
            .setMessage(
                "Apakah kamu yakin ingin menghapus \"${task.task}\"?"
            )
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Delete") { _, _ ->

                val result = dbHelper.deleteTask(task.id)

                if (result > 0) {

                    Toast.makeText(
                        this,
                        "Kegiatan berhasil dihapus",
                        Toast.LENGTH_SHORT
                    ).show()

                    loadTasks()

                } else {

                    Toast.makeText(
                        this,
                        "Gagal menghapus kegiatan",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .show()
    }

    // Tombol aksi menggunakan TextView dengan ukuran tetap
    // agar tulisan Edit dan Delete tidak terpotong.
    private fun createActionButton(
        label: String,
        backgroundColor: Int,
        bottomMargin: Int = 0
    ): TextView {

        return TextView(this).apply {

            text = label
            textSize = 12f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD

            gravity = Gravity.CENTER
            isAllCaps = false
            isClickable = true
            isFocusable = true

            background = GradientDrawable().apply {
                setColor(backgroundColor)
                cornerRadius = dp(7).toFloat()
            }

            setPadding(dp(6), 0, dp(6), 0)

            layoutParams = LinearLayout.LayoutParams(
                dp(76),
                dp(36)
            ).apply {
                if (bottomMargin > 0) {
                    this.bottomMargin = dp(bottomMargin)
                }
            }
        }
    }

    private inner class TaskAdapter : BaseAdapter() {

        override fun getCount(): Int {
            return taskItems.size
        }

        override fun getItem(position: Int): TaskItem {
            return taskItems[position]
        }

        override fun getItemId(position: Int): Long {
            return taskItems[position].id.toLong()
        }

        override fun getView(
            position: Int,
            convertView: View?,
            parent: ViewGroup?
        ): View {

            val task = taskItems[position]
            val darkMode = ThemeHelper.isDark(this@TaskListActivity)

            val row = LinearLayout(this@TaskListActivity).apply {

                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL

                setPadding(
                    dp(10),
                    dp(12),
                    dp(10),
                    dp(12)
                )

                background = GradientDrawable().apply {

                    setColor(
                        if (darkMode) {
                            ThemeHelper.DARK_SURFACE
                        } else {
                            Color.WHITE
                        }
                    )

                    cornerRadius = dp(12).toFloat()

                    setStroke(
                        dp(1),
                        if (darkMode) {
                            Color.rgb(55, 70, 92)
                        } else {
                            Color.rgb(220, 229, 241)
                        }
                    )
                }

                layoutParams = AbsListViewParams()
            }

            val checkBox = CheckBox(this@TaskListActivity).apply {

                buttonTintList = ColorStateList.valueOf(
                    if (darkMode) {
                        Color.rgb(141, 184, 240)
                    } else {
                        Color.rgb(49, 93, 155)
                    }
                )

                isFocusable = false
                isChecked = task.isCompleted

                contentDescription =
                    "Tandai ${task.task} sebagai selesai"
            }

            val textContainer = LinearLayout(this@TaskListActivity).apply {

                orientation = LinearLayout.VERTICAL
                setPadding(dp(6), 0, dp(6), 0)
            }

            val tvTaskName = TextView(this@TaskListActivity).apply {

                text = task.task
                textSize = 16f

                setTextColor(
                    if (darkMode) {
                        Color.WHITE
                    } else {
                        Color.rgb(32, 54, 83)
                    }
                )

                typeface = Typeface.DEFAULT_BOLD

                if (task.isCompleted) {
                    paintFlags =
                        paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    alpha = 0.55f
                } else {
                    paintFlags =
                        paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    alpha = 1f
                }
            }

            val tvDate = TextView(this@TaskListActivity).apply {

                text = if (task.dueDate.isEmpty()) {
                    "Tanggal belum ditentukan"
                } else {
                    task.dueDate
                }

                textSize = 12f

                setTextColor(
                    if (darkMode) {
                        Color.rgb(190, 204, 224)
                    } else {
                        Color.rgb(113, 128, 150)
                    }
                )

                setPadding(0, dp(6), 0, 0)
            }

            textContainer.addView(tvTaskName)
            textContainer.addView(tvDate)

            val actionContainer = LinearLayout(this@TaskListActivity).apply {

                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
            }

            val btnEdit = createActionButton(
                "Edit",
                if (darkMode) {
                    Color.rgb(66, 109, 168)
                } else {
                    Color.rgb(49, 93, 155)
                },
                bottomMargin = 6
            )

            val btnDelete = createActionButton(
                "Delete",
                if (darkMode) {
                    Color.rgb(176, 79, 91)
                } else {
                    Color.rgb(190, 75, 85)
                }
            )

            actionContainer.addView(btnEdit)
            actionContainer.addView(btnDelete)

            row.addView(
                checkBox,
                LinearLayout.LayoutParams(
                    dp(38),
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )

            row.addView(
                textContainer,
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            row.addView(
                actionContainer,
                LinearLayout.LayoutParams(
                    dp(82),
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )

            checkBox.setOnCheckedChangeListener { _, isChecked ->

                dbHelper.updateTaskStatus(
                    task.id,
                    isChecked
                )

                loadTasks()
            }

            btnEdit.setOnClickListener {
                showEditDialog(task)
            }

            btnDelete.setOnClickListener {
                deleteTask(task)
            }

            return row
        }
    }

    private fun AbsListViewParams(): ViewGroup.LayoutParams {
        return ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroy() {

        if (::dbHelper.isInitialized) {
            dbHelper.close()
        }

        super.onDestroy()
    }
}
