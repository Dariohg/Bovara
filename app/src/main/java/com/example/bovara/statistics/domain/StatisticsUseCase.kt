package com.example.bovara.statistics.domain

import com.example.bovara.ganado.data.model.GanadoEstadistica
import com.example.bovara.statistics.data.model.Respaldo
import com.example.bovara.statistics.data.model.RespaldoRequest
import kotlinx.coroutines.flow.Flow

interface StatisticsUseCase {
    suspend fun getGanadoEstadisticas(): GanadoEstadistica
    suspend fun getGanadoEstadisticasFlow(): Flow<GanadoEstadistica>

    // Métodos para gestionar respaldos
    suspend fun getBackups(): List<Respaldo>
    suspend fun createBackup(request: RespaldoRequest): Respaldo
}

