
package id.ac.stmi.todolist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class ProfileActivity : Activity() {

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        ThemeHelper.apply(this)

        dbHelper = DBHelper(this)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etAddress = findViewById<EditText>(R.id.etAddress)
        val btnContinue = findViewById<Button>(R.id.btnContinue)

        val profile = dbHelper.getProfile()

        if (profile != null) {
            etName.setText(profile.name)
            etEmail.setText(profile.email)
            etPhone.setText(profile.phone)
            etAddress.setText(profile.address)
        }

        btnContinue.setOnClickListener {

            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val address = etAddress.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() ||
                phone.isEmpty() || address.isEmpty()
            ) {
                Toast.makeText(
                    this,
                    "Please complete all profile fields",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Invalid email address"
                return@setOnClickListener
            }

            dbHelper.saveProfile(name, email, phone, address)

            startActivity(
                Intent(this, MainActivity::class.java)
            )

            finish()
        }
    }

    override fun onDestroy() {
        if (::dbHelper.isInitialized) {
            dbHelper.close()
        }
        super.onDestroy()
    }
}
