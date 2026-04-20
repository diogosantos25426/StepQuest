package com.example.stepquest

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.stepquest.data.AppDatabase
import com.example.stepquest.data.SessionManager
import kotlinx.coroutines.launch

class MapActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var stepsToday: Int = 0
    private var lastUnlockedLevel: Int = 0
    private var userId: Int = -1

    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager

    private lateinit var btnLevel1: ImageButton
    private lateinit var btnLevel2: ImageButton
    private lateinit var btnLevel3: ImageButton
    private lateinit var pbSteps: ProgressBar
    private lateinit var tvStepCount: TextView

    // Nível 1 desbloqueado por defeito (0 passos) para testes
    private val DEBUG_STEP_LIMIT = 0 
    private val LEVEL_2_LIMIT = 500
    private val LEVEL_3_LIMIT = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        db = AppDatabase.getDatabase(this)
        sessionManager = SessionManager(this)
        userId = sessionManager.getUserId()

        btnLevel1 = findViewById(R.id.btn_level_1)
        btnLevel2 = findViewById(R.id.btn_level_2)
        btnLevel3 = findViewById(R.id.btn_level_3)
        pbSteps = findViewById(R.id.pb_steps)
        tvStepCount = findViewById(R.id.tv_step_count)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        checkPermissions()
        loadProgress()
        setupListeners()
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 100)
            }
        }
    }

    private fun loadProgress() {
        lifecycleScope.launch {
            lastUnlockedLevel = db.userDao().getLastUnlockedLevel(userId)
            updateMapUI()
        }
    }

    private fun setupListeners() {
        btnLevel1.setOnClickListener { handleLevelClick(1, DEBUG_STEP_LIMIT) }
        btnLevel2.setOnClickListener { handleLevelClick(2, LEVEL_2_LIMIT) }
        btnLevel3.setOnClickListener { handleLevelClick(3, LEVEL_3_LIMIT) }
    }

    private fun handleLevelClick(level: Int, requiredSteps: Int) {
        if (stepsToday >= requiredSteps || lastUnlockedLevel >= level) {
            val intent = if (level == 1) {
                // Ir direto para a batalha se for o Nível 1
                Intent(this, BattleActivity::class.java)
            } else {
                Intent(this, LevelActivity::class.java)
            }
            intent.putExtra("LEVEL_ID", level)
            startActivity(intent)
        } else {
            val missing = requiredSteps - stepsToday
            Toast.makeText(this, "Ainda te faltam $missing passos!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMapUI() {
        // Update Progress Bar
        pbSteps.progress = stepsToday
        tvStepCount.text = "$stepsToday / 1000"

        // Update Level 1
        updatePin(btnLevel1, 1, DEBUG_STEP_LIMIT)
        // Update Level 2
        updatePin(btnLevel2, 2, LEVEL_2_LIMIT)
        // Update Level 3
        updatePin(btnLevel3, 3, LEVEL_3_LIMIT)
    }

    private fun updatePin(button: ImageButton, level: Int, meta: Int) {
        when {
            lastUnlockedLevel > level -> button.setImageResource(R.drawable.pin_concluido)
            stepsToday >= meta -> {
                button.setImageResource(R.drawable.pin_disponivel)
                if (lastUnlockedLevel < level) {
                    saveUnlock(level)
                }
            }
            else -> button.setImageResource(R.drawable.pin_bloqueado)
        }
    }

    private fun saveUnlock(level: Int) {
        lifecycleScope.launch {
            db.userDao().updateLastUnlockedLevel(userId, level)
            lastUnlockedLevel = level
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            stepsToday = event.values[0].toInt() % 1001
            updateMapUI()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        stepSensor?.also {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}
