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
import kotlin.math.sqrt

class MapActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var stepDetector: Sensor? = null
    private var lightSensor: Sensor? = null
    private var accelerometer: Sensor? = null
    
    private var stepsToday: Int = 0
    private var initialStepCount: Int = -1
    private var lastUnlockedLevel: Int = 0
    private var userId: Int = -1

    // Variáveis para o algoritmo do acelerómetro
    private var magnitudeAnterior = 0f
    private var threshold = 12f 
    private var lastStepTime: Long = 0

    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager

    private lateinit var btnLevel1: ImageButton
    private lateinit var btnLevel2: ImageButton
    private lateinit var btnLevel3: ImageButton
    private lateinit var pbSteps: ProgressBar
    private lateinit var tvStepCount: TextView

    private val LEVEL_1_LIMIT = 0 
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
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

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
        btnLevel1.setOnClickListener { handleLevelClick(1, LEVEL_1_LIMIT) }
        btnLevel2.setOnClickListener { handleLevelClick(2, LEVEL_2_LIMIT) }
        btnLevel3.setOnClickListener { handleLevelClick(3, LEVEL_3_LIMIT) }
        
        findViewById<ImageButton>(R.id.btn_quests)?.setOnClickListener {
            startActivity(Intent(this, QuestsActivity::class.java))
        }
    }

    private fun handleLevelClick(level: Int, requiredSteps: Int) {
        if (stepsToday >= requiredSteps || lastUnlockedLevel >= level) {
            val intent = if (level == 1) {
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
        pbSteps.progress = stepsToday
        tvStepCount.text = "$stepsToday / 1000"

        updatePin(btnLevel1, 1, LEVEL_1_LIMIT)
        updatePin(btnLevel2, 2, LEVEL_2_LIMIT)
        updatePin(btnLevel3, 3, LEVEL_3_LIMIT)
    }

    private fun updatePin(button: ImageButton, level: Int, meta: Int) {
        when {
            lastUnlockedLevel >= level -> button.setImageResource(R.drawable.pin_concluido)
            stepsToday >= meta -> {
                button.setImageResource(R.drawable.pin_disponivel)
            }
            else -> button.setImageResource(R.drawable.pin_bloqueado)
        }
    }

    private fun onStep() {
        stepsToday++
        updateMapUI()
        
        // Atualizar quests de passos em tempo real
        lifecycleScope.launch {
            val activeQuests = db.questDao().getActiveQuestsByType("PASSOS")
            activeQuests.forEach { quest ->
                val novoProgresso = quest.progressoAtual + 1
                if (novoProgresso >= quest.objetivoQuantidade) {
                    db.questDao().updateQuest(quest.copy(progressoAtual = novoProgresso, isConcluida = true))
                    db.userDao().addXp(userId, quest.recompensaXP)
                    Toast.makeText(this@MapActivity, "Missão Cumprida: ${quest.titulo}!", Toast.LENGTH_SHORT).show()
                } else {
                    db.questDao().updateQuest(quest.copy(progressoAtual = novoProgresso))
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_STEP_DETECTOR -> onStep()
            Sensor.TYPE_STEP_COUNTER -> {
                val totalSteps = event.values[0].toInt()
                if (initialStepCount == -1) {
                    initialStepCount = totalSteps
                }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val delta = magnitude - magnitudeAnterior
                magnitudeAnterior = magnitude
                if (delta > threshold) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastStepTime > 300) {
                        onStep()
                        lastStepTime = currentTime
                    }
                }
            }
            Sensor.TYPE_LIGHT -> {
                val lux = event.values[0]
                checkLightQuest(lux)
            }
        }
    }

    private fun checkLightQuest(lux: Float) {
        lifecycleScope.launch {
            val activeQuests = db.questDao().getActiveQuestsByType("LUZ")
            activeQuests.forEach { quest ->
                if (lux >= quest.objetivoQuantidade) {
                    db.questDao().updateQuest(quest.copy(isConcluida = true))
                    db.userDao().addXp(userId, quest.recompensaXP)
                    Toast.makeText(this@MapActivity, "Missão Cumprida: ${quest.titulo}!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onResume() {
        super.onResume()
        stepSensor?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST) }
        stepDetector?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST) }
        accelerometer?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        lightSensor?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }
}
