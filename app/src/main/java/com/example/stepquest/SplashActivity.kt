package com.example.stepquest

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.stepquest.data.SessionManager

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Instala o Splash Screen ANTES do super.onCreate()
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        // Não é estritamente necessário ter um layout se o Splash API tratar de tudo,
        // mas mantemos para compatibilidade se houver lógica visual extra.
        setContentView(R.layout.activity_splash)

        val sessionManager = SessionManager(this)

        // Simula um carregamento ou espera antes de decidir a próxima tela
        Handler(Looper.getMainLooper()).postDelayed({
            val targetActivity = if (sessionManager.getUserId() != -1) {
                MenuActivity::class.java
            } else {
                LoginActivity::class.java
            }
            
            startActivity(Intent(this, targetActivity))
            finish()
        }, 2000) // Reduzi para 2s para ser mais fluido
    }
}