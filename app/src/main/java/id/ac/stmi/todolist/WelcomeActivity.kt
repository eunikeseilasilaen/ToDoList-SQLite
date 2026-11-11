
package id.ac.stmi.todolist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button

class WelcomeActivity : Activity() {

    private lateinit var btnGetStarted: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_welcome)

        // Menerapkan tema aplikasi
        ThemeHelper.apply(this)

        // Mengatur warna mini card
        ThemeHelper.applyWelcomeReminder(this)

        // Menghubungkan tombol Get Started
        btnGetStarted = findViewById(R.id.btnGetStarted)

        btnGetStarted.setOnClickListener {
            val intent = Intent(
                this,
                ProfileActivity::class.java
            )

            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        ThemeHelper.apply(this)
        ThemeHelper.applyWelcomeReminder(this)
    }
}
