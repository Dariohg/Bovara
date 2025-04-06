package com.example.bovara.pendiente.data.repository

import com.example.bovara.pendiente.data.datasource.PendienteDao
import com.example.bovara.pendiente.data.model.PendienteEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

class PendienteRepository(private val pendienteDao: PendienteDao) {

    fun obtenerTodos(): Flow<List<PendienteEntity>> =
        pendienteDao.obtenerTodos()

    suspend fun obtenerPorId(id: Int): PendienteEntity? =
        pendienteDao.obtenerPorId(id)

    fun obtenerPorMedicina(idMedicina: Int): Flow<List<PendienteEntity>> =
        pendienteDao.obtenerPorMedicina(idMedicina)

    fun obtenerPorEstatus(estatus: String): Flow<List<PendienteEntity>> =
        pendienteDao.obtenerPorEstatus(estatus)

    fun obtenerPorRangoDeFechas(inicio: Date, fin: Date): Flow<List<PendienteEntity>> =
        pendienteDao.obtenerPorRangoDeFechas(inicio, fin)

    suspend fun insertar(pendiente: PendienteEntity): Long =
        pendienteDao.insertar(pendiente)

    suspend fun insertarVarios(pendientes: List<PendienteEntity>): List<Long> =
        pendienteDao.insertarVarios(pendientes)

    suspend fun actualizar(pendiente: PendienteEntity) =
        pendienteDao.actualizar(pendiente)

    suspend fun eliminar(pendiente: PendienteEntity) =
        pendienteDao.eliminar(pendiente)
}
