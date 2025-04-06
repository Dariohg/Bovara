package com.example.bovara.statistics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bovara.statistics.data.model.Respaldo
import com.example.bovara.statistics.domain.StatisticsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Estados de UI definidos en el mismo archivo del ViewModel
sealed class StatisticsUiState {
    object Loading : StatisticsUiState()
    object Empty : StatisticsUiState()
    data class Success(val respaldos: List<Respaldo>) : StatisticsUiState()
    data class Error(val message: String) : StatisticsUiState()
}

class StatisticsViewModel(private val useCase: StatisticsUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val uiState: StateFlow<StatisticsUiState> = _uiState

    private val _isCreatingBackup = MutableStateFlow(false)
    val isCreatingBackup: StateFlow<Boolean> = _isCreatingBackup

    init {
        fetchBackups()
    }

    fun fetchBackups() {
        viewModelScope.launch {
            _uiState.value = StatisticsUiState.Loading
            try {
                val respaldos = useCase.getRespaldos()
                if (respaldos.isEmpty()) {
                    _uiState.value = StatisticsUiState.Empty
                } else {
                    _uiState.value = StatisticsUiState.Success(respaldos)
                }
            } catch (e: Exception) {
                _uiState.value = StatisticsUiState.Error("Error al cargar datos: ${e.message}")
            }
        }
    }

    fun createBackup() {
        viewModelScope.launch {
            _isCreatingBackup.value = true
            try {
                val success = useCase.createRespaldo()
                if (success) {
                    fetchBackups()
                }
            } catch (e: Exception) {
                _uiState.value = StatisticsUiState.Error("Error al crear respaldo: ${e.message}")
            } finally {
                _isCreatingBackup.value = false
            }
        }
    }

    class Factory(private val useCase: StatisticsUseCase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
                return StatisticsViewModel(useCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}