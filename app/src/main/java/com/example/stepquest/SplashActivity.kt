package com.example.stepquest

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.stepquest.data.SessionManager

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val sessionManager = SessionManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            val targetActivity = if (sessionManager.getUserId() != -1) {
                MenuActivity::class.java
            } else {
                LoginActivity::class.java
            }
            
            startActivity(Intent(this, targetActivity))
            finish()
        }, 3000)
    }
}