
package com.example.bovara.crianza.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.bovara.ganado.data.model.GanadoEntity
import java.util.Date

@Entity(
    tableName = "crianza",
    foreignKeys = [
        ForeignKey(
            entity = GanadoEntity::class,
            parentColumns = ["id"],
            childColumns = ["madreId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = GanadoEntity::class,
            parentColumns = ["id"],
            childColumns = ["criaId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("madreId"), Index("criaId")]
)
data class CrianzaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val madreId: Int,
    val criaId: Int, // ID de la cría registrada en la tabla de ganado
    val fechaNacimiento: Date,
    val notas: String? = null,
    val fechaRegistro: Date = Date()
)