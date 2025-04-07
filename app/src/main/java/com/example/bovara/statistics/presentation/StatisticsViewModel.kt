package com.example.bovara.statistics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bovara.ganado.data.model.GanadoEstadistica
import com.example.bovara.ganado.domain.GanadoUseCase
import com.example.bovara.statistics.data.model.DetallesHembras
import com.example.bovara.statistics.data.model.DetallesMachos
import com.example.bovara.statistics.data.model.EstadoAnimales
import com.example.bovara.statistics.data.model.Respaldo
import com.example.bovara.statistics.data.model.RespaldoRequest
import com.example.bovara.statistics.data.repository.StatisticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class StatisticsViewModel(
    private val ganadoUseCase: GanadoUseCase,
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private val _isCreatingBackup = MutableStateFlow(false)
    val isCreatingBackup: StateFlow<Boolean> = _isCreatingBackup.asStateFlow()

    init {
        fetchBackups()
    }

    fun fetchBackups() {
        viewModelScope.launch {
            _uiState.value = StatisticsUiState.Loading
            try {
                val respaldos = statisticsRepository.getAllBackups()
                if (respaldos.isEmpty()) {
                    _uiState.value = StatisticsUiState.Empty
                } else {
                    _uiState.value = StatisticsUiState.Success(respaldos)
                }
            } catch (e: Exception) {
                _uiState.value = StatisticsUiState.Error("Error al cargar los respaldos: ${e.message}")
            }
        }
    }

    fun createBackup() {
        viewModelScope.launch {
            _isCreatingBackup.value = true
            try {
                // Obtener estadísticas actuales del ganado
                val estadisticas = ganadoUseCase.obtenerEstadisticasGanado()

                // Crear un nuevo respaldo con las estadísticas
                val request = mapGanadoEstadisticaToRespaldoRequest(estadisticas)

                // Guardar el respaldo
                statisticsRepository.createBackup(request)

                // Recargar la lista de respaldos
                fetchBackups()
            } catch (e: Exception) {
                _uiState.value = StatisticsUiState.Error("Error al crear respaldo: ${e.message}")
            } finally {
                _isCreatingBackup.value = false
            }
        }
    }

    private fun mapGanadoEstadisticaToRespaldoRequest(estadisticas: GanadoEstadistica): RespaldoRequest {
        return RespaldoRequest(
            totalMachos = estadisticas.totalMachos,
            totalHembras = estadisticas.totalHembras,
            detalleMachos = DetallesMachos(
                becerro = estadisticas.detalleMachos["becerro"] ?: 0,
                torito = estadisticas.detalleMachos["torito"] ?: 0,
                toro = estadisticas.detalleMachos["toro"] ?: 0
            ),
            detalleHembras = DetallesHembras(
                becerra = estadisticas.detalleHembras["becerra"] ?: 0,
                vaca = estadisticas.detalleHembras["vaca"] ?: 0
            ),
            estadoAnimales = EstadoAnimales(
                activos = estadisticas.estadoAnimales["activo"] ?: 0,
                vendidos = estadisticas.estadoAnimales["vendido"] ?: 0,
                muertos = estadisticas.estadoAnimales["muerto"] ?: 0
            )
        )
    }

    class Factory(
        private val ganadoUseCase: GanadoUseCase,
        private val statisticsRepository: StatisticsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
                return StatisticsViewModel(ganadoUseCase, statisticsRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class StatisticsUiState {
    object Loading : StatisticsUiState()
    object Empty : StatisticsUiState()
    data class Error(val message: String) : StatisticsUiState()
    data class Success(val respaldos: List<Respaldo>) : StatisticsUiState()
}

