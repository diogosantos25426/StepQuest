package com.example.stepquest.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [User::class, WalkingSession::class, Item::class, Quest::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun sessionDao(): SessionDao
    abstract fun questDao(): QuestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stepquest_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
    
    suspend fun populateInitialItems(userId: Int) {
        val dao = userDao()
        dao.insertItem(Item(userId = userId, nome = "Poção de Vida", descricao = "Cura 30 HP", tipo = "CURA", valor = 30, quantidade = 5, imagemRes = "inventario"))
        dao.insertItem(Item(userId = userId, nome = "Bomba", descricao = "Tira 40 HP ao inimigo", tipo = "DANO", valor = 40, quantidade = 2, imagemRes = "iconeespada"))
        dao.insertItem(Item(userId = userId, nome = "Espada Lendária", descricao = "Aumenta o dano", tipo = "ARMA", valor = 10, quantidade = 1, imagemRes = "iconeespada"))
    }
}