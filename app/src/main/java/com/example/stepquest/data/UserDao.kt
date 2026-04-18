package com.example.stepquest.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registerUser(user: User): Long

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun getUser(username: String, password: String): User?

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username)")
    suspend fun checkUserExists(username: String): Boolean

    @Query("SELECT last_unlocked_level FROM users WHERE id = :userId")
    suspend fun getLastUnlockedLevel(userId: Int): Int

    @Query("UPDATE users SET last_unlocked_level = :level WHERE id = :userId")
    suspend fun updateLastUnlockedLevel(userId: Int, level: Int)

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?

    @Query("UPDATE users SET xp_total = xp_total + :xp WHERE id = :userId")
    suspend fun addXp(userId: Int, xp: Int)

    // --- Itens ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item)

    @Query("UPDATE items SET quantidade = :novaQuantidade WHERE id = :itemId")
    suspend fun updateItemQuantity(itemId: Int, novaQuantidade: Int)

    @Query("SELECT * FROM items WHERE userId = :userId AND quantidade > 0")
    suspend fun getUserItems(userId: Int): List<Item>
    
    @Query("SELECT * FROM items WHERE userId = :userId AND nome = :nome LIMIT 1")
    suspend fun getItemByName(userId: Int, nome: String): Item?
}