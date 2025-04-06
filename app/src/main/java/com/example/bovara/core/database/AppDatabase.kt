package com.example.bovara.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.RoomDatabase.Callback
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.bovara.ganado.data.datasource.GanadoDao
import com.example.bovara.ganado.data.model.GanadoEntity
import com.example.bovara.medicamento.data.datasource.MedicamentoDao
import com.example.bovara.medicamento.data.model.MedicamentoEntity
import com.example.bovara.crianza.data.datasource.CrianzaDao
import com.example.bovara.crianza.data.model.CrianzaEntity
import com.example.bovara.pendiente.data.datasource.PendienteDao
import com.example.bovara.pendiente.data.model.PendienteEntity
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.Executors

@Database(
    entities = [
        GanadoEntity::class,
        MedicamentoEntity::class,
        CrianzaEntity::class,
        PendienteEntity::class
    ],
    version = 4, // Incrementamos la versión de la BD
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ganadoDao(): GanadoDao
    abstract fun medicamentoDao(): MedicamentoDao
    abstract fun crianzaDao(): CrianzaDao
    abstract fun pendienteDao(): PendienteDao // Nuevo DAO agregado

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Crea copia de seguridad de la base de datos existente antes de cualquier migración
                backupExistingDatabase(context)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bovara_database"
                )
                    // Usar migración destructiva automática
                    .fallbackToDestructiveMigration()
                    // Añadir callback para registrar eventos de migración
                    .addCallback(object : Callback() {
                        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                            super.onDestructiveMigration(db)
                            // Esto se ejecuta después de una migración destructiva
                            // Puedes agregar código para recuperar datos si es necesario
                        }
                    })
                    .build()

                INSTANCE = instance
                instance
            }
        }

        // Función para crear copia de seguridad de la base de datos existente
        private fun backupExistingDatabase(context: Context) {
            val dbFile = context.getDatabasePath("bovara_database")
            if (!dbFile.exists()) return

            Executors.newSingleThreadExecutor().execute {
                try {
                    val backupDir = File(context.getExternalFilesDir(null), "backup")
                    if (!backupDir.exists()) {
                        backupDir.mkdirs()
                    }

                    // Crear copia con marca de tiempo para mantener versiones anteriores
                    val timestamp = System.currentTimeMillis()
                    val backupFile = File(backupDir, "bovara_backup_$timestamp.db")

                    FileInputStream(dbFile).use { input ->
                        FileOutputStream(backupFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    // Mantener solo las últimas 5 copias de seguridad para no ocupar demasiado espacio
                    cleanupOldBackups(backupDir)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        // Eliminar copias de seguridad antiguas (mantener solo las 5 más recientes)
        private fun cleanupOldBackups(backupDir: File) {
            val backupFiles = backupDir.listFiles { file ->
                file.name.startsWith("bovara_backup_") && file.name.endsWith(".db")
            }

            backupFiles?.sortByDescending { it.lastModified() }

            backupFiles?.drop(5)?.forEach { it.delete() }
        }
    }
}
