package com.example.stepquest

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

        findViewById<Button>(R.id.btn_back_to_map).setOnClickListener {
            finish()
        }
    }
}