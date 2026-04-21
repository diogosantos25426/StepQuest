package com.example.stepquest

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stepquest.data.AppDatabase
import com.example.stepquest.data.Item
import com.example.stepquest.data.SessionManager
import kotlinx.coroutines.launch

class InventoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: InventoryAdapter
    private lateinit var db: AppDatabase
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory)

        db = AppDatabase.getDatabase(this)
        sessionManager = SessionManager(this)
        val userId = sessionManager.getUserId()

        recyclerView = findViewById(R.id.rv_full_inventory)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        
        adapter = InventoryAdapter(emptyList()) { item ->
            // No ecrã de inventário fora da batalha, podemos mostrar info do item
        }
        recyclerView.adapter = adapter

        findViewById<ImageButton>(R.id.btn_back_inventory).setOnClickListener {
            finish()
        }

        loadItems(userId)
    }

    private fun loadItems(userId: Int) {
        lifecycleScope.launch {
            var items = db.userDao().getUserItems(userId)
            
            // Inserir itens de demonstração se o inventário estiver vazio
            if (items.isEmpty()) {
                val demoItems = listOf(
                    Item(userId = userId, nome = "Espada de Aço", descricao = "Uma lâmina robusta de cavaleiro.", tipo = "ARMA", valor = 20, quantidade = 1, imagemRes = "item_sword"),
                    Item(userId = userId, nome = "Poção de Vida", descricao = "Recupera 50 HP instantaneamente.", tipo = "CURA", valor = 10, quantidade = 5, imagemRes = "item_potion"),
                    Item(userId = userId, nome = "Bomba de Fogo", descricao = "Causa 40 de dano em área.", tipo = "DANO", valor = 15, quantidade = 3, imagemRes = "item_bomb"),
                    Item(userId = userId, nome = "Escudo de Madeira", descricao = "Defesa básica.", tipo = "DEFESA", valor = 15, quantidade = 1, imagemRes = "inventario"),
                    Item(userId = userId, nome = "Pergaminho Antigo", descricao = "Contém segredos do castelo.", tipo = "QUEST", valor = 0, quantidade = 1, imagemRes = "popup_bg")
                )
                demoItems.forEach { db.userDao().insertItem(it) }
                items = db.userDao().getUserItems(userId)
            }

            adapter.updateItems(items)
        }
    }
}