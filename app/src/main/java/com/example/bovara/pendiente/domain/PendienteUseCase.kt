package com.example.bovara.pendiente.domain

import com.example.bovara.pendiente.data.model.PendienteEntity
import com.example.bovara.pendiente.data.repository.PendienteRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date

class PendienteUseCase(private val repository: PendienteRepository) {

    fun obtenerTodos(): Flow<List<PendienteEntity>> =
        repository.obtenerTodos()

    suspend fun obtenerPorId(id: Int): PendienteEntity? =
        repository.obtenerPorId(id)

    fun obtenerPorMedicina(idMedicina: Int): Flow<List<PendienteEntity>> =
        repository.obtenerPorMedicina(idMedicina)

    fun obtenerPorEstatus(estatus: String): Flow<List<PendienteEntity>> =
        repository.obtenerPorEstatus(estatus)

    fun obtenerPorRangoDeFechas(inicio: Date, fin: Date): Flow<List<PendienteEntity>> =
        repository.obtenerPorRangoDeFechas(inicio, fin)

    suspend fun insertar(pendiente: PendienteEntity): Long =
        repository.insertar(pendiente)

    suspend fun insertarVarios(pendientes: List<PendienteEntity>): List<Long> =
        repository.insertarVarios(pendientes)

    suspend fun actualizar(pendiente: PendienteEntity) =
        repository.actualizar(pendiente)

    suspend fun eliminar(pendiente: PendienteEntity) =
        repository.eliminar(pendiente)

    suspend fun obtenerPendientesDelDiaConHorasRelativas(): Flow<List<PendienteEntity>> =
        repository.obtenerPendientesDelDiaConHorasRelativas()

}
