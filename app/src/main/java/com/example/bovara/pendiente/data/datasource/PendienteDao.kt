package com.example.bovara.pendiente.data.datasource

import androidx.room.*
import com.example.bovara.pendiente.data.model.PendienteEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface PendienteDao {
    @Query("SELECT * FROM pendientes ORDER BY fecha_programada ASC, hora ASC")
    fun obtenerTodos(): Flow<List<PendienteEntity>>

    @Query("SELECT * FROM pendientes WHERE id = :id")
    suspend fun obtenerPorId(id: Int): PendienteEntity?

    @Query("SELECT * FROM pendientes WHERE id_medicina = :idMedicina ORDER BY fecha_programada ASC")
    fun obtenerPorMedicina(idMedicina: Int): Flow<List<PendienteEntity>>

    @Query("SELECT * FROM pendientes WHERE estatus = :estatus ORDER BY fecha_programada ASC")
    fun obtenerPorEstatus(estatus: String): Flow<List<PendienteEntity>>

    @Query("SELECT * FROM pendientes WHERE fecha_programada BETWEEN :inicio AND :fin ORDER BY fecha_programada ASC")
    fun obtenerPorRangoDeFechas(inicio: Date, fin: Date): Flow<List<PendienteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(pendiente: PendienteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarVarios(pendientes: List<PendienteEntity>): List<Long>

    @Update
    suspend fun actualizar(pendiente: PendienteEntity)

    @Delete
    suspend fun eliminar(pendiente: PendienteEntity)

    @Query("SELECT * FROM pendientes WHERE fecha_programada BETWEEN :inicio AND :fin")
    suspend fun obtenerDelDiaActual(inicio: Date, fin: Date): List<PendienteEntity>

}
