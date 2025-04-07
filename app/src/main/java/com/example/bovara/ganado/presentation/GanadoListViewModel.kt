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

class GanadoListViewModel(
    private val ganadoUseCase: GanadoUseCase,
    initialSearchQuery: String = ""
) : ViewModel() {

    private val _state = MutableStateFlow(GanadoListState())
    val state: StateFlow<GanadoListState> = _state

    private var allGanado = listOf<GanadoEntity>()

    init {
        loadGanado()
        if (initialSearchQuery.isNotEmpty()) {
            updateSearchQuery(initialSearchQuery)
        }
    }

    private fun loadGanado() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            ganadoUseCase.getAllGanado().collect { ganado ->
                allGanado = ganado
                filterGanado()
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun setListMode(mode: GanadoListMode) {
        _state.update { it.copy(listMode = mode) }
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        filterGanado()
    }

    private fun filterGanado() {
        val query = _state.value.searchQuery
        val filtered = if (query.isBlank()) {
            allGanado
        } else {
            allGanado.filter { it.numeroArete.contains(query, ignoreCase = true) }
        }

        _state.update { it.copy(filteredGanado = filtered) }
    }

    class Factory(
        private val ganadoUseCase: GanadoUseCase,
        private val initialSearchQuery: String = ""
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GanadoListViewModel::class.java)) {
                return GanadoListViewModel(ganadoUseCase, initialSearchQuery) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class GanadoListState(
    val listMode: GanadoListMode = GanadoListMode.BY_DATE,
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val filteredGanado: List<GanadoEntity> = emptyList()
)