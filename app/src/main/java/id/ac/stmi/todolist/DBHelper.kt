
package id.ac.stmi.todolist

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class TaskItem(
    val id: Int,
    val task: String,
    val category: String,
    val dueDate: String,
    val isCompleted: Boolean
)

data class ProfileItem(
    val name: String,
    val email: String,
    val phone: String,
    val address: String
)

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "ToDoList.db"
        private const val DATABASE_VERSION = 3

        // Tabel Task
        private const val TABLE_TASK = "tasks"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TASK = "task"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_DATE = "due_date"
        private const val COLUMN_COMPLETED = "is_completed"

        // Tabel Profil
        private const val TABLE_PROFILE = "user_profile"
        private const val PROFILE_ID = "id"
        private const val PROFILE_NAME = "name"
        private const val PROFILE_EMAIL = "email"
        private const val PROFILE_PHONE = "phone"
        private const val PROFILE_ADDRESS = "address"
    }

    override fun onCreate(db: SQLiteDatabase) {

        // Membuat tabel Task
        val createTaskTable = """
            CREATE TABLE $TABLE_TASK (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TASK TEXT NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL DEFAULT 'Personal',
                $COLUMN_DATE TEXT NOT NULL DEFAULT '',
                $COLUMN_COMPLETED INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()

        db.execSQL(createTaskTable)

        // Membuat tabel Profil
        val createProfileTable = """
            CREATE TABLE $TABLE_PROFILE (
                $PROFILE_ID INTEGER PRIMARY KEY,
                $PROFILE_NAME TEXT NOT NULL,
                $PROFILE_EMAIL TEXT NOT NULL,
                $PROFILE_PHONE TEXT NOT NULL,
                $PROFILE_ADDRESS TEXT NOT NULL
            )
        """.trimIndent()

        db.execSQL(createProfileTable)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {

        // Pembaruan database dari versi 1 ke versi 2
        if (oldVersion < 2) {
            db.execSQL(
                "ALTER TABLE $TABLE_TASK ADD COLUMN $COLUMN_CATEGORY TEXT NOT NULL DEFAULT 'Personal'"
            )

            db.execSQL(
                "ALTER TABLE $TABLE_TASK ADD COLUMN $COLUMN_DATE TEXT NOT NULL DEFAULT ''"
            )

            db.execSQL(
                "ALTER TABLE $TABLE_TASK ADD COLUMN $COLUMN_COMPLETED INTEGER NOT NULL DEFAULT 0"
            )
        }

        // Pembaruan database dari versi 2 ke versi 3
        if (oldVersion < 3) {
            val createProfileTable = """
                CREATE TABLE $TABLE_PROFILE (
                    $PROFILE_ID INTEGER PRIMARY KEY,
                    $PROFILE_NAME TEXT NOT NULL,
                    $PROFILE_EMAIL TEXT NOT NULL,
                    $PROFILE_PHONE TEXT NOT NULL,
                    $PROFILE_ADDRESS TEXT NOT NULL
                )
            """.trimIndent()

            db.execSQL(createProfileTable)
        }
    }

    // =====================================
    // FUNGSI PROFIL PENGGUNA
    // =====================================

    // Menyimpan atau memperbarui profil
    fun saveProfile(
        name: String,
        email: String,
        phone: String,
        address: String
    ): Long {

        val values = ContentValues().apply {
            put(PROFILE_ID, 1)
            put(PROFILE_NAME, name)
            put(PROFILE_EMAIL, email)
            put(PROFILE_PHONE, phone)
            put(PROFILE_ADDRESS, address)
        }

        return writableDatabase.insertWithOnConflict(
            TABLE_PROFILE,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    // Mengambil data profil pengguna
    fun getProfile(): ProfileItem? {

        val cursor = readableDatabase.query(
            TABLE_PROFILE,
            null,
            "$PROFILE_ID = ?",
            arrayOf("1"),
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                return ProfileItem(
                    name = it.getString(
                        it.getColumnIndexOrThrow(PROFILE_NAME)
                    ),
                    email = it.getString(
                        it.getColumnIndexOrThrow(PROFILE_EMAIL)
                    ),
                    phone = it.getString(
                        it.getColumnIndexOrThrow(PROFILE_PHONE)
                    ),
                    address = it.getString(
                        it.getColumnIndexOrThrow(PROFILE_ADDRESS)
                    )
                )
            }
        }

        return null
    }

    // =====================================
    // FUNGSI TASK
    // =====================================

    // Menambahkan kegiatan
    fun insertTask(
        task: String,
        category: String,
        dueDate: String
    ): Long {

        val values = ContentValues().apply {
            put(COLUMN_TASK, task)
            put(COLUMN_CATEGORY, category)
            put(COLUMN_DATE, dueDate)
            put(COLUMN_COMPLETED, 0)
        }

        return writableDatabase.insert(TABLE_TASK, null, values)
    }

    // Kompatibilitas dengan kode versi awal
    fun insertTask(task: String): Long {
        return insertTask(task, "Personal", "")
    }

    // Mengambil seluruh kegiatan beserta detailnya
    fun getAllTaskDetails(): ArrayList<TaskItem> {

        val taskList = ArrayList<TaskItem>()

        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM $TABLE_TASK ORDER BY $COLUMN_ID DESC",
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                taskList.add(
                    TaskItem(
                        id = it.getInt(
                            it.getColumnIndexOrThrow(COLUMN_ID)
                        ),
                        task = it.getString(
                            it.getColumnIndexOrThrow(COLUMN_TASK)
                        ),
                        category = it.getString(
                            it.getColumnIndexOrThrow(COLUMN_CATEGORY)
                        ),
                        dueDate = it.getString(
                            it.getColumnIndexOrThrow(COLUMN_DATE)
                        ),
                        isCompleted = it.getInt(
                            it.getColumnIndexOrThrow(COLUMN_COMPLETED)
                        ) == 1
                    )
                )
            }
        }

        return taskList
    }

    // Mengambil daftar nama kegiatan
    fun getAllTasks(): ArrayList<String> {
        return ArrayList(
            getAllTaskDetails().map { it.task }
        )
    }

    // Mengubah kegiatan
    fun updateTask(
        id: Int,
        task: String,
        category: String,
        dueDate: String
    ): Int {

        val values = ContentValues().apply {
            put(COLUMN_TASK, task)
            put(COLUMN_CATEGORY, category)
            put(COLUMN_DATE, dueDate)
        }

        return writableDatabase.update(
            TABLE_TASK,
            values,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
    }

    // Menghapus kegiatan
    fun deleteTask(id: Int): Int {
        return writableDatabase.delete(
            TABLE_TASK,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
    }

    // Mengubah status kegiatan
    fun updateTaskStatus(
        id: Int,
        isCompleted: Boolean
    ): Int {

        val values = ContentValues().apply {
            put(COLUMN_COMPLETED, if (isCompleted) 1 else 0)
        }

        return writableDatabase.update(
            TABLE_TASK,
            values,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
    }

    // Mengambil kegiatan berdasarkan kategori
    fun getTasksByCategory(category: String): ArrayList<TaskItem> {

        val taskList = ArrayList<TaskItem>()

        val cursor = readableDatabase.query(
            TABLE_TASK,
            null,
            "$COLUMN_CATEGORY = ?",
            arrayOf(category),
            null,
            null,
            "$COLUMN_ID DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                taskList.add(
                    TaskItem(
                        id = it.getInt(
                            it.getColumnIndexOrThrow(COLUMN_ID)
                        ),
                        task = it.getString(
                            it.getColumnIndexOrThrow(COLUMN_TASK)
                        ),
                        category = it.getString(
                            it.getColumnIndexOrThrow(COLUMN_CATEGORY)
                        ),
                        dueDate = it.getString(
                            it.getColumnIndexOrThrow(COLUMN_DATE)
                        ),
                        isCompleted = it.getInt(
                            it.getColumnIndexOrThrow(COLUMN_COMPLETED)
                        ) == 1
                    )
                )
            }
        }

        return taskList
    }
}
