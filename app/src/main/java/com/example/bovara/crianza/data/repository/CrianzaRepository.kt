package com.example.bovara.crianza.data.repository

import com.example.bovara.crianza.data.datasource.CrianzaDao
import com.example.bovara.crianza.data.model.CrianzaEntity
import kotlinx.coroutines.flow.Flow

class CrianzaRepository(private val crianzaDao: CrianzaDao) {
    fun getCrianzasByMadreId(madreId: Int): Flow<List<CrianzaEntity>> =
        crianzaDao.getCrianzasByMadreId(madreId)

    fun getCrianzaByCriaId(criaId: Int): Flow<CrianzaEntity?> =
        crianzaDao.getCrianzaByCriaId(criaId)

    suspend fun insertCrianza(crianza: CrianzaEntity): Long =
        crianzaDao.insertCrianza(crianza)

    suspend fun updateCrianza(crianza: CrianzaEntity) =
        crianzaDao.updateCrianza(crianza)

    suspend fun deleteCrianza(crianza: CrianzaEntity) =
        crianzaDao.deleteCrianza(crianza)
}