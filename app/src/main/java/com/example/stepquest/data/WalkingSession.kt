package com.example.stepquest.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "walking_sessions",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WalkingSession(
    @PrimaryKey(autoGenerate = true) val sessionId: Int = 0,
    val userId: Int,
    val steps: Int,
    val distance: Double,
    val date: Long
)