package com.example.bovara.statistics.domain

import com.example.bovara.statistics.data.model.Respaldo
import com.example.bovara.statistics.data.repository.StatisticsRepository

interface StatisticsUseCase {
    suspend fun getRespaldos(): List<Respaldo>
    suspend fun createRespaldo(): Boolean
}

class StatisticsUseCaseImpl(private val repository: StatisticsRepository) : StatisticsUseCase {

    override suspend fun getRespaldos(): List<Respaldo> {
        return repository.getRespaldos()
    }

    override suspend fun createRespaldo(): Boolean {
        return repository.createRespaldo()
    }
}