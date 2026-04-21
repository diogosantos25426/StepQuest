package com.example.stepquest

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.stepquest.data.AppDatabase
import com.example.stepquest.data.SessionManager
import kotlinx.coroutines.launch

class MenuActivity : AppCompatActivity() {

    private lateinit var heroImageView: ImageView
    private lateinit var tvSteps: TextView
    private lateinit var pbEnergy: ProgressBar
    private lateinit var pbXp: ProgressBar
    
    private val handler = Handler(Looper.getMainLooper())
    private var currentFrame = 0
    private val frameCount = 6
    private lateinit var frames: List<Bitmap>

    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        db = AppDatabase.getDatabase(this)
        sessionManager = SessionManager(this)
        userId = sessionManager.getUserId()

        // Initialize UI Components
        heroImageView = findViewById(R.id.iv_hero_walk)
        tvSteps = findViewById(R.id.tv_steps_dashboard)
        pbEnergy = findViewById(R.id.pb_energy)
        pbXp = findViewById(R.id.pb_xp)

        val btnInfo = findViewById<View>(R.id.btn_info)
        val btnMap = findViewById<View>(R.id.btn_nav_map)
        val btnInventory = findViewById<View>(R.id.btn_nav_inventory)
        val btnQuests = findViewById<View>(R.id.btn_nav_quests)
        val btnStats = findViewById<View>(R.id.btn_nav_stats)

        // Navigation Logic
        btnMap.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        btnQuests.setOnClickListener {
            startActivity(Intent(this, QuestsActivity::class.java))
        }

        btnInventory.setOnClickListener {
            startActivity(Intent(this, InventoryActivity::class.java))
        }

        val soonMessage = { name: String -> 
            Toast.makeText(this, "$name (Brevemente)", Toast.LENGTH_SHORT).show()
        }

        btnStats.setOnClickListener { soonMessage("Estatísticas") }
        
        // Custom Info Dialog
        btnInfo.setOnClickListener {
            showCustomInfoDialog()
        }

        setupHeroAnimation()
    }

    override fun onResume() {
        super.onResume()
        updateRealData()
    }

    private fun showCustomInfoDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_info, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btn_dialog_ok).setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun updateRealData() {
        lifecycleScope.launch {
            val user = db.userDao().getUserById(userId)
            if (user != null) {
                pbXp.progress = user.xpTotal % 100 // Exemplo: nível a cada 100 XP
                pbEnergy.progress = 100 // Pode ser ligado a uma lógica real depois
                
                val totalSteps = db.userDao().getTotalSteps(userId) ?: 0
                val meta = 5000
                tvSteps.text = "Passos: $totalSteps / Meta: $meta"
            }
        }
    }

    private fun setupHeroAnimation() {
        val options = BitmapFactory.Options().apply { inScaled = false }
        val spriteSheet = BitmapFactory.decodeResource(resources, R.drawable.spritesheet_heroi, options)

        if (spriteSheet != null) {
            val frameWidth = spriteSheet.width / frameCount
            val frameHeight = spriteSheet.height
            
            try {
                frames = List(frameCount) { i ->
                    Bitmap.createBitmap(spriteSheet, i * frameWidth, 0, frameWidth, frameHeight)
                }
                heroImageView.scaleType = ImageView.ScaleType.FIT_CENTER
                startAnimation()
            } catch (e: Exception) {
                heroImageView.setImageResource(R.drawable.spritesheet_heroi)
            }
        }
    }

    private val animationRunnable = object : Runnable {
        override fun run() {
            if (::frames.isInitialized && frames.isNotEmpty()) {
                heroImageView.setImageBitmap(frames[currentFrame])
                currentFrame = (currentFrame + 1) % frames.size
                handler.postDelayed(this, 130)
            }
        }
    }

    private fun startAnimation() {
        handler.post(animationRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(animationRunnable)
    }
}
