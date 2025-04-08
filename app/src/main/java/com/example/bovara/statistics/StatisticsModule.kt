package com.example.bovara.statistics

import android.content.Context
import com.example.bovara.core.network.RetrofitHelper
import com.example.bovara.ganado.domain.GanadoUseCase
import com.example.bovara.statistics.data.datasource.StatisticsApi
import com.example.bovara.statistics.data.repository.StatisticsRepository
import com.example.bovara.statistics.presentation.StatisticsViewModel

object StatisticsModule {

    fun provideStatisticsApi(): StatisticsApi {
        return RetrofitHelper.retrofit.create(StatisticsApi::class.java)
    }

    fun provideStatisticsRepository(): StatisticsRepository {
        return StatisticsRepository(provideStatisticsApi())
    }

    fun provideStatisticsViewModelFactory(context: Context, ganadoUseCase: GanadoUseCase): StatisticsViewModel.Factory {
        return StatisticsViewModel.Factory(context, ganadoUseCase, provideStatisticsRepository())
    }
}
