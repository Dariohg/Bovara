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

/**
 * Clase que proporciona las dependencias de la aplicación mediante un enfoque de
 * Service Locator simplificado, evitando la necesidad de usar Dagger o Hilt.
 */
object AppDependencies {

    val pendienteRepository: PendienteRepository? =null

    // Objetos singleton para las dependencias principales
    private var appDatabase: AppDatabase? = null

    // Repositorios
    var ganadoRepository: GanadoRepository? = null
    var medicamentoRepository: MedicamentoRepository? = null
    private var crianzaRepository: CrianzaRepository? = null

    // Casos de uso
    private var ganadoUseCase: GanadoUseCase? = null
    private var medicamentoUseCase: MedicamentoUseCase? = null
    private var crianzaUseCase: CrianzaUseCase? = null

    /**
     * Inicializa las dependencias principales. Debe llamarse en la clase Application.
     */
    fun initialize(context: Context) {
        // Inicializar la base de datos
        appDatabase = AppDatabase.getDatabase(context)
    }

    /**
     * Obtiene la instancia de la base de datos
     */
    fun getAppDatabase(context: Context): AppDatabase {
        return appDatabase ?: synchronized(this) {
            appDatabase ?: AppDatabase.getDatabase(context).also {
                appDatabase = it
            }
        }
    }

    /**
     * Obtiene el repositorio de ganado
     */
    fun getGanadoRepository(context: Context): GanadoRepository {
        return ganadoRepository ?: synchronized(this) {
            ganadoRepository ?: GanadoRepository(getAppDatabase(context).ganadoDao()).also {
                ganadoRepository = it
            }
        }
    }

    /**
     * Obtiene el caso de uso de ganado
     */
    fun getGanadoUseCase(context: Context): GanadoUseCase {
        return ganadoUseCase ?: synchronized(this) {
            ganadoUseCase ?: GanadoUseCase(getGanadoRepository(context)).also {
                ganadoUseCase = it
            }
        }
    }

    /**
     * Obtiene el repositorio de medicamentos
     */
    fun getMedicamentoRepository(context: Context): MedicamentoRepository {
        return medicamentoRepository ?: synchronized(this) {
            medicamentoRepository ?: MedicamentoRepository(getAppDatabase(context).medicamentoDao()).also {
                medicamentoRepository = it
            }
        }
    }

    /**
     * Obtiene el caso de uso de medicamentos
     */
    fun getMedicamentoUseCase(context: Context): MedicamentoUseCase {
        return medicamentoUseCase ?: synchronized(this) {
            medicamentoUseCase ?: MedicamentoUseCase(getMedicamentoRepository(context)).also {
                medicamentoUseCase = it
            }
        }
    }





    /**
     * Obtiene el repositorio de crianza
     */
    fun getCrianzaRepository(context: Context): CrianzaRepository {
        return crianzaRepository ?: synchronized(this) {
            crianzaRepository ?: CrianzaRepository(getAppDatabase(context).crianzaDao()).also {
                crianzaRepository = it
            }
        }
    }

    /**
     * Obtiene el caso de uso de crianza
     *
     * Nota: Este método necesita el caso de uso de ganado para incrementar el contador de crías
     */
    fun getCrianzaUseCase(context: Context): CrianzaUseCase {
        return crianzaUseCase ?: synchronized(this) {
            crianzaUseCase ?: CrianzaUseCase(
                getCrianzaRepository(context),
                getGanadoUseCase(context)
            ).also {
                crianzaUseCase = it
            }
        }
    }

    /**
     * Limpia todas las dependencias. Útil para pruebas o cuando se necesita reiniciar la aplicación.
     */
    fun clearAll() {
        appDatabase = null
        ganadoRepository = null
        medicamentoRepository = null
        crianzaRepository = null
        ganadoUseCase = null
        medicamentoUseCase = null
        crianzaUseCase = null
    }
}