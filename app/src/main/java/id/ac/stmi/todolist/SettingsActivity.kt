
package id.ac.stmi.todolist

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Menerapkan tema aplikasi
        ThemeHelper.apply(this)

        val rbLight = findViewById<RadioButton>(R.id.rbLight)
        val rbDark = findViewById<RadioButton>(R.id.rbDark)
        val btnSave = findViewById<Button>(R.id.btnSaveTheme)
        val btnBack = findViewById<Button>(R.id.btnSettingsBack)

        val preferences = getSharedPreferences(
            "AppSettings",
            MODE_PRIVATE
        )

        // Mengambil tema yang sebelumnya disimpan
        val savedTheme = preferences.getString(
            "theme",
            "Light Mode"
        )

        // Menampilkan pilihan tema yang sedang digunakan
        if (savedTheme == "Dark Mode") {
            rbDark.isChecked = true
        } else {
            rbLight.isChecked = true
        }

        // Menyimpan pilihan tema
        btnSave.setOnClickListener {

            val selectedTheme = if (rbDark.isChecked) {
                "Dark Mode"
            } else {
                "Light Mode"
            }

            preferences.edit()
                .putString("theme", selectedTheme)
                .apply()

            setResult(RESULT_OK)
            finish()
        }

        // Kembali ke halaman sebelumnya
        btnBack.setOnClickListener {
            finish()
        }
    }
}
