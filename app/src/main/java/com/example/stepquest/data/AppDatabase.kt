package com.example.stepquest.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [User::class, WalkingSession::class, Item::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun sessionDao(): SessionDao

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
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Nota: No primeiro login real, o userId ainda não é conhecido.
                // Esta lógica de pre-populate aqui é genérica. 
                // Numa app real, isto seria feito após o registo do utilizador.
            }
        }
    }
    
    // Função auxiliar para popular itens para um utilizador específico
    suspend fun populateInitialItems(userId: Int) {
        val dao = userDao()
        dao.insertItem(Item(userId = userId, nome = "Poção de Vida", descricao = "Cura 30 HP", tipo = "CURA", valor = 30, quantidade = 5, imagemRes = "inventario"))
        dao.insertItem(Item(userId = userId, nome = "Bomba", descricao = "Tira 40 HP ao inimigo", tipo = "DANO", valor = 40, quantidade = 2, imagemRes = "iconeespada"))
        dao.insertItem(Item(userId = userId, nome = "Espada Lendária", descricao = "Aumenta o dano", tipo = "ARMA", valor = 10, quantidade = 1, imagemRes = "iconeespada"))
    }
}