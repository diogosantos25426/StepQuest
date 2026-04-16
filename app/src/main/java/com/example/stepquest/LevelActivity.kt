package com.example.stepquest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LevelActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level)

        val levelId = intent.getIntExtra("LEVEL_ID", 1)
        val tvTitle = findViewById<TextView>(R.id.tv_level_title)
        tvTitle.text = "Level $levelId"

        // Botão para iniciar a batalha no Nível 1
        if (levelId == 1) {
            val btnStartBattle = Button(this).apply {
                text = "Entrar em Combate"
                setOnClickListener {
                    startActivity(Intent(this@LevelActivity, BattleActivity::class.java))
                }
            }
            // Adicionar o botão programaticamente ou substituir se o layout for simples
            (findViewById<Button>(R.id.btn_back_to_map).parent as android.view.ViewGroup).addView(btnStartBattle, 0)
        }

        findViewById<Button>(R.id.btn_back_to_map).setOnClickListener {
            finish()
        }
    }
}