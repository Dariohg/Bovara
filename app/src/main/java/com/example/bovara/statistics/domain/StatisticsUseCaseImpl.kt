package com.example.bovara.statistics.domain

import com.example.bovara.ganado.data.model.GanadoEstadistica
import com.example.bovara.ganado.domain.GanadoUseCase
import com.example.bovara.statistics.data.model.Respaldo
import com.example.bovara.statistics.data.model.RespaldoRequest
import com.example.bovara.statistics.data.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class StatisticsUseCaseImpl(
    private val statisticsRepository: StatisticsRepository,
    private val ganadoUseCase: GanadoUseCase
) : StatisticsUseCase {

    override suspend fun getGanadoEstadisticas(): GanadoEstadistica {
        return ganadoUseCase.obtenerEstadisticasGanado()
    }

    override suspend fun getGanadoEstadisticasFlow(): Flow<GanadoEstadistica> = flow {
        emit(ganadoUseCase.obtenerEstadisticasGanado())
    }

    override suspend fun getBackupsByDeviceId(deviceId: String): List<Respaldo> {
        return statisticsRepository.getBackupsByDeviceId(deviceId)
    }

    override suspend fun createBackup(request: RespaldoRequest): Respaldo {
        return statisticsRepository.createBackup(request)
    }
}


