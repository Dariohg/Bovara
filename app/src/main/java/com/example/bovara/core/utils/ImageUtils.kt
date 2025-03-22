package com.example.bovara.core.utils

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
            // Generar un nombre único para la imagen
            val fileName = "img_${UUID.randomUUID()}"

            // Obtener el bitmap de la URI
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)

            // Guardar el bitmap y obtener la ruta relativa
            return saveImageToInternalStorage(context, bitmap, fileName) ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar imagen desde URI: ${e.message}")
            return ""
        }
    }

    fun saveImageToInternalStorage(context: Context, bitmap: Bitmap, fileName: String): String? {
        return try {
            // Crear directorio de imágenes si no existe
            val directory = File(context.filesDir, "images")
            if (!directory.exists()) {
                directory.mkdir()
            }

            // Crear archivo para guardar la imagen
            val file = File(directory, "$fileName.jpg")

            // Guardar imagen en formato JPEG con compresión
            FileOutputStream(file).use { outputStream ->
                // Redimensionar la imagen para ahorrar espacio
                val resizedBitmap = resizeBitmap(bitmap, 800, 600)
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            }

            // Devolver ruta relativa
            "images/$fileName.jpg"
        } catch (e: IOException) {
            Log.e(TAG, "Error al guardar imagen: ${e.message}")
            null
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