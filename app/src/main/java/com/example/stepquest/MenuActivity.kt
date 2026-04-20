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

class MenuActivity : AppCompatActivity() {

    private lateinit var heroImageView: ImageView
    private lateinit var tvSteps: TextView
    private lateinit var pbEnergy: ProgressBar
    private lateinit var pbXp: ProgressBar
    
    private val handler = Handler(Looper.getMainLooper())
    private var currentFrame = 0
    private val frameCount = 3
    private lateinit var frames: List<Bitmap>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

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

        // Set Placeholder Data
        setupPlaceholderData()

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

    private fun showCustomInfoDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_info, null)
        val dialog = AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setView(dialogView)
            .create()

        dialogView.findViewById<Button>(R.id.btn_dialog_ok).setOnClickListener {
            dialog.dismiss()
        }

        // Se quiseres mudar a fonte via código caso adiciones um .ttf:
        // val medievalFont = ResourcesCompat.getFont(this, R.font.tua_fonte)
        // dialogView.findViewById<TextView>(R.id.tv_dialog_title).typeface = medievalFont

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun setupPlaceholderData() {
        pbEnergy.progress = 80
        pbXp.progress = 40
        tvSteps.text = "Passos: 1200 / Meta: 5000"
    }

    private fun setupHeroAnimation() {
        val options = BitmapFactory.Options().apply { inScaled = false }
        val spriteSheet = BitmapFactory.decodeResource(resources, R.drawable.spritesheet_heroi, options)

        if (spriteSheet != null) {
            val actualFrameCount = if (spriteSheet.width > spriteSheet.height) 2 else 1
            val frameWidth = spriteSheet.width / actualFrameCount
            val frameHeight = spriteSheet.height
            
            try {
                frames = List(actualFrameCount) { i ->
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
                handler.postDelayed(this, 300)
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