package com.example.bovara.medicamento.data.datasource

import androidx.room.*
import com.example.bovara.medicamento.data.model.MedicamentoEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface MedicamentoDao {
    @Query("SELECT * FROM medicamentos WHERE ganadoId = :ganadoId ORDER BY fechaAplicacion DESC")
    fun getMedicamentosByGanadoId(ganadoId: Int): Flow<List<MedicamentoEntity>>

    @Query("SELECT * FROM medicamentos WHERE id = :id")
    fun getMedicamentoById(id: Int): Flow<MedicamentoEntity?>

    @Query("SELECT * FROM medicamentos WHERE esProgramado = 1")
    fun getMedicamentosProgramados(): Flow<List<MedicamentoEntity>>

    @Query("SELECT * FROM medicamentos WHERE fechaAplicacion BETWEEN :inicio AND :fin")
    fun getMedicamentosByRangoDeFechas(inicio: Date, fin: Date): Flow<List<MedicamentoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicamento(medicamento: MedicamentoEntity): Long

    @Update
    suspend fun updateMedicamento(medicamento: MedicamentoEntity)

    @Delete
    suspend fun deleteMedicamento(medicamento: MedicamentoEntity)
}