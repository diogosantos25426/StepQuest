package com.example.stepquest.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quests")
data class Quest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val descricao: String,
    val tipo: String, // 'PASSOS', 'LUZ', 'COMBATE'
    val objetivoQuantidade: Int,
    var progressoAtual: Int = 0,
    val recompensaItemNome: String? = null, // Facilitar usando nome em vez de ID por agora
    val recompensaXP: Int,
    var isConcluida: Boolean = false
)