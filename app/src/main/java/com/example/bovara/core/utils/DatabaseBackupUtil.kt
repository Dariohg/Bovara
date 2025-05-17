package com.example.bovara.core.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.bovara.core.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Utilidad para realizar respaldos y restauraciones de la base de datos
 */
object DatabaseBackupUtil {
    private const val TAG = "DatabaseBackupUtil"
    private const val BUFFER_SIZE = 8192
    private const val DB_NAME = "bovara_database"

    /**
     * Realiza un respaldo de la base de datos a un archivo zip
     *
     * @param context Contexto de la aplicación
     * @return Uri del archivo de respaldo o null si ocurrió un error
     */
    suspend fun backupDatabase(context: Context): Uri? = withContext(Dispatchers.IO) {
        try {
            // Obtiene la ruta de la base de datos
            val dbFile = context.getDatabasePath(DB_NAME)

            if (!dbFile.exists()) {
                Log.e(TAG, "La base de datos no existe en $dbFile")
                return@withContext null
            }

            // Crea el directorio de respaldos si no existe
            val backupDir = File(context.getExternalFilesDir(null), "backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            // Crea el nombre del archivo de respaldo con la fecha y hora actual
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val backupFileName = "bovara_backup_${dateFormat.format(Date())}.zip"
            val backupFile = File(backupDir, backupFileName)

            // Cierra la base de datos para asegurarse de que todos los cambios están guardados
            Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                .build()
                .close()

            // Crea el archivo zip
            ZipOutputStream(BufferedOutputStream(FileOutputStream(backupFile))).use { zos ->
                // Agrega el archivo de la base de datos al zip
                val dbData = ByteArray(BUFFER_SIZE)
                FileInputStream(dbFile).use { fis ->
                    BufferedInputStream(fis).use { bis ->
                        val entry = ZipEntry(dbFile.name)
                        zos.putNextEntry(entry)
                        var count: Int
                        while (bis.read(dbData, 0, BUFFER_SIZE).also { count = it } != -1) {
                            zos.write(dbData, 0, count)
                        }
                        zos.closeEntry()
                    }
                }

                // También respaldamos los archivos -shm y -wal si existen
                val shmFile = File(dbFile.path + "-shm")
                if (shmFile.exists()) {
                    addFileToZip(shmFile, zos)
                }

                val walFile = File(dbFile.path + "-wal")
                if (walFile.exists()) {
                    addFileToZip(walFile, zos)
                }

                // Opcionalmente, también podríamos incluir las imágenes u otros archivos importantes
                val imagesDir = File(context.filesDir, "images")
                if (imagesDir.exists() && imagesDir.isDirectory) {
                    addDirectoryToZip(imagesDir, "images", zos)
                }
            }

            Log.d(TAG, "Respaldo creado exitosamente en ${backupFile.absolutePath}")

            // Devuelve la URI del archivo de respaldo
            return@withContext FileProvider.getUriForFile(
                context,
                "com.example.bovara.fileprovider",
                backupFile
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear respaldo: ${e.message}")
            e.printStackTrace()
            return@withContext null
        }
    }

    /**
     * Restaura la base de datos desde un archivo de respaldo
     *
     * @param context Contexto de la aplicación
     * @param backupUri URI del archivo de respaldo
     * @return true si la restauración fue exitosa, false en caso contrario
     */
    suspend fun restoreDatabase(context: Context, backupUri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val dbFile = context.getDatabasePath(DB_NAME)
            val dbPath = dbFile.absolutePath

            // Cierra la base de datos para poder reemplazarla
            Room.databaseBuilder(context, AppDatabase::class.java, DB_NAME)
                .build()
                .close()

            // Crea un directorio temporal para la extracción
            val tempDir = File(context.cacheDir, "db_restore_temp")
            if (tempDir.exists()) {
                tempDir.deleteRecursively()
            }
            tempDir.mkdirs()

            // Extrae el archivo zip
            context.contentResolver.openInputStream(backupUri)?.use { inputStream ->
                ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
                    var entry: ZipEntry?
                    while (zis.nextEntry.also { entry = it } != null) {
                        entry?.let { zipEntry ->
                            val fileName = zipEntry.name
                            val newFile = File(tempDir, fileName)

                            if (zipEntry.isDirectory) {
                                newFile.mkdirs()
                            } else {
                                // Crea directorios padre si no existen
                                newFile.parentFile?.mkdirs()

                                // Extrae el archivo
                                FileOutputStream(newFile).use { fos ->
                                    val buffer = ByteArray(BUFFER_SIZE)
                                    var count: Int
                                    while (zis.read(buffer).also { count = it } != -1) {
                                        fos.write(buffer, 0, count)
                                    }
                                }
                            }
                            zis.closeEntry()
                        }
                    }
                }
            }

            // Ahora copiamos los archivos extraídos a sus ubicaciones correctas

            // Primero la base de datos principal
            val extractedDb = File(tempDir, DB_NAME)
            if (extractedDb.exists()) {
                // Elimina la base de datos actual y copia la nueva
                if (dbFile.exists()) dbFile.delete()
                extractedDb.copyTo(dbFile, overwrite = true)

                // Copia los archivos -shm y -wal si existen
                val extractedShmFile = File(tempDir, "$DB_NAME-shm")
                if (extractedShmFile.exists()) {
                    extractedShmFile.copyTo(File("$dbPath-shm"), overwrite = true)
                }

                val extractedWalFile = File(tempDir, "$DB_NAME-wal")
                if (extractedWalFile.exists()) {
                    extractedWalFile.copyTo(File("$dbPath-wal"), overwrite = true)
                }
            } else {
                Log.e(TAG, "No se encontró la base de datos en el respaldo")
                return@withContext false
            }

            // Restaurar imágenes si existen
            val extractedImagesDir = File(tempDir, "images")
            if (extractedImagesDir.exists() && extractedImagesDir.isDirectory) {
                val appImagesDir = File(context.filesDir, "images")
                if (appImagesDir.exists()) appImagesDir.deleteRecursively()
                appImagesDir.mkdirs()

                extractedImagesDir.listFiles()?.forEach { imageFile ->
                    if (imageFile.isFile) {
                        imageFile.copyTo(File(appImagesDir, imageFile.name), overwrite = true)
                    }
                }
            }

            // Limpia los archivos temporales
            tempDir.deleteRecursively()

            Log.d(TAG, "Restauración completada exitosamente")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error al restaurar respaldo: ${e.message}")
            e.printStackTrace()
            return@withContext false
        }
    }

    /**
     * Añade un archivo al zip
     */
    private fun addFileToZip(file: File, zos: ZipOutputStream) {
        val buffer = ByteArray(BUFFER_SIZE)
        FileInputStream(file).use { fis ->
            BufferedInputStream(fis).use { bis ->
                val entry = ZipEntry(file.name)
                zos.putNextEntry(entry)
                var count: Int
                while (bis.read(buffer, 0, BUFFER_SIZE).also { count = it } != -1) {
                    zos.write(buffer, 0, count)
                }
                zos.closeEntry()
            }
        }
    }

    /**
     * Añade un directorio completo al zip, manteniendo la estructura
     */
    private fun addDirectoryToZip(directory: File, path: String, zos: ZipOutputStream) {
        val files = directory.listFiles() ?: return

        for (file in files) {
            if (file.isDirectory) {
                addDirectoryToZip(file, "$path/${file.name}", zos)
            } else {
                val buffer = ByteArray(BUFFER_SIZE)
                FileInputStream(file).use { fis ->
                    BufferedInputStream(fis).use { bis ->
                        val entry = ZipEntry("$path/${file.name}")
                        zos.putNextEntry(entry)
                        var count: Int
                        while (bis.read(buffer, 0, BUFFER_SIZE).also { count = it } != -1) {
                            zos.write(buffer, 0, count)
                        }
                        zos.closeEntry()
                    }
                }
            }
        }
    }
}