package com.example.bovara.statistics.data.repository

import com.example.bovara.statistics.data.datasource.StatisticsApi
import com.example.bovara.statistics.data.model.Respaldo

class StatisticsRepository(private val api: StatisticsApi) {

    suspend fun getRespaldos(): List<Respaldo> {
        return try {
            api.getRespaldos().respaldos
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun createRespaldo(): Boolean {
        return try {
            api.createRespaldo()
        } catch (e: Exception) {
            throw e
        }
    }
}