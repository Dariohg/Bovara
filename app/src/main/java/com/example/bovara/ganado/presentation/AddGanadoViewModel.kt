package com.example.bovara.ganado.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bovara.core.utils.DateUtils
import com.example.bovara.ganado.data.model.GanadoEntity
import com.example.bovara.ganado.domain.GanadoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

class AddGanadoViewModel(
    private val ganadoUseCase: GanadoUseCase,
    private val madreId: Int? = null // Parámetro opcional para pre-seleccionar una madre
) : ViewModel() {

    private val _state = MutableStateFlow(AddGanadoState())
    val state: StateFlow<AddGanadoState> = _state

    init {
        // Si se proporciona un ID de madre, cargar los datos de esa madre
        madreId?.let { id ->
            viewModelScope.launch {
                try {
                    val madre = ganadoUseCase.getGanadoById(id).first()
                    madre?.let {
                        // Actualizar el estado con la madre seleccionada
                        _state.update { state ->
                            state.copy(
                                madreSeleccionada = madre,
                                madreId = madre.id
                            )
                        }
                    }
                } catch (e: Exception) {
                    // Manejar error si no se puede cargar la madre
                }
            }
        }

        // Cargar todas las vacas activas para la selección de madre
        loadVacasActivas()
    }

    private fun loadVacasActivas() {
        viewModelScope.launch {
            try {
                // En este punto, solo vamos a cargar las vacas (no becerras) como posibles madres
                // para mantener el comportamiento actual
                val vacas = ganadoUseCase.getGanadoByEstado("activo").first()
                    .filter { it.tipo == "vaca" } // Solo incluir vacas, no becerras

                _state.update { it.copy(vacasDisponibles = vacas) }
            } catch (e: Exception) {
                // Manejar error si no se pueden cargar las vacas
            }
        }
    }

    fun onEvent(event: AddGanadoEvent) {
        when (event) {
            is AddGanadoEvent.NumeroAreteChanged -> {
                _state.update { it.copy(numeroArete = event.value) }
                validateNumeroArete()
                checkCanSave()
            }
            is AddGanadoEvent.ApodoChanged -> {
                _state.update { it.copy(apodo = event.value) }
            }
            is AddGanadoEvent.SexoChanged -> {
                val newState = _state.value.copy(sexo = event.value)
                // Si cambia el sexo, resetear el tipo
                _state.update { newState.copy(tipo = "") }
                validateSexo()
                checkCanSave()
            }
            is AddGanadoEvent.TipoChanged -> {
                _state.update { it.copy(tipo = event.value) }
                validateTipo()
                checkCanSave()

                // Si el tipo es becerro, becerra o torito, mostrar selector de madre
                val esCria = event.value in listOf("becerro", "becerra", "torito")
                _state.update { it.copy(mostrarSelectorMadre = esCria) }
            }
            is AddGanadoEvent.ColorChanged -> {
                _state.update { it.copy(color = event.value) }
                validateColor()
                checkCanSave()
            }
            is AddGanadoEvent.FechaNacimientoChanged -> {
                // Normalizar la fecha para evitar problemas de zona horaria
                val normalizedDate = DateUtils.normalizeDateToLocalMidnight(event.value)
                _state.update { it.copy(fechaNacimiento = normalizedDate) }
            }
            is AddGanadoEvent.EstadoChanged -> {
                _state.update { it.copy(estado = event.value) }
            }
            is AddGanadoEvent.ImageUrlChanged -> {
                _state.update { it.copy(imagenUrl = event.value) }
            }
            is AddGanadoEvent.MadreSeleccionada -> {
                _state.update { it.copy(
                    madreSeleccionada = event.madre,
                    madreId = event.madre?.id
                ) }
            }
            is AddGanadoEvent.SaveGanado -> saveGanado()
            is AddGanadoEvent.CheckAreteExists -> checkAreteExists()
        }
    }

    private fun validateNumeroArete() {
        val numeroArete = _state.value.numeroArete
        val error = when {
            numeroArete.isBlank() -> "Número de arete requerido"
            !numeroArete.matches(Regex("^07\\d{8}$")) -> "Formato incorrecto. Debe ser 07 seguido de 8 dígitos"
            else -> null
        }
        _state.update { it.copy(numeroAreteError = error) }
    }

    private fun checkAreteExists() {
        val numeroArete = _state.value.numeroArete

        // No verificar si ya hay un error de formato o está vacío
        if (numeroArete.isBlank() || _state.value.numeroAreteError != null) {
            return
        }

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
                // Manejar errores de consulta si ocurren
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
        val canSave = state.numeroArete.isNotBlank() &&
                state.numeroAreteError == null &&
                state.sexo.isNotBlank() &&
                state.sexoError == null &&
                state.tipo.isNotBlank() &&
                state.tipoError == null &&
                state.color.isNotBlank() &&
                state.colorError == null

        _state.update { it.copy(canSave = canSave) }
    }

    private fun saveGanado() {
        val state = _state.value

        // No guardar si hay errores o si ya está en proceso
        if (!state.canSave || state.isLoading) return

        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // Verificar una última vez que el arete no exista
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

                // Llamar al caso de uso para guardar el ganado
                val id = ganadoUseCase.saveGanado(
                    numeroArete = state.numeroArete,
                    apodo = state.apodo.takeIf { it.isNotBlank() },
                    sexo = state.sexo,
                    tipo = state.tipo,
                    color = state.color,
                    fechaNacimiento = state.fechaNacimiento,
                    estado = state.estado,
                    imagenUrl = state.imagenUrl,
                    madreId = state.madreId // Ahora incluimos la madre
                )

                // Si la cría fue guardada exitosamente y tiene madre, registrar en el historial de crianza
                if (state.madreId != null && state.tipo in listOf("becerro", "becerra", "torito")) {
                    try {
                        // Usamos el CrianzaUseCase desde GanadoUseCase para registrar la crianza
                        ganadoUseCase.registrarCrianza(
                            madreId = state.madreId,
                            criaId = id.toInt(),
                            fechaNacimiento = state.fechaNacimiento ?: Date()
                        )
                    } catch (e: Exception) {
                        // Si falla el registro de crianza, aún así continuamos (ya se creó el animal)
                        _state.update {
                            it.copy(
                                error = "Animal guardado, pero hubo un error al registrar la relación con la madre: ${e.message}"
                            )
                        }
                    }
                }

                // Actualizar el estado con el ID del ganado guardado
                _state.update { it.copy(
                    isLoading = false,
                    savedGanadoId = id.toInt(),
                    error = null
                )}
            } catch (e: Exception) {
                // Detectar si el error es específicamente por arete duplicado
                val errorMessage = e.message ?: "Error al guardar el ganado"
                val isAreteError = errorMessage.contains("arete ya existe", ignoreCase = true)

                // Actualizar el estado con el error
                _state.update { it.copy(
                    isLoading = false,
                    error = errorMessage,
                    numeroAreteError = if (isAreteError) "Este número de arete ya está registrado" else null,
                    canSave = !isAreteError
                )}
            }
        }
    }

    class Factory(
        private val ganadoUseCase: GanadoUseCase,
        private val madreId: Int? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddGanadoViewModel::class.java)) {
                return AddGanadoViewModel(ganadoUseCase, madreId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class AddGanadoState(
    val numeroArete: String = "",
    val numeroAreteError: String? = null,
    val apodo: String = "",
    val sexo: String = "",
    val sexoError: String? = null,
    val tipo: String = "",
    val tipoError: String? = null,
    val color: String = "",
    val colorError: String? = null,
    val fechaNacimiento: Date? = null,
    val estado: String = "activo",
    val imagenUrl: String? = null,
    val isLoading: Boolean = false,
    val canSave: Boolean = false,
    val savedGanadoId: Int? = null,
    val error: String? = null,
    val vacasDisponibles: List<GanadoEntity> = emptyList(),
    val madreSeleccionada: GanadoEntity? = null,
    val madreId: Int? = null,
    val mostrarSelectorMadre: Boolean = false
)

sealed class AddGanadoEvent {
    data class NumeroAreteChanged(val value: String) : AddGanadoEvent()
    data class ApodoChanged(val value: String) : AddGanadoEvent()
    data class SexoChanged(val value: String) : AddGanadoEvent()
    data class TipoChanged(val value: String) : AddGanadoEvent()
    data class ColorChanged(val value: String) : AddGanadoEvent()
    data class FechaNacimientoChanged(val value: Date) : AddGanadoEvent()
    data class EstadoChanged(val value: String) : AddGanadoEvent()
    data class ImageUrlChanged(val value: String) : AddGanadoEvent()
    data class MadreSeleccionada(val madre: GanadoEntity?) : AddGanadoEvent()
    object SaveGanado : AddGanadoEvent()
    object CheckAreteExists : AddGanadoEvent()
}