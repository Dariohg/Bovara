package com.example.bovara.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.bovara.ganado.data.datasource.GanadoDao
import com.example.bovara.ganado.data.model.GanadoEntity
import com.example.bovara.medicamento.data.datasource.MedicamentoDao
import com.example.bovara.medicamento.data.model.MedicamentoEntity
import com.example.bovara.crianza.data.datasource.CrianzaDao
import com.example.bovara.crianza.data.model.CrianzaEntity

@Database(
    entities = [
        GanadoEntity::class,
        MedicamentoEntity::class,
        CrianzaEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ganadoDao(): GanadoDao
    abstract fun medicamentoDao(): MedicamentoDao
    abstract fun crianzaDao(): CrianzaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bovara_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}