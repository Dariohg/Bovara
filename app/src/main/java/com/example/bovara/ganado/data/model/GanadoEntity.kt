
package com.example.bovara.ganado.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "ganado")
data class GanadoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val numeroArete: String,
    val apodo: String? = null,
    val sexo: String, // "macho" o "hembra"
    val tipo: String, // "toro", "torito", "vaca" o "becerra"
    val color: String,
    val fechaNacimiento: Date? = null,
    val madreId: Int? = null,
    val cantidadCrias: Int = 0,
    val estado: String = "activo", // "activo", "vendido", "muerto"
    val imagenUrl: String? = null, // Ruta de la imagen principal
    val imagenesSecundarias: List<String>? = null, // Lista de rutas de imágenes secundarias
    val fechaRegistro: Date = Date() // Fecha en la que se registró el animal
)