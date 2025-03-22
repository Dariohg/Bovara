package com.example.bovara.ganado.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bovara.ganado.data.model.GanadoEntity
import com.example.bovara.ganado.domain.GanadoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GanadoDetailViewModel(
    private val ganadoId: Int,
    private val ganadoUseCase: GanadoUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(GanadoDetailState())
    val state: StateFlow<GanadoDetailState> = _state

    init {
        loadGanado()
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
        private val ganadoUseCase: GanadoUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GanadoDetailViewModel::class.java)) {
                return GanadoDetailViewModel(ganadoId, ganadoUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class GanadoDetailState(
    val ganado: GanadoEntity? = null,
    val isLoading: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null
)