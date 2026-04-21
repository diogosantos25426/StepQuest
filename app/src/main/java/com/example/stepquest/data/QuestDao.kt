package com.example.stepquest.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface QuestDao {
    @Query("SELECT * FROM quests WHERE isConcluida = 0")
    suspend fun getActiveQuests(): List<Quest>

    @Update
    suspend fun updateQuest(quest: Quest)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuest(quest: Quest)

    @Query("SELECT * FROM quests")
    suspend fun getAllQuests(): List<Quest>

    @Query("SELECT * FROM quests WHERE tipo = :tipo AND isConcluida = 0")
    suspend fun getActiveQuestsByType(tipo: String): List<Quest>
}