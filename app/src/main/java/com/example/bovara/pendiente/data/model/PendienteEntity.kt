package com.example.bovara.pendiente.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.util.Date

@Entity(tableName = "pendientes")
data class PendienteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    @ColumnInfo(name = "id_medicina", index = true)
    val idMedicina: Long,

    @ColumnInfo(name = "fecha_programada")
    val fechaProgramada: Date,

    @ColumnInfo(name = "hora")
    val hora: String, // Formato HH:mm

    @ColumnInfo(name = "estatus")
    var estatus: String // Ejemplo: "pendiente", "completado"
)
