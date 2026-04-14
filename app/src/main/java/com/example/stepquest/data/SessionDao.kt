package com.example.stepquest.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SessionDao {
    @Insert
    suspend fun insertSession(session: WalkingSession)

    @Query("SELECT * FROM walking_sessions WHERE userId = :userId ORDER BY date DESC")
    suspend fun getSessionsByUser(userId: Int): List<WalkingSession>
}