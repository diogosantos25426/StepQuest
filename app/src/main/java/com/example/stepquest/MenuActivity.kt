package com.example.stepquest

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    private lateinit var heroImageView: ImageView
    private val handler = Handler(Looper.getMainLooper())
    private var currentFrame = 0
    private val frameCount = 2
    private lateinit var frames: List<Bitmap>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        heroImageView = findViewById(R.id.hero_animation)
        val btnPlay = findViewById<Button>(R.id.btn_play)

        btnPlay.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        setupHeroAnimation()
    }

    private fun setupHeroAnimation() {
        val options = BitmapFactory.Options().apply { inScaled = false }
        val spriteSheet = BitmapFactory.decodeResource(resources, R.drawable.spritesheet_heroi, options)

        if (spriteSheet != null) {
            val frameWidth = spriteSheet.width / frameCount
            val frameHeight = spriteSheet.height
            
            frames = List(frameCount) { i ->
                Bitmap.createBitmap(spriteSheet, i * frameWidth, 0, frameWidth, frameHeight)
            }

            heroImageView.scaleType = ImageView.ScaleType.FIT_CENTER
            startAnimation()
        }
    }

    private val animationRunnable = object : Runnable {
        override fun run() {
            if (::frames.isInitialized && frames.isNotEmpty()) {
                heroImageView.setImageBitmap(frames[currentFrame])
                currentFrame = (currentFrame + 1) % frameCount
                handler.postDelayed(this, 180)
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