package com.example.bovara.core.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object ImageUtils {

    private const val TAG = "ImageUtils"

    fun createImageFile(context: Context): File {
        // Crear nombre único para el archivo
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "com.example.bovara.fileprovider",
            file
        )
    }

    fun saveImageToInternalStorage(context: Context, uri: Uri): String {
        try {
            // Crear directorio de imágenes si no existe
            val directory = File(context.filesDir, "images")
            if (!directory.exists()) {
                directory.mkdirs() // Usar mkdirs() en lugar de mkdir()
            }

            // Generar un nombre único para la imagen
            val fileName = "img_${UUID.randomUUID()}"

            // Usar inputStream para manejar correctamente los recursos
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)

                if (bitmap == null) {
                    Log.e(TAG, "No se pudo decodificar la imagen")
                    return ""
                }

                // Crear archivo para guardar la imagen
                val file = File(directory, "$fileName.jpg")

                // Guardar imagen en formato JPEG con compresión
                FileOutputStream(file).use { outputStream ->
                    val resizedBitmap = resizeBitmap(bitmap, 800, 600)
                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                    outputStream.flush()
                }

                // Registrar éxito en el log
                Log.d(TAG, "Imagen guardada exitosamente en: ${file.absolutePath}")
                return "images/$fileName.jpg"
            } ?: run {
                Log.e(TAG, "No se pudo abrir el input stream para la URI")
                return ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar imagen: ${e.message}")
            e.printStackTrace()
            return ""
        }
    }

    fun loadImageFromInternalStorage(context: Context, path: String): Bitmap? {
        return try {
            val file = File(context.filesDir, path)
            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "Error al cargar imagen: ${e.message}")
            null
        }
    }

    // En ImageUtils.kt añadir este método
    fun createImageUri(context: Context): Uri? {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "img_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            }
            context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating image URI: ${e.message}")
            null
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val ratio = width.toFloat() / height.toFloat()

        val newWidth: Int
        val newHeight: Int

        if (width > height) {
            newWidth = maxWidth
            newHeight = (newWidth / ratio).toInt()
        } else {
            newHeight = maxHeight
            newWidth = (newHeight * ratio).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}