package com.example.bovara.statistics.presentation

import android.content.Context
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bovara.ganado.data.model.GanadoEstadistica
import com.example.bovara.ganado.domain.GanadoUseCase
import com.example.bovara.statistics.data.model.*
import com.example.bovara.statistics.data.repository.StatisticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StatisticsViewModel(
    private val context: Context,
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
                val deviceId = obtenerIdDispositivo()
                val respaldos = statisticsRepository.getBackupsByDeviceId(deviceId)
                _uiState.value = if (respaldos.isEmpty()) {
                    StatisticsUiState.Empty
                } else {
                    StatisticsUiState.Success(respaldos)
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
                val deviceId = obtenerIdDispositivo()
                val estadisticas = ganadoUseCase.obtenerEstadisticasGanado()
                val request = mapGanadoEstadisticaToRespaldoRequest(estadisticas, deviceId)

                if (request.totalMachos == 0 &&
                    request.totalHembras == 0 &&
                    request.detalleMachos.becerro == 0 &&
                    request.detalleMachos.torito == 0 &&
                    request.detalleMachos.toro == 0 &&
                    request.detalleHembras.becerra == 0 &&
                    request.detalleHembras.vaca == 0 &&
                    request.estadoAnimales.activos == 0 &&
                    request.estadoAnimales.vendidos == 0 &&
                    request.estadoAnimales.muertos == 0) {
                    _uiState.value = StatisticsUiState.Error("No hay datos para crear el respaldo.")
                    return@launch
                }

                statisticsRepository.createBackup(request)
                fetchBackups()
            } catch (e: Exception) {
                _uiState.value = StatisticsUiState.Error("Error al crear respaldo: ${e.message}")
            } finally {
                _isCreatingBackup.value = false
            }
        }
    }


    private fun obtenerIdDispositivo(): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "ID_DESCONOCIDO"
    }

    private fun mapGanadoEstadisticaToRespaldoRequest(estadisticas: GanadoEstadistica, deviceId: String): RespaldoRequest {
        return RespaldoRequest(
            idDispositivo = deviceId,
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
        private val context: Context,
        private val ganadoUseCase: GanadoUseCase,
        private val statisticsRepository: StatisticsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
                return StatisticsViewModel(context, ganadoUseCase, statisticsRepository) as T
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
