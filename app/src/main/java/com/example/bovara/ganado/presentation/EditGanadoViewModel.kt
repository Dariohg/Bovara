package com.example.bovara.ganado.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bovara.core.utils.DateUtils
import com.example.bovara.ganado.data.model.GanadoEntity
import com.example.bovara.ganado.domain.GanadoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

class EditGanadoViewModel(
    private val ganadoId: Int,
    private val ganadoUseCase: GanadoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(EditGanadoState(isInitialLoading = true))
    val state: StateFlow<EditGanadoState> = _state

    init {
        loadGanado()
    }

    private fun loadGanado() {
        viewModelScope.launch {
            try {
                ganadoUseCase.getGanadoById(ganadoId).collect { ganado ->
                    ganado?.let {
                        _state.update { currentState ->
                            currentState.copy(
                                ganado = ganado,
                                numeroArete = ganado.numeroArete,
                                apodo = ganado.apodo ?: "",
                                sexo = ganado.sexo,
                                tipo = ganado.tipo,
                                color = ganado.color,
                                fechaNacimiento = ganado.fechaNacimiento,
                                estado = ganado.estado,
                                canSave = true,
                                isInitialLoading = false,
                                isLoading = false,
                                error = null
                            )
                        }
                    } ?: _state.update {
                        it.copy(
                            isInitialLoading = false,
                            isLoading = false,
                            error = "Animal no encontrado"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isInitialLoading = false,
                        isLoading = false,
                        error = e.message ?: "Error al cargar los datos del animal"
                    )
                }
            }
        }
    }

    fun onEvent(event: EditGanadoEvent) {
        when (event) {
            is EditGanadoEvent.ApodoChanged -> {
                _state.update { it.copy(apodo = event.value) }
            }
            is EditGanadoEvent.TipoChanged -> {
                _state.update { it.copy(tipo = event.value) }
                validateTipo()
                checkCanSave()
            }
            is EditGanadoEvent.ColorChanged -> {
                _state.update { it.copy(color = event.value) }
                validateColor()
                checkCanSave()
            }
            is EditGanadoEvent.FechaNacimientoChanged -> {
                _state.update { it.copy(fechaNacimiento = event.value) }
            }
            is EditGanadoEvent.EstadoChanged -> {
                _state.update { it.copy(estado = event.value) }
            }
            is EditGanadoEvent.NumeroAreteChanged -> {
                _state.update { it.copy(numeroArete = event.value) }
                validateNumeroArete()
                checkCanSave()
            }
            is EditGanadoEvent.SexoChanged -> {
                val newState = _state.value.copy(sexo = event.value)
                // Si cambia el sexo, asegurarnos que el tipo es compatible
                if ((event.value == "macho" && !listOf("toro", "torito", "becerro").contains(newState.tipo)) ||
                    (event.value == "hembra" && !listOf("vaca", "becerra").contains(newState.tipo))) {
                    // Resetear el tipo si no es compatible con el nuevo sexo
                    _state.update { newState.copy(tipo = "") }
                } else {
                    _state.update { newState }
                }
                validateSexo()
                checkCanSave()
            }
            is EditGanadoEvent.ImageUrlChanged -> {
                _state.update { it.copy(imagenUrl = event.value) }
            }
            is EditGanadoEvent.SaveGanado -> saveGanado()
        }
    }


    private fun validateNumeroArete() {
        val numeroArete = _state.value.numeroArete
        val originalNumeroArete = _state.value.ganado?.numeroArete

        val error = when {
            numeroArete.isBlank() -> "Número de arete requerido"
            !numeroArete.matches(Regex("^07\\d{8}$")) -> "Formato incorrecto. Debe ser 07 seguido de 8 dígitos"
            else -> null
        }
        _state.update { it.copy(numeroAreteError = error) }

        // Si cambió el arete, verificar que no exista
        if (numeroArete != originalNumeroArete) {
            checkAreteExists()
        }
    }
    private fun checkAreteExists() {
        val numeroArete = _state.value.numeroArete
        val originalNumeroArete = _state.value.ganado?.numeroArete

        // Si el arete no cambió, no validamos
        if (numeroArete == originalNumeroArete) return

        viewModelScope.launch {
            try {
                val exists = ganadoUseCase.areteExists(numeroArete)
                if (exists) {
                    _state.update {
                        it.copy(
                            numeroAreteError = "Este número de arete ya está registrado",
                            canSave = false
                        )
                    }
                } else {
                    _state.update {
                        it.copy(numeroAreteError = null)
                    }
                    checkCanSave()
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = "Error al verificar el número de arete: ${e.message}")
                }
            }
        }
    }

    private fun validateSexo() {
        val sexo = _state.value.sexo
        val error = when {
            sexo.isBlank() -> "Seleccione el sexo del animal"
            else -> null
        }
        _state.update { it.copy(sexoError = error) }
    }

    private fun validateTipo() {
        val tipo = _state.value.tipo
        val sexo = _state.value.sexo
        val error = when {
            tipo.isBlank() -> "Seleccione el tipo de animal"
            sexo == "macho" && tipo !in listOf("toro", "torito", "becerro") ->
                "Tipo no válido para machos"
            sexo == "hembra" && tipo !in listOf("vaca", "becerra") ->
                "Tipo no válido para hembras"
            else -> null
        }
        _state.update { it.copy(tipoError = error) }
    }

    private fun validateColor() {
        val color = _state.value.color
        val error = when {
            color.isBlank() -> "Color requerido"
            else -> null
        }
        _state.update { it.copy(colorError = error) }
    }

    private fun checkCanSave() {
        val state = _state.value
        val hasChanges = state.ganado != null && (
                state.apodo != (state.ganado.apodo ?: "") ||
                        state.tipo != state.ganado.tipo ||
                        state.color != state.ganado.color ||
                        state.fechaNacimiento != state.ganado.fechaNacimiento ||
                        state.estado != state.ganado.estado
                )

        val canSave = hasChanges &&
                state.tipo.isNotBlank() &&
                state.tipoError == null &&
                state.color.isNotBlank() &&
                state.colorError == null

        _state.update { it.copy(canSave = canSave) }
    }

    private fun saveGanado() {
        val state = _state.value

        // No guardar si hay errores o si ya está en proceso
        if (!state.canSave || state.isLoading || state.ganado == null) return

        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // Llamar al caso de uso para guardar el ganado

                if (state.numeroArete != state.ganado.numeroArete) {
                    if (ganadoUseCase.areteExists(state.numeroArete)) {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                numeroAreteError = "Este número de arete ya está registrado",
                                canSave = false
                            )
                        }
                        return@launch
                    }
                }

                val id = ganadoUseCase.saveGanado(
                    id = ganadoId,
                    numeroArete = state.numeroArete,
                    apodo = state.apodo.takeIf { it.isNotBlank() },
                    sexo = state.sexo,
                    tipo = state.tipo,
                    color = state.color,
                    fechaNacimiento = state.fechaNacimiento,
                    estado = state.estado,
                    cantidadCrias = state.ganado.cantidadCrias,
                    madreId = state.ganado.madreId,
                    imagenUrl = state.imagenUrl ?: state.ganado.imagenUrl,
                    imagenesSecundarias = state.ganado.imagenesSecundarias
                )

                // Actualizar el estado para indicar que se guardó correctamente
                _state.update { it.copy(
                    isLoading = false,
                    updated = true,
                    error = null
                )}
            } catch (e: Exception) {
                // Actualizar el estado con el error
                _state.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al guardar los cambios"
                )}
            }
        }
    }

    class Factory(
        private val ganadoId: Int,
        private val ganadoUseCase: GanadoUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EditGanadoViewModel::class.java)) {
                return EditGanadoViewModel(ganadoId, ganadoUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class EditGanadoState(
    val numeroAreteError: String? = null,
    val sexoError: String? = null,
    val imagenUrl: String? = null,
    val ganado: GanadoEntity? = null,
    val numeroArete: String = "",
    val apodo: String = "",
    val sexo: String = "",
    val tipo: String = "",
    val tipoError: String? = null,
    val color: String = "",
    val colorError: String? = null,
    val fechaNacimiento: Date? = null,
    val estado: String = "activo",
    val isInitialLoading: Boolean = false,
    val isLoading: Boolean = false,
    val canSave: Boolean = false,
    val updated: Boolean = false,
    val error: String? = null
)

sealed class EditGanadoEvent {
    data class NumeroAreteChanged(val value: String) : EditGanadoEvent()
    data class SexoChanged(val value: String) : EditGanadoEvent()
    data class ImageUrlChanged(val value: String) : EditGanadoEvent()
    data class ApodoChanged(val value: String) : EditGanadoEvent()
    data class TipoChanged(val value: String) : EditGanadoEvent()
    data class ColorChanged(val value: String) : EditGanadoEvent()
    data class FechaNacimientoChanged(val value: Date) : EditGanadoEvent()
    data class EstadoChanged(val value: String) : EditGanadoEvent()
    object SaveGanado : EditGanadoEvent()
}