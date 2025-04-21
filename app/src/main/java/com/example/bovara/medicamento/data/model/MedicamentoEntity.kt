package com.example.bovara.medicamento.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.bovara.ganado.data.model.GanadoEntity
import java.io.Serializable
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
    val ganadoId: Int? = null,  // Cambiado a nullable
    val tipo: String = "vacuna", // "vacuna", "desparasitante", "vitamina", "antibiótico", "otro"
    val lote: String? = null, // Para identificar lotes de vacunas
    val aplicado: Boolean = false, // Indica si ya fue aplicado
    val notas: String? = null, // Notas adicionales
    val fechaRegistro: Date = Date() // Fecha en que se registró esta entrada
): Serializable