// File: app/src/main/java/com/example/bovara/medicamento/presentation/VacunasGanadoViewModel.kt
package com.example.bovara.medicamento.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bovara.ganado.data.model.GanadoEntity
import com.example.bovara.ganado.domain.GanadoUseCase
import com.example.bovara.medicamento.data.model.MedicamentoEntity
import com.example.bovara.medicamento.domain.MedicamentoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

class VacunasGanadoViewModel(
    private val ganadoId: Int,
    private val medicamentoUseCase: MedicamentoUseCase,
    private val ganadoUseCase: GanadoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(VacunasGanadoState())
    val state: StateFlow<VacunasGanadoState> = _state

    init {
        loadGanado()
        loadVacunas()
    }

    private fun loadGanado() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            ganadoUseCase.getGanadoById(ganadoId).collect { ganado ->
                _state.update { it.copy(ganado = ganado) }
            }
        }
    }

    private fun loadVacunas() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            medicamentoUseCase.getMedicamentosByGanadoId(ganadoId).collect { vacunas ->
                _state.update {
                    it.copy(
                        vacunas = vacunas,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun marcarVacunaComoAplicada(medicamentoId: Int) {
        viewModelScope.launch {
            try {
                medicamentoUseCase.marcarComoAplicado(medicamentoId)
                // El flujo se actualizará automáticamente a través de Room
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    // Factory para crear el ViewModel
    class Factory(
        private val ganadoId: Int,
        private val medicamentoUseCase: MedicamentoUseCase,
        private val ganadoUseCase: GanadoUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VacunasGanadoViewModel::class.java)) {
                return VacunasGanadoViewModel(
                    ganadoId,
                    medicamentoUseCase,
                    ganadoUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class VacunasGanadoState(
    val ganado: GanadoEntity? = null,
    val vacunas: List<MedicamentoEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)