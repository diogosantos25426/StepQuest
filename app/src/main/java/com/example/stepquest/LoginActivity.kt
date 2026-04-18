package com.example.stepquest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.stepquest.data.AppDatabase
import com.example.stepquest.data.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = AppDatabase.getDatabase(this)
        sessionManager = SessionManager(this)

        // Se já estiver logado, vai para o Menu
        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, MenuActivity::class.java))
            finish()
        }

        val etUsername = findViewById<EditText>(R.id.et_username)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val tvRegister = findViewById<TextView>(R.id.btn_register)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch {
                    val user = db.userDao().getUser(username, password)
                    if (user != null) {
                        sessionManager.saveUserSession(user.id, user.username)
                        
                        // Verificar se é necessário popular itens iniciais
                        val items = db.userDao().getUserItems(user.id)
                        if (items.isEmpty()) {
                            db.populateInitialItems(user.id)
                        }

                        Toast.makeText(this@LoginActivity, "Bem-vindo, ${user.username}!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MenuActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Credenciais inválidas", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}