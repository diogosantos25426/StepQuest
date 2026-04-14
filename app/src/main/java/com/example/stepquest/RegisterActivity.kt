package com.example.stepquest

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.stepquest.data.AppDatabase
import com.example.stepquest.data.User
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        db = AppDatabase.getDatabase(this)

        val etUsername = findViewById<EditText>(R.id.et_register_username)
        val etPassword = findViewById<EditText>(R.id.et_register_password)
        val btnRegister = findViewById<Button>(R.id.btn_do_register)
        val tvBackToLogin = findViewById<TextView>(R.id.tv_back_to_login)

        btnRegister.setOnClickListener {
            val userStr = etUsername.text.toString()
            val passStr = etPassword.text.toString()

            if (userStr.isNotEmpty() && passStr.isNotEmpty()) {
                lifecycleScope.launch {
                    val exists = db.userDao().checkUserExists(userStr)
                    if (!exists) {
                        val newUser = User(username = userStr, password = passStr)
                        db.userDao().registerUser(newUser)
                        Toast.makeText(this@RegisterActivity, "Account created!", Toast.LENGTH_SHORT).show()
                        finish() // Volta para o login
                    } else {
                        Toast.makeText(this@RegisterActivity, "User already exists", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        tvBackToLogin.setOnClickListener {
            finish()
        }
    }
}