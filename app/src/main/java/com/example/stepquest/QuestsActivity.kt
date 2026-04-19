package com.example.stepquest

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stepquest.data.AppDatabase
import com.example.stepquest.data.Quest
import kotlinx.coroutines.launch

class QuestsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: QuestAdapter
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quests)

        db = AppDatabase.getDatabase(this)
        
        recyclerView = findViewById(R.id.rv_quests)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        adapter = QuestAdapter(emptyList())
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.btn_back_quests).setOnClickListener {
            finish()
        }

        loadQuests()
    }

    private fun loadQuests() {
        lifecycleScope.launch {
            // Se não houver missões, criar as iniciais para teste
            var quests = db.questDao().getAllQuests()
            if (quests.isEmpty()) {
                val initialQuests = listOf(
                    Quest(titulo = "Caminhada Matinal", descricao = "Dar 2000 passos", tipo = "PASSOS", objetivoQuantidade = 2000, recompensaXP = 50, recompensaItemNome = "Poção de Vida"),
                    Quest(titulo = "Banho de Sol", descricao = "Detetar luz solar intensa", tipo = "LUZ", objetivoQuantidade = 300, recompensaXP = 100),
                    Quest(titulo = "Primeiro Sangue", descricao = "Derrotar 1 inimigo", tipo = "COMBATE", objetivoQuantidade = 1, recompensaXP = 20, recompensaItemNome = "Bomba")
                )
                initialQuests.forEach { db.questDao().insertQuest(it) }
                quests = db.questDao().getAllQuests()
            }
            adapter.updateQuests(quests)
        }
    }
}