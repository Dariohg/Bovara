package com.example.bovara.statistics

import com.example.bovara.core.network.RetrofitHelper
import com.example.bovara.ganado.domain.GanadoUseCase
import com.example.bovara.statistics.data.datasource.StatisticsApi
import com.example.bovara.statistics.data.repository.StatisticsRepository
import com.example.bovara.statistics.presentation.StatisticsViewModel

object StatisticsModule {

    fun provideStatisticsApi(): StatisticsApi {
        // Use RetrofitHelper's retrofit property directly
        return RetrofitHelper.retrofit.create(StatisticsApi::class.java)
    }

    // Or use the createApi helper method
    // fun provideStatisticsApi(): StatisticsApi = RetrofitHelper.createApi()

    fun provideStatisticsRepository(): StatisticsRepository {
        return StatisticsRepository(provideStatisticsApi())
    }

    fun provideStatisticsViewModelFactory(ganadoUseCase: GanadoUseCase): StatisticsViewModel.Factory {
        return StatisticsViewModel.Factory(ganadoUseCase, provideStatisticsRepository())
    }
}

