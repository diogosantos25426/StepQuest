package com.example.stepquest.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val nome: String,
    val descricao: String,
    val tipo: String, // 'CURA', 'DANO', 'ARMA'
    val valor: Int,
    val quantidade: Int,
    val imagemRes: String // Nome do recurso drawable
)