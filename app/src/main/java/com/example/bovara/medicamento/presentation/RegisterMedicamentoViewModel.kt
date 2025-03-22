// File: app/src/main/java/com/example/bovara/medicamento/presentation/RegisterMedicamentoViewModel.kt

package com.example.bovara.medicamento.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bovara.medicamento.data.model.MedicamentoEntity
import com.example.bovara.medicamento.domain.MedicamentoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterMedicamentoViewModel(
    private val medicamentoUseCase: MedicamentoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterMedicamentoState())
    val state: StateFlow<RegisterMedicamentoState> = _state

    fun onEvent(event: RegisterMedicamentoEvent) {
        when (event) {
            is RegisterMedicamentoEvent.NombreChanged -> {
                _state.update { it.copy(nombre = event.value) }
                validateNombre()
                checkCanSave()
            }
            is RegisterMedicamentoEvent.DescripcionChanged -> {
                _state.update { it.copy(descripcion = event.value) }
                validateDescripcion()
                checkCanSave()
            }
            is RegisterMedicamentoEvent.TipoChanged -> {
                _state.update { it.copy(tipo = event.value) }
            }
            is RegisterMedicamentoEvent.DosisMLChanged -> {
                _state.update { it.copy(dosisML = event.value) }
                validateDosisML()
                checkCanSave()
            }
            is RegisterMedicamentoEvent.NotasChanged -> {
                _state.update { it.copy(notas = event.value) }
            }
            is RegisterMedicamentoEvent.SaveMedicamento -> {
                saveMedicamento()
            }
            is RegisterMedicamentoEvent.ClearError -> {
                _state.update { it.copy(error = null) }
            }
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
                state.dosisMLError == null

        _state.update { it.copy(canSave = canSave) }
    }

    private fun saveMedicamento() {
        val state = _state.value

        if (!state.canSave || state.isLoading) return

        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val dosis = state.dosisML.toFloatOrNull() ?: 0f

                val id = medicamentoUseCase.saveMedicamento(
                    nombre = state.nombre,
                    descripcion = state.descripcion,
                    dosisML = dosis,
                    ganadoId = 0, // Esto asegura que se guarde como un medicamento genérico
                    tipo = state.tipo,
                    esProgramado = false,
                    notas = state.notas.takeIf { it.isNotBlank() }
                )

                // Obtener el medicamento guardado
                medicamentoUseCase.getMedicamentoById(id.toInt()).collect { medicamento ->
                    medicamento?.let {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                savedMedicamento = medicamento
                            )
                        }
                    }
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
        private val medicamentoUseCase: MedicamentoUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RegisterMedicamentoViewModel::class.java)) {
                return RegisterMedicamentoViewModel(medicamentoUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class RegisterMedicamentoState(
    val nombre: String = "",
    val nombreError: String? = null,
    val descripcion: String = "",
    val descripcionError: String? = null,
    val tipo: String = "vacuna",
    val dosisML: String = "5.0",
    val dosisMLError: String? = null,
    val notas: String = "",
    val isLoading: Boolean = false,
    val canSave: Boolean = false,
    val savedMedicamento: MedicamentoEntity? = null,
    val error: String? = null
)

sealed class RegisterMedicamentoEvent {
    data class NombreChanged(val value: String) : RegisterMedicamentoEvent()
    data class DescripcionChanged(val value: String) : RegisterMedicamentoEvent()
    data class TipoChanged(val value: String) : RegisterMedicamentoEvent()
    data class DosisMLChanged(val value: String) : RegisterMedicamentoEvent()
    data class NotasChanged(val value: String) : RegisterMedicamentoEvent()
    object SaveMedicamento : RegisterMedicamentoEvent()
    object ClearError : RegisterMedicamentoEvent()
}