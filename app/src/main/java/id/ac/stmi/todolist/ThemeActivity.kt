package id.ac.stmi.todolist

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast

class ThemeActivity : Activity() {

    private lateinit var radioGroupTheme: RadioGroup
    private lateinit var radioLight: RadioButton
    private lateinit var radioDark: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme)

        radioGroupTheme = findViewById(R.id.radioGroupTheme)
        radioLight = findViewById(R.id.radioLight)
        radioDark = findViewById(R.id.radioDark)

        val btnSaveTheme = findViewById<Button>(R.id.btnSaveTheme)
        val btnBackTheme = findViewById<Button>(R.id.btnBackTheme)

        // Menampilkan tema yang sebelumnya dipilih
        val currentTheme = ThemeManager.getTheme(this)

        if (currentTheme == "Dark") {
            radioDark.isChecked = true
        } else {
            radioLight.isChecked = true
        }

        // Menyimpan pilihan tema
        btnSaveTheme.setOnClickListener {

            val selectedTheme = when (radioGroupTheme.checkedRadioButtonId) {
                R.id.radioLight -> "Light"
                R.id.radioDark -> "Dark"
                else -> "Light"
            }

            ThemeManager.saveTheme(this, selectedTheme)

            Toast.makeText(
                this,
                "$selectedTheme Mode berhasil disimpan",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }

        btnBackTheme.setOnClickListener {
            finish()
        }
    }
}