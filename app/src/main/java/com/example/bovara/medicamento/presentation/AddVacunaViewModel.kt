package com.example.bovara.medicamento.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bovara.ganado.domain.GanadoUseCase
import com.example.bovara.medicamento.domain.MedicamentoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

class AddVacunaViewModel(
    private val ganadoId: Int,
    private val medicamentoUseCase: MedicamentoUseCase,
    private val ganadoUseCase: GanadoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AddVacunaState())
    val state: StateFlow<AddVacunaState> = _state

    init {
        loadGanadoInfo()

        // Valores por defecto para registro individual de vacunas
        _state.update { it.copy(
            tipo = "vacuna",
            aplicado = true,          // Siempre es aplicado para vacunas individuales
            esProgramado = false,     // Nunca es programado para vacunas individuales
            fechaAplicacion = Date()  // Fecha actual por defecto
        )}

        checkCanSave()
    }

    private fun loadGanadoInfo() {
        viewModelScope.launch {
            ganadoUseCase.getGanadoById(ganadoId).collect { ganado ->
                ganado?.let {
                    _state.update { state ->
                        state.copy(
                            ganadoInfo = Pair(ganado.apodo, ganado.numeroArete)
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: AddVacunaEvent) {
        when (event) {
            is AddVacunaEvent.NombreChanged -> {
                _state.update { it.copy(nombre = event.value) }
                validateNombre()
                checkCanSave()
            }
            is AddVacunaEvent.DescripcionChanged -> {
                _state.update { it.copy(descripcion = event.value) }
                validateDescripcion()
                checkCanSave()
            }
            is AddVacunaEvent.TipoChanged -> {
                _state.update { it.copy(tipo = event.value) }
            }
            is AddVacunaEvent.DosisMLChanged -> {
                _state.update { it.copy(dosisML = event.value) }
                validateDosisML()
                checkCanSave()
            }
            is AddVacunaEvent.AplicadoChanged -> {
                val aplicado = event.value
                _state.update {
                    it.copy(
                        aplicado = aplicado,
                        // Si está aplicado, no puede estar programado
                        esProgramado = if (aplicado) false else it.esProgramado
                    )
                }
            }
            is AddVacunaEvent.EsProgramadoChanged -> {
                val esProgramado = event.value
                _state.update {
                    it.copy(
                        esProgramado = esProgramado,
                        // Si está programado, no puede estar aplicado
                        aplicado = if (esProgramado) false else it.aplicado,
                        // Si está programado, se usa fechaProgramada en lugar de fechaAplicacion
                        fechaProgramada = if (esProgramado && it.fechaProgramada == null)
                            Date() else it.fechaProgramada
                    )
                }
            }
            is AddVacunaEvent.FechaAplicacionChanged -> {
                _state.update { it.copy(fechaAplicacion = event.value) }
            }
            is AddVacunaEvent.FechaProgramadaChanged -> {
                _state.update { it.copy(fechaProgramada = event.value) }
            }
            is AddVacunaEvent.RecordatorioChanged -> {
                _state.update { it.copy(recordatorio = event.value) }
            }
            is AddVacunaEvent.NotasChanged -> {
                _state.update { it.copy(notas = event.value) }
            }
            is AddVacunaEvent.SaveVacuna -> saveVacuna()
        }
    }

    private fun validateNombre() {
        val nombre = _state.value.nombre
        val error = when {
            nombre.isBlank() -> "El nombre es requerido"
            nombre.length < 2 -> "El nombre debe tener al menos 2 caracteres"
            else -> null
        }
        _state.update { it.copy(nombreError = error) }
    }

    private fun validateDescripcion() {
        val descripcion = _state.value.descripcion
        val error = when {
            descripcion.isBlank() -> "La descripción es requerida"
            descripcion.length < 5 -> "La descripción debe ser más detallada"
            else -> null
        }
        _state.update { it.copy(descripcionError = error) }
    }

    private fun validateDosisML() {
        val dosisMLStr = _state.value.dosisML
        val error = try {
            val dosisML = dosisMLStr.toFloatOrNull() ?: 0f
            when {
                dosisMLStr.isBlank() -> "La dosis es requerida"
                dosisML <= 0f -> "La dosis debe ser mayor que 0"
                else -> null
            }
        } catch (e: Exception) {
            "Formato de dosis inválido"
        }
        _state.update { it.copy(dosisMLError = error) }
    }

    private fun checkCanSave() {
        val state = _state.value
        val dosisValid = try {
            state.dosisML.toFloatOrNull()?.let { it > 0 } ?: false
        } catch (e: Exception) {
            false
        }

        val canSave = state.nombre.isNotBlank() &&
                state.nombreError == null &&
                state.descripcion.isNotBlank() &&
                state.descripcionError == null &&
                dosisValid &&
                state.dosisMLError == null &&
                (state.aplicado && state.fechaAplicacion != null)

        _state.update { it.copy(canSave = canSave) }
    }

    private fun saveVacuna() {
        val state = _state.value

        if (!state.canSave || state.isLoading) return

        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val dosis = state.dosisML.toFloatOrNull() ?: 0f

                medicamentoUseCase.saveMedicamento(
                    nombre = state.nombre,
                    descripcion = state.descripcion,
                    fechaAplicacion = state.fechaAplicacion ?: Date(),
                    dosisML = dosis,
                    ganadoId = ganadoId,
                    tipo = state.tipo,
                    esProgramado = false, // Nunca es programado para vacunas individuales
                    aplicado = true, // Siempre es aplicado para vacunas individuales
                    notas = state.notas.takeIf { it.isNotBlank() }
                )

                _state.update {
                    it.copy(
                        isLoading = false,
                        savedSuccessfully = true
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al guardar el medicamento"
                    )
                }
            }
        }
    }

    class Factory(
        private val ganadoId: Int,
        private val medicamentoUseCase: MedicamentoUseCase,
        private val ganadoUseCase: GanadoUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddVacunaViewModel::class.java)) {
                return AddVacunaViewModel(
                    ganadoId,
                    medicamentoUseCase,
                    ganadoUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class AddVacunaState(
    val nombre: String = "",
    val nombreError: String? = null,
    val descripcion: String = "",
    val descripcionError: String? = null,
    val tipo: String = "vacuna",
    val dosisML: String = "",
    val dosisMLError: String? = null,
    val ganadoInfo: Pair<String?, String>? = null,
    val aplicado: Boolean = true,
    val esProgramado: Boolean = false,
    val fechaAplicacion: Date? = Date(),
    val fechaProgramada: Date? = null,
    val recordatorio: Boolean = false,
    val notas: String = "",
    val isLoading: Boolean = false,
    val canSave: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val error: String? = null
)

sealed class AddVacunaEvent {
    data class NombreChanged(val value: String) : AddVacunaEvent()
    data class DescripcionChanged(val value: String) : AddVacunaEvent()
    data class TipoChanged(val value: String) : AddVacunaEvent()
    data class DosisMLChanged(val value: String) : AddVacunaEvent()
    data class AplicadoChanged(val value: Boolean) : AddVacunaEvent()
    data class EsProgramadoChanged(val value: Boolean) : AddVacunaEvent()
    data class FechaAplicacionChanged(val value: Date) : AddVacunaEvent()
    data class FechaProgramadaChanged(val value: Date) : AddVacunaEvent()
    data class RecordatorioChanged(val value: Boolean) : AddVacunaEvent()
    data class NotasChanged(val value: String) : AddVacunaEvent()
    object SaveVacuna : AddVacunaEvent()
}