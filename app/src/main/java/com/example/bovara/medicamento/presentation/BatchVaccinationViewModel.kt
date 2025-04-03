package com.example.bovara.medicamento.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bovara.core.utils.DateUtils
import com.example.bovara.ganado.data.model.GanadoEntity
import com.example.bovara.ganado.domain.GanadoUseCase
import com.example.bovara.medicamento.data.model.MedicamentoEntity
import com.example.bovara.medicamento.domain.MedicamentoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class BatchVaccinationViewModel(
    private val ganadoUseCase: GanadoUseCase,
    private val medicamentoUseCase: MedicamentoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(BatchVaccinationState())
    val state: StateFlow<BatchVaccinationState> = _state

    init {
        loadGanadoActivo()
        loadMedicamentosDisponibles()
        loadVacunacionPausada()
    }

    private fun loadGanadoActivo() {
        viewModelScope.launch {
            try {
                val animalesActivos = ganadoUseCase.getGanadoByEstado("activo").first()

                // Agrupar los animales por tipo
                val toros = animalesActivos.filter { it.tipo == "toro" }
                val toritos = animalesActivos.filter { it.tipo== "torito" }
                val vacas = animalesActivos.filter { it.tipo == "vaca" }
                val becerras = animalesActivos.filter { it.tipo == "becerra" }
                val becerros = animalesActivos.filter { it.tipo == "becerro" }
                val otros = animalesActivos.filter {
                    it.tipo != "toro" && it.tipo != "vaca" &&
                            it.tipo != "becerra" && it.tipo != "becerro" && it.tipo != "torito"
                }

                // Agrupar los animales
                val animalesAgrupados = AnimalesAgrupados(
                    toros = toros,
                    toritos = toritos,
                    vacas = vacas,
                    becerras = becerras,
                    becerros = becerros,
                    otros = otros
                )

                _state.update { it.copy(
                    animalesCandidatos = animalesActivos,
                    animalesAgrupados = animalesAgrupados
                )}
            } catch (e: Exception) {
                // Manejar error
                _state.update { it.copy(
                    error = e.message ?: "Error al cargar los animales"
                )}
            }
        }
    }

    private fun loadMedicamentosDisponibles() {
        viewModelScope.launch {
            try {
                medicamentoUseCase.getMedicamentosGenericos().collect { medicamentos ->
                    _state.update { it.copy(
                        medicamentosDisponibles = medicamentos
                    )}
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun loadVacunacionPausada() {
        // Aquí implementaremos la lógica para cargar una vacunación pausada
        // Por ahora, simplemente recuperamos los valores almacenados localmente
        // En una implementación más completa, esto vendría de una BD o SharedPreferences
    }

    fun onEvent(event: BatchVaccinationEvent) {
        when (event) {
            is BatchVaccinationEvent.MedicamentoSelected -> {
                _state.update { it.copy(
                    selectedMedicamento = event.medicamento,
                    dosisML = event.medicamento.dosisML
                )}
            }

            is BatchVaccinationEvent.NewMedicamentoCreated -> {
                viewModelScope.launch {
                    try {
                        // Validar los datos antes de intentar guardar
                        if (event.nombre.isBlank() || event.descripcion.isBlank()) {
                            _state.update { it.copy(
                                error = "El nombre y la descripción son requeridos"
                            )}
                            return@launch
                        }

                        // Crear nuevo medicamento en la BD con valores por defecto razonables
                        val id = medicamentoUseCase.saveMedicamento(
                            nombre = event.nombre,
                            descripcion = event.descripcion,
                            dosisML = 5.0f, // Valor por defecto
                            ganadoId = 0, // ID genérico para medicamentos de vacunación por lotes
                            tipo = "vacuna",
                            esProgramado = false, // Cambiar a false ya que se aplicará de inmediato
                            aplicado = false // Inicialmente no aplicado
                        )

                        // Obtener el medicamento recién creado
                        val nuevoMedicamento = medicamentoUseCase.getMedicamentoById(id.toInt()).first()

                        // Actualizar estado solo si se obtuvo el medicamento
                        nuevoMedicamento?.let { med ->
                            _state.update { it.copy(
                                selectedMedicamento = med,
                                dosisML = med.dosisML,
                                medicamentosDisponibles = it.medicamentosDisponibles + med,
                                error = null // Limpiar cualquier error previo
                            )}
                        } ?: run {
                            // Manejar el caso donde no se pudo obtener el medicamento
                            _state.update { it.copy(
                                error = "Error al obtener el medicamento creado"
                            )}
                        }
                    } catch (e: Exception) {
                        // Manejar errores específicamente
                        _state.update { it.copy(
                            error = "Error al crear medicamento: ${e.message}"
                        )}
                    }
                }
            }

            is BatchVaccinationEvent.DateChanged -> {
                _state.update { it.copy(
                    fechaAplicacion = it.fechaAplicacion
                )}
            }

            is BatchVaccinationEvent.DosisIncreased -> {
                _state.update { it.copy(
                    dosisML = it.dosisML + 0.5f
                )}
            }

            is BatchVaccinationEvent.DosisDecreased -> {
                if (_state.value.dosisML > 0.5f) {
                    _state.update { it.copy(
                        dosisML = it.dosisML - 0.5f
                    )}
                }
            }

            is BatchVaccinationEvent.ToggleAnimalSelection -> {
                val currentSelection = _state.value.animalesSeleccionados
                val newSelection = if (currentSelection.contains(event.animalId)) {
                    currentSelection - event.animalId
                } else {
                    currentSelection + event.animalId
                }

                _state.update { it.copy(
                    animalesSeleccionados = newSelection
                )}
            }

            is BatchVaccinationEvent.SelectAllAnimals -> {
                val allIds = _state.value.animalesCandidatos.map { it.id }.toSet()
                _state.update { it.copy(
                    animalesSeleccionados = allIds
                )}
            }

            is BatchVaccinationEvent.UnselectAllAnimals -> {
                _state.update { it.copy(
                    animalesSeleccionados = emptySet()
                )}
            }

            is BatchVaccinationEvent.FinishVaccination -> {
                saveVaccination(isPaused = false)
            }

            is BatchVaccinationEvent.PauseVaccination -> {
                saveVaccination(isPaused = true)
            }
        }
    }

    private fun saveVaccination(isPaused: Boolean) {
        viewModelScope.launch {
            try {
                val medicamento = _state.value.selectedMedicamento ?: return@launch
                val animalesIds = _state.value.animalesSeleccionados.toList()

                if (animalesIds.isEmpty()) return@launch

                // Guardar el lote de vacunaciones
                medicamentoUseCase.saveMedicamentosEnLote(
                    ganados = animalesIds,
                    nombre = medicamento.nombre,
                    descripcion = medicamento.descripcion,
                    fechaAplicacion = _state.value.fechaAplicacion,
                    dosisML = _state.value.dosisML,
                    tipo = "vacuna",
                    // Si está pausado, lo marcamos como programados para continuarlo después
                    esProgramado = isPaused,
                    // Pasar el valor de aplicado como parámetro
                    aplicado = !isPaused
                )

                // Si está finalizado (no pausado), reseteamos el estado
                if (!isPaused) {
                    _state.update { it.copy(
                        animalesSeleccionados = emptySet(),
                        isFinished = true
                    )}
                } else {
                    // Si está pausado, guardamos el estado actual
                    _state.update { it.copy(
                        isPaused = true
                    )}
                }
            } catch (e: Exception) {
                _state.update { it.copy(
                    error = e.message ?: "Error al guardar la vacunación"
                )}
            }
        }
    }

    // Factory para crear el ViewModel
    class Factory(
        private val ganadoUseCase: GanadoUseCase,
        private val medicamentoUseCase: MedicamentoUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BatchVaccinationViewModel::class.java)) {
                return BatchVaccinationViewModel(
                    ganadoUseCase,
                    medicamentoUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class BatchVaccinationState(
    val animalesCandidatos: List<GanadoEntity> = emptyList(),
    val animalesAgrupados: AnimalesAgrupados = AnimalesAgrupados(),
    val animalesSeleccionados: Set<Int> = emptySet(),
    val medicamentosDisponibles: List<MedicamentoEntity> = emptyList(),
    val selectedMedicamento: MedicamentoEntity? = null,
    val fechaAplicacion: Date = Date(),
    val dosisML: Float = 5.0f,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false,
    val error: String? = null
)

sealed class BatchVaccinationEvent {
    data class MedicamentoSelected(val medicamento: MedicamentoEntity) : BatchVaccinationEvent()
    data class NewMedicamentoCreated(val nombre: String, val descripcion: String) : BatchVaccinationEvent()
    data class DateChanged(val date: Date) : BatchVaccinationEvent()
    object DosisIncreased : BatchVaccinationEvent()
    object DosisDecreased : BatchVaccinationEvent()
    data class ToggleAnimalSelection(val animalId: Int) : BatchVaccinationEvent()
    object SelectAllAnimals : BatchVaccinationEvent()
    object UnselectAllAnimals : BatchVaccinationEvent()
    object FinishVaccination : BatchVaccinationEvent()
    object PauseVaccination : BatchVaccinationEvent()
}

data class AnimalesAgrupados(
    val toros: List<GanadoEntity> = emptyList(),
    val toritos: List<GanadoEntity> = emptyList(),
    val vacas: List<GanadoEntity> = emptyList(),
    val becerras: List<GanadoEntity> = emptyList(),
    val becerros: List<GanadoEntity> = emptyList(),
    val otros: List<GanadoEntity> = emptyList()
)