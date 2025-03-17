package com.example.bovara.medicamento.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.bovara.ganado.data.model.GanadoEntity
import java.util.Date

@Entity(
    tableName = "medicamentos",
    foreignKeys = [
        ForeignKey(
            entity = GanadoEntity::class,
            parentColumns = ["id"],
            childColumns = ["ganadoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ganadoId")]
)
data class MedicamentoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val fechaAplicacion: Date,
    val dosisML: Float,
    val ganadoId: Int,
    val esProgramado: Boolean = false // Indica si es una vacuna programada regular
)