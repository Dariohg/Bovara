package com.example.bovara.ganado.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bovara.ganado.data.model.GanadoEntity
import com.example.bovara.ganado.domain.GanadoUseCase
import com.example.bovara.medicamento.data.model.MedicamentoEntity
import com.example.bovara.medicamento.domain.MedicamentoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GanadoDetailViewModel(
    private val ganadoId: Int,
    private val ganadoUseCase: GanadoUseCase,
    private val medicamentoUseCase: MedicamentoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(GanadoDetailState())
    val state: StateFlow<GanadoDetailState> = _state

    init {
        loadGanado()
        loadVacunasRecientes()
    }

    private fun loadVacunasRecientes() {
        viewModelScope.launch {
            medicamentoUseCase.getMedicamentosByGanadoId(ganadoId).collect { vacunas ->
                _state.update {
                    it.copy(
                        vacunasRecientes = vacunas
                            .filter { it.aplicado }
                            .sortedByDescending { it.fechaAplicacion }
                            .take(3)
                    )
                }
            }
        }
    }

    private fun loadGanado() {
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                ganadoUseCase.getGanadoById(ganadoId).collect { ganado ->
                    _state.update {
                        it.copy(
                            ganado = ganado,
                            isLoading = false,
                            error = null
                        )
                    }

                    // Si el animal tiene madre, cargarla
                    ganado?.madreId?.let { madreId ->
                        loadMadre(madreId)
                    }

                    // Si el animal es una vaca o una becerra, cargar sus crías
                    if (ganado?.tipo == "vaca" || ganado?.tipo == "becerra") {
                        loadCrias(ganado.id)
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al cargar los datos del animal"
                    )
                }
            }
        }
    }

    private fun loadMadre(madreId: Int) {
        viewModelScope.launch {
            try {
                ganadoUseCase.getGanadoById(madreId).collectLatest { madre ->
                    _state.update {
                        it.copy(madre = madre)
                    }
                }
            } catch (e: Exception) {
                // Si falla la carga de la madre, simplemente dejamos el valor como null
            }
        }
    }

    private fun loadCrias(madreId: Int) {
        viewModelScope.launch {
            try {
                ganadoUseCase.getCriasByMadreId(madreId).collectLatest { crias ->
                    _state.update {
                        it.copy(crias = crias)
                    }

                    // Si es una becerra y tiene alguna cría, convertirla a vaca
                    val currentGanado = _state.value.ganado
                    if (currentGanado?.tipo == "becerra" && crias.isNotEmpty()) {
                        convertBecerraToVaca(currentGanado)
                    }
                }
            } catch (e: Exception) {
                // Si falla la carga de crías, actualizamos el estado con lista vacía
                _state.update {
                    it.copy(crias = emptyList())
                }
            }
        }
    }

    // Función para convertir una becerra a vaca cuando tiene su primera cría
    private fun convertBecerraToVaca(becerra: GanadoEntity) {
        viewModelScope.launch {
            try {
                // Registrar lo que estamos haciendo para debug
                println("Convirtiendo becerra (id=${becerra.id}) a vaca")

                // Actualizar el tipo de becerra a vaca
                val updatedGanado = becerra.copy(tipo = "vaca")

                // Actualizar el ganado en la base de datos
                ganadoUseCase.updateGanado(updatedGanado)

                // Forzar una recarga de los datos para reflejar el cambio inmediatamente
                loadGanado()
            } catch (e: Exception) {
                println("Error al convertir becerra a vaca: ${e.message}")
                _state.update {
                    it.copy(error = "Error al actualizar el tipo del animal: ${e.message}")
                }
            }
        }
    }

    fun deleteGanado() {
        viewModelScope.launch {
            try {
                state.value.ganado?.let { ganado ->
                    _state.update { it.copy(isLoading = true) }
                    ganadoUseCase.deleteGanado(ganado)
                    _state.update { it.copy(isDeleted = true, isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al eliminar el animal"
                    )
                }
            }
        }
    }

    class Factory(
        private val ganadoId: Int,
        private val ganadoUseCase: GanadoUseCase,
        private val medicamentoUseCase: MedicamentoUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GanadoDetailViewModel::class.java)) {
                return GanadoDetailViewModel(ganadoId, ganadoUseCase, medicamentoUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class GanadoDetailState(
    val ganado: GanadoEntity? = null,
    val madre: GanadoEntity? = null,
    val crias: List<GanadoEntity> = emptyList(),
    val vacunasRecientes: List<MedicamentoEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null
)