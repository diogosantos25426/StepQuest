package com.example.stepquest

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.stepquest.data.AppDatabase
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
            val items = db.userDao().getUserItems(userId)
            adapter.updateItems(items)
        }
    }
}