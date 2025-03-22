package com.example.bovara.medicamento.data.repository

import com.example.bovara.medicamento.data.datasource.MedicamentoDao
import com.example.bovara.medicamento.data.model.MedicamentoEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

class MedicamentoRepository(private val medicamentoDao: MedicamentoDao) {
    fun getMedicamentosByGanadoId(ganadoId: Int): Flow<List<MedicamentoEntity>> =
        medicamentoDao.getMedicamentosByGanadoId(ganadoId)

    fun getMedicamentoById(id: Int): Flow<MedicamentoEntity?> =
        medicamentoDao.getMedicamentoById(id)

    fun getMedicamentosProgramadosPendientes(): Flow<List<MedicamentoEntity>> =
        medicamentoDao.getMedicamentosProgramadosPendientes()

    fun getMedicamentosAplicados(): Flow<List<MedicamentoEntity>> =
        medicamentoDao.getMedicamentosAplicados()

    fun getMedicamentosByTipo(tipo: String): Flow<List<MedicamentoEntity>> =
        medicamentoDao.getMedicamentosByTipo(tipo)

    fun getMedicamentosByLote(lote: String): Flow<List<MedicamentoEntity>> =
        medicamentoDao.getMedicamentosByLote(lote)

    fun getMedicamentosByRangoDeFechas(inicio: Date, fin: Date): Flow<List<MedicamentoEntity>> =
        medicamentoDao.getMedicamentosByRangoDeFechas(inicio, fin)

    suspend fun insertMedicamento(medicamento: MedicamentoEntity): Long =
        medicamentoDao.insertMedicamento(medicamento)

    suspend fun insertMedicamentos(medicamentos: List<MedicamentoEntity>): List<Long> =
        medicamentoDao.insertMedicamentos(medicamentos)

    suspend fun updateMedicamento(medicamento: MedicamentoEntity) =
        medicamentoDao.updateMedicamento(medicamento)

    suspend fun deleteMedicamento(medicamento: MedicamentoEntity) =
        medicamentoDao.deleteMedicamento(medicamento)
}