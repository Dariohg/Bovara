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
                // Normalizar la fecha para evitar problemas de zona horaria
                val normalizedDate = DateUtils.normalizeDateToLocalMidnight(event.value)
                _state.update { it.copy(fechaNacimiento = normalizedDate) }
            }
            is EditGanadoEvent.EstadoChanged -> {
                _state.update { it.copy(estado = event.value) }
            }
            is EditGanadoEvent.SaveGanado -> saveGanado()
        }
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
                    imagenUrl = state.ganado.imagenUrl,
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
    data class ApodoChanged(val value: String) : EditGanadoEvent()
    data class TipoChanged(val value: String) : EditGanadoEvent()
    data class ColorChanged(val value: String) : EditGanadoEvent()
    data class FechaNacimientoChanged(val value: Date) : EditGanadoEvent()
    data class EstadoChanged(val value: String) : EditGanadoEvent()
    object SaveGanado : EditGanadoEvent()
}