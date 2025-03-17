package com.example.bovara.ganado.data.repository

import com.example.bovara.ganado.data.datasource.GanadoDao
import com.example.bovara.ganado.data.model.GanadoEntity
import kotlinx.coroutines.flow.Flow

class GanadoRepository(private val ganadoDao: GanadoDao) {
    fun getAllGanado(): Flow<List<GanadoEntity>> = ganadoDao.getAllGanado()

    fun getGanadoById(id: Int): Flow<GanadoEntity?> = ganadoDao.getGanadoById(id)

    fun getGanadoByTipo(tipo: String): Flow<List<GanadoEntity>> = ganadoDao.getGanadoByTipo(tipo)

    fun getGanadoByEstado(estado: String): Flow<List<GanadoEntity>> = ganadoDao.getGanadoByEstado(estado)

    fun getCriasByMadreId(madreId: Int): Flow<List<GanadoEntity>> = ganadoDao.getCriasByMadreId(madreId)

    fun searchGanado(query: String): Flow<List<GanadoEntity>> = ganadoDao.searchGanado(query)

    suspend fun insertGanado(ganado: GanadoEntity): Long = ganadoDao.insertGanado(ganado)

    suspend fun updateGanado(ganado: GanadoEntity) = ganadoDao.updateGanado(ganado)

    suspend fun incrementarCriasDeMadre(madreId: Int) = ganadoDao.incrementarCriasDeMadre(madreId)

    suspend fun actualizarEstado(ganadoId: Int, nuevoEstado: String) = ganadoDao.actualizarEstado(ganadoId, nuevoEstado)

    suspend fun deleteGanado(ganado: GanadoEntity) = ganadoDao.deleteGanado(ganado)
}