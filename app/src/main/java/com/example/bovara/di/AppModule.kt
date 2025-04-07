package com.example.bovara.di

import android.content.Context
import com.example.bovara.core.database.AppDatabase
import com.example.bovara.crianza.data.repository.CrianzaRepository
import com.example.bovara.crianza.domain.CrianzaUseCase
import com.example.bovara.ganado.data.repository.GanadoRepository
import com.example.bovara.ganado.domain.GanadoUseCase
import com.example.bovara.medicamento.data.repository.MedicamentoRepository
import com.example.bovara.medicamento.domain.MedicamentoUseCase
import com.example.bovara.pendiente.data.repository.PendienteRepository
import com.example.bovara.pendiente.domain.PendienteUseCase

/**
 * Clase de utilidad para proporcionar las dependencias de la aplicación.
 * Esta versión reemplaza el módulo Dagger/Hilt con un enfoque más simple.
 */
object AppModule {

    // Singleton de la base de datos
    private var appDatabase: AppDatabase? = null

    /**
     * Proporciona la instancia de la base de datos de la aplicación
     */
    fun provideAppDatabase(context: Context): AppDatabase {
        return appDatabase ?: synchronized(this) {
            appDatabase ?: AppDatabase.getDatabase(context).also {
                appDatabase = it
            }
        }
    }

    /**
     * Proporciona el repositorio de ganado
     */
    fun provideGanadoRepository(context: Context): GanadoRepository {
        return GanadoRepository(provideAppDatabase(context).ganadoDao())
    }



    /**
     * Proporciona el caso de uso de ganado
     */
    fun provideGanadoUseCase(context: Context): GanadoUseCase {
        return GanadoUseCase(provideGanadoRepository(context))
    }

    /**
     * Proporciona el repositorio de medicamentos
     */
    fun provideMedicamentoRepository(context: Context): MedicamentoRepository {
        return MedicamentoRepository(provideAppDatabase(context).medicamentoDao())
    }

    /**
     * Proporciona el caso de uso de medicamentos
     */
    fun provideMedicamentoUseCase(context: Context): MedicamentoUseCase {
        return MedicamentoUseCase(provideMedicamentoRepository(context))
    }

    fun providePendienteUseCase(context: Context): PendienteUseCase {
        return PendienteUseCase(providePendienteRepository(context))
    }

    fun providePendienteRepository(context: Context): PendienteRepository {
        return PendienteRepository(provideAppDatabase(context).pendienteDao())
    }

    /**
     * Proporciona el repositorio de crianza
     */
    fun provideCrianzaRepository(context: Context): CrianzaRepository {
        return CrianzaRepository(provideAppDatabase(context).crianzaDao())
    }

    /**
     * Proporciona el caso de uso de crianza
     */
    fun provideCrianzaUseCase(context: Context): CrianzaUseCase {
        // Aquí necesitamos el GanadoUseCase para CrianzaUseCase
        return CrianzaUseCase(
            provideCrianzaRepository(context),
            provideGanadoUseCase(context)
        )
    }


}