package com.example.stepquest

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class BattleActivity : AppCompatActivity() {

    private lateinit var ivEnemy: ImageView
    private lateinit var ivHero: ImageView
    private lateinit var pbEnemyHp: ProgressBar
    private lateinit var tvEnemyHp: TextView
    private lateinit var pbHeroEnergy: ProgressBar
    private lateinit var tvHeroEnergy: TextView
    private lateinit var pbHeroStamina: ProgressBar
    private lateinit var tvHeroStamina: TextView

    private var enemyHp = 100
    private var heroEnergy = 100
    private var heroStamina = 100

    private val handler = Handler(Looper.getMainLooper())
    
    private lateinit var heroIdleFrame: Bitmap
    private lateinit var heroAttackFrames: List<Bitmap>
    private lateinit var enemyIdleFrame: Bitmap
    private lateinit var enemyAttackFrames: List<Bitmap>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battle)

        // Inicializar Views
        ivEnemy = findViewById(R.id.iv_enemy)
        ivHero = findViewById(R.id.iv_hero)
        pbEnemyHp = findViewById(R.id.pb_enemy_hp)
        tvEnemyHp = findViewById(R.id.tv_enemy_hp)
        pbHeroEnergy = findViewById(R.id.pb_hero_energy)
        tvHeroEnergy = findViewById(R.id.tv_hero_energy)
        pbHeroStamina = findViewById(R.id.pb_hero_stamina)
        tvHeroStamina = findViewById(R.id.tv_hero_stamina)

        val btnAttack = findViewById<View>(R.id.btn_attack)
        val btnInventory = findViewById<View>(R.id.btn_inventory)
        val btnFlee = findViewById<View>(R.id.btn_flee)

        loadSprites()

        btnAttack.setOnClickListener {
            performHeroAttack()
        }

        btnInventory.setOnClickListener {
            Toast.makeText(this, "A abrir inventário de combate...", Toast.LENGTH_SHORT).show()
        }

        btnFlee.setOnClickListener {
            finish()
        }
    }

    private fun loadSprites() {
        val options = BitmapFactory.Options().apply { inScaled = false }
        
        // Carregar herói (3 frames: 0=Idle, 1-2=Attack)
        val heroSheet = BitmapFactory.decodeResource(resources, R.drawable.spritesheet_heroicombate, options)
        if (heroSheet != null) {
            val w = heroSheet.width / 3
            val h = heroSheet.height
            heroIdleFrame = Bitmap.createBitmap(heroSheet, 0, 0, w, h)
            heroAttackFrames = listOf(
                Bitmap.createBitmap(heroSheet, w, 0, w, h),
                Bitmap.createBitmap(heroSheet, 2 * w, 0, w, h)
            )
            ivHero.setImageBitmap(heroIdleFrame)
        }

        // Carregar inimigo (3 frames: 0=Idle, 1-2=Attack)
        val enemySheet = BitmapFactory.decodeResource(resources, R.drawable.spritesheet_inimigo1, options)
        if (enemySheet != null) {
            val w = enemySheet.width / 3
            val h = enemySheet.height
            enemyIdleFrame = Bitmap.createBitmap(enemySheet, 0, 0, w, h)
            enemyAttackFrames = listOf(
                Bitmap.createBitmap(enemySheet, w, 0, w, h),
                Bitmap.createBitmap(enemySheet, 2 * w, 0, w, h)
            )
            ivEnemy.setImageBitmap(enemyIdleFrame)
        }
    }

    private fun performHeroAttack() {
        // Bloquear botões durante animação
        findViewById<View>(R.id.btn_attack).isEnabled = false

        animateAttack(ivHero, heroAttackFrames, heroIdleFrame) {
            // Lógica de dano ao inimigo
            enemyHp -= 20
            if (enemyHp < 0) enemyHp = 0
            updateStatus()

            if (enemyHp <= 0) {
                showVictoryDialog()
            } else {
                // Contra-ataque do inimigo após 1 segundo
                handler.postDelayed({
                    performEnemyAttack()
                }, 1000)
            }
        }
    }

    private fun performEnemyAttack() {
        animateAttack(ivEnemy, enemyAttackFrames, enemyIdleFrame) {
            // Lógica de dano ao herói (reduz energia)
            heroEnergy -= 15
            if (heroEnergy < 0) heroEnergy = 0
            updateStatus()

            findViewById<View>(R.id.btn_attack).isEnabled = true
            
            if (heroEnergy <= 0) {
                Toast.makeText(this, "Ficaste sem energia!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun animateAttack(imageView: ImageView, attackFrames: List<Bitmap>, idleFrame: Bitmap, onComplete: () -> Unit) {
        // Frame 1
        imageView.setImageBitmap(attackFrames[0])
        
        handler.postDelayed({
            // Frame 2
            imageView.setImageBitmap(attackFrames[1])
            
            handler.postDelayed({
                // Volta ao Idle
                imageView.setImageBitmap(idleFrame)
                onComplete()
            }, 200)
        }, 200)
    }

    private fun updateStatus() {
        pbEnemyHp.progress = enemyHp
        tvEnemyHp.text = "$enemyHp/100"

        pbHeroEnergy.progress = heroEnergy
        tvHeroEnergy.text = "$heroEnergy/100"
        
        pbHeroStamina.progress = heroStamina
        tvHeroStamina.text = "$heroStamina/100"
    }

    private fun showVictoryDialog() {
        AlertDialog.Builder(this)
            .setTitle("Vitória!")
            .setMessage("Derrotaste o inimigo e ganhaste XP!")
            .setPositiveButton("Continuar") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
}