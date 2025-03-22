// File: app/src/main/java/com/example/bovara/medicamento/presentation/BatchDetailViewModel.kt
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Modelo para representar un animal con su vacuna correspondiente
data class GanadoWithVacuna(
    val ganado: GanadoEntity,
    val vacuna: MedicamentoEntity
)

class BatchDetailViewModel(
    private val lote: String,
    private val medicamentoUseCase: MedicamentoUseCase,
    private val ganadoUseCase: GanadoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BatchDetailState())
    val state: StateFlow<BatchDetailState> = _state

    init {
        loadBatchDetails()
    }

    private fun loadBatchDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                // Cargar vacunas del lote
                val vacunas = medicamentoUseCase.getMedicamentosByLote(lote).first()

                // Si hay vacunas, cargar los animales correspondientes
                if (vacunas.isNotEmpty()) {
                    val animalesWithVacunas = mutableListOf<GanadoWithVacuna>()

                    for (vacuna in vacunas) {
                        val ganado = ganadoUseCase.getGanadoById(vacuna.ganadoId).first()
                        ganado?.let {
                            animalesWithVacunas.add(GanadoWithVacuna(it, vacuna))
                        }
                    }

                    _state.update {
                        it.copy(
                            vacunas = vacunas,
                            animales = animalesWithVacunas,
                            isLoading = false
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "No se encontraron vacunas para este lote"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar detalles del lote"
                    )
                }
            }
        }
    }

    // Factory para crear el ViewModel
    class Factory(
        private val lote: String,
        private val medicamentoUseCase: MedicamentoUseCase,
        private val ganadoUseCase: GanadoUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BatchDetailViewModel::class.java)) {
                return BatchDetailViewModel(
                    lote,
                    medicamentoUseCase,
                    ganadoUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class BatchDetailState(
    val vacunas: List<MedicamentoEntity> = emptyList(),
    val animales: List<GanadoWithVacuna> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)