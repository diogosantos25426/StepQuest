package com.example.stepquest

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.stepquest.data.AppDatabase
import com.example.stepquest.data.SessionManager
import com.example.stepquest.data.User
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvTotalSteps: TextView
    private lateinit var btnSave: Button
    
    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        db = AppDatabase.getDatabase(this)
        sessionManager = SessionManager(this)
        
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        tvTotalSteps = findViewById(R.id.tvTotalSteps)
        btnSave = findViewById(R.id.btnSave)

        loadUserData()

        btnSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun loadUserData() {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            finish()
            return
        }

        lifecycleScope.launch {
            currentUser = db.userDao().getUserById(userId)
            val totalSteps = db.userDao().getTotalSteps(userId) ?: 0
            
            currentUser?.let {
                etUsername.setText(it.username)
                etPassword.setText(it.password)
                tvTotalSteps.text = "Passos Totais: $totalSteps"
            }
        }
    }

    private fun saveChanges() {
        val newUsername = etUsername.text.toString().trim()
        val newPassword = etPassword.text.toString().trim()

        if (newUsername.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            currentUser?.let {
                val updatedUser = it.copy(username = newUsername, password = newPassword)
                db.userDao().updateUser(updatedUser)
                Toast.makeText(this@ProfileActivity, "Dados do herói atualizados!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}