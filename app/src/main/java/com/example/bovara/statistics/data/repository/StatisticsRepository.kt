package com.example.bovara.statistics.data.repository

import com.example.bovara.statistics.data.datasource.StatisticsApi
import com.example.bovara.statistics.data.model.Respaldo
import com.example.bovara.statistics.data.model.RespaldoRequest

class StatisticsRepository(private val statisticsApi: StatisticsApi) {

    suspend fun getBackupsByDeviceId(deviceId: String): List<Respaldo> {
        return statisticsApi.getBackupsByDeviceId(deviceId).respaldos
    }

    suspend fun createBackup(request: RespaldoRequest): Respaldo {
        return statisticsApi.createBackup(request)
    }
}


