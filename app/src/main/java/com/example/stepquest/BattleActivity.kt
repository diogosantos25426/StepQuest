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
import androidx.lifecycle.lifecycleScope
import com.example.stepquest.data.AppDatabase
import com.example.stepquest.data.Item
import com.example.stepquest.data.SessionManager
import kotlinx.coroutines.launch
import kotlin.random.Random

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

    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battle)

        db = AppDatabase.getDatabase(this)
        sessionManager = SessionManager(this)
        userId = sessionManager.getUserId()

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
            val bottomSheet = InventoryBottomSheet { item ->
                handleItemUse(item)
            }
            bottomSheet.show(supportFragmentManager, "InventoryBottomSheet")
        }

        btnFlee.setOnClickListener {
            finish()
        }
    }

    private fun handleItemUse(item: Item) {
        findViewById<View>(R.id.btn_attack).isEnabled = false
        
        when (item.tipo) {
            "CURA" -> {
                heroEnergy += item.valor
                if (heroEnergy > 100) heroEnergy = 100
                Toast.makeText(this, "Usaste ${item.nome}! +${item.valor} HP", Toast.LENGTH_SHORT).show()
                updateStatus()
                // Após curar, o inimigo ataca
                handler.postDelayed({ performEnemyAttack() }, 1000)
            }
            "DANO" -> {
                enemyHp -= item.valor
                if (enemyHp < 0) enemyHp = 0
                Toast.makeText(this, "Usaste ${item.nome}! -${item.valor} HP ao inimigo", Toast.LENGTH_SHORT).show()
                updateStatus()
                
                if (enemyHp <= 0) {
                    concederRecompensas()
                } else {
                    handler.postDelayed({ performEnemyAttack() }, 1000)
                }
            }
            else -> {
                findViewById<View>(R.id.btn_attack).isEnabled = true
            }
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
        findViewById<View>(R.id.btn_attack).isEnabled = false

        animateAttack(ivHero, heroAttackFrames, heroIdleFrame) {
            enemyHp -= 20
            if (enemyHp < 0) enemyHp = 0
            updateStatus()

            if (enemyHp <= 0) {
                concederRecompensas()
            } else {
                handler.postDelayed({
                    performEnemyAttack()
                }, 1000)
            }
        }
    }

    private fun performEnemyAttack() {
        animateAttack(ivEnemy, enemyAttackFrames, enemyIdleFrame) {
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
        imageView.setImageBitmap(attackFrames[0])
        handler.postDelayed({
            imageView.setImageBitmap(attackFrames[1])
            handler.postDelayed({
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

    private fun concederRecompensas() {
        val xpGanho = Random.nextInt(20, 51)
        var itemGanho: String? = null
        
        lifecycleScope.launch {
            db.userDao().addXp(userId, xpGanho)
            
            if (Random.nextFloat() <= 0.3f) {
                val potion = db.userDao().getItemByName(userId, "Poção de Vida")
                if (potion != null) {
                    db.userDao().updateItemQuantity(potion.id, potion.quantidade + 1)
                } else {
                    db.userDao().insertItem(Item(userId = userId, nome = "Poção de Vida", descricao = "Cura 30 HP", tipo = "CURA", valor = 30, quantidade = 1, imagemRes = "inventario"))
                }
                itemGanho = "Poção de Vida"
            }

            val message = StringBuilder("Ganhaste $xpGanho XP!")
            if (itemGanho != null) {
                message.append("\nEncontraste um item: $itemGanho!")
            }

            AlertDialog.Builder(this@BattleActivity)
                .setTitle("Vitória!")
                .setMessage(message.toString())
                .setPositiveButton("Incrível!") { _, _ ->
                    finish()
                }
                .setCancelable(false)
                .show()
        }
    }
}