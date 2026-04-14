package com.example.stepquest.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val password: String,
    @ColumnInfo(name = "xp_total") val xpTotal: Int = 0,
    @ColumnInfo(name = "last_unlocked_level") val lastUnlockedLevel: Int = 0
)