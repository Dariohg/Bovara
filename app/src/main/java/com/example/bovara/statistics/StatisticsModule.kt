package com.example.bovara.statistics

import android.content.Context
import com.example.bovara.statistics.data.datasource.StatisticsApi
import com.example.bovara.statistics.data.datasource.StatisticsApiMock
import com.example.bovara.statistics.data.repository.StatisticsRepository
import com.example.bovara.statistics.domain.StatisticsUseCase
import com.example.bovara.statistics.domain.StatisticsUseCaseImpl
import com.example.bovara.statistics.presentation.StatisticsViewModel

object StatisticsModule {

    fun provideStatisticsApi(): StatisticsApi {
        return StatisticsApiMock()
    }

    fun provideStatisticsRepository(): StatisticsRepository {
        return StatisticsRepository(provideStatisticsApi())
    }

    fun provideStatisticsUseCase(): StatisticsUseCase {
        return StatisticsUseCaseImpl(provideStatisticsRepository())
    }

    fun provideStatisticsViewModel(): StatisticsViewModel {
        return StatisticsViewModel(provideStatisticsUseCase())
    }
}