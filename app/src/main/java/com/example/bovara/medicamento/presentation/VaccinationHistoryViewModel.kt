// File: app/src/main/java/com/example/bovara/medicamento/presentation/VaccinationHistoryViewModel.kt
package com.example.bovara.medicamento.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bovara.medicamento.domain.MedicamentoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

class VaccinationHistoryViewModel(
    private val medicamentoUseCase: MedicamentoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(VaccinationHistoryState())
    val state: StateFlow<VaccinationHistoryState> = _state

    init {
        loadVacunacionesHistorial()
    }

    private fun loadVacunacionesHistorial() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                medicamentoUseCase.getMedicamentosAplicados().collect { vacunas ->
                    // Agrupar por lote
                    val vacunasConLote = vacunas.filter { it.lote != null && it.aplicado }

                    // Agrupar por fecha
                    val vacunacionesPorFecha = vacunasConLote.groupBy { it.fechaAplicacion }

                    // Para cada fecha, agrupar por lote
                    val vacunacionesAgrupadas = vacunacionesPorFecha.map { (fecha, vacunasEnFecha) ->
                        val gruposPorLote = vacunasEnFecha
                            .groupBy { it.lote!! }
                            .map { (lote, vacunasEnLote) ->
                                VacunacionGrupo(
                                    lote = lote,
                                    medicamento = vacunasEnLote.first().nombre,
                                    cantidadAnimales = vacunasEnLote.size
                                )
                            }

                        fecha to gruposPorLote
                    }.toMap()

                    _state.update {
                        it.copy(
                            vacunacionesAgrupadas = vacunacionesAgrupadas,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = e.message ?: "Error al cargar el historial",
                        isLoading = false
                    )
                }
            }
        }
    }

    // Factory para crear el ViewModel
    class Factory(
        private val medicamentoUseCase: MedicamentoUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VaccinationHistoryViewModel::class.java)) {
                return VaccinationHistoryViewModel(
                    medicamentoUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class VaccinationHistoryState(
    val vacunacionesAgrupadas: Map<Date, List<VacunacionGrupo>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class VacunacionGrupo(
    val lote: String,
    val medicamento: String,
    val cantidadAnimales: Int
)