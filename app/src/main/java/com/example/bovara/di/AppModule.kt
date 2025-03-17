package com.example.bovara.di

import android.content.Context
import androidx.test.espresso.core.internal.deps.dagger.Module
import androidx.test.espresso.core.internal.deps.dagger.Provides
import com.example.bovara.core.database.AppDatabase
import com.example.bovara.crianza.data.repository.CrianzaRepository
import com.example.bovara.crianza.domain.CrianzaUseCase
import com.example.bovara.ganado.data.repository.GanadoRepository
import com.example.bovara.ganado.domain.GanadoUseCase
import com.example.bovara.medicamento.data.repository.MedicamentoRepository
import com.example.bovara.medicamento.domain.MedicamentoUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    // Ganado
    @Provides
    @Singleton
    fun provideGanadoRepository(database: AppDatabase): GanadoRepository {
        return GanadoRepository(database.ganadoDao())
    }

    @Provides
    @Singleton
    fun provideGanadoUseCase(repository: GanadoRepository): GanadoUseCase {
        return GanadoUseCase(repository)
    }

    // Medicamento
    @Provides
    @Singleton
    fun provideMedicamentoRepository(database: AppDatabase): MedicamentoRepository {
        return MedicamentoRepository(database.medicamentoDao())
    }

    @Provides
    @Singleton
    fun provideMedicamentoUseCase(repository: MedicamentoRepository): MedicamentoUseCase {
        return MedicamentoUseCase(repository)
    }

    // Crianza
    @Provides
    @Singleton
    fun provideCrianzaRepository(database: AppDatabase): CrianzaRepository {
        return CrianzaRepository(database.crianzaDao())
    }

    @Provides
    @Singleton
    fun provideCrianzaUseCase(repository: CrianzaRepository): CrianzaUseCase {
        return CrianzaUseCase(repository)
    }
}