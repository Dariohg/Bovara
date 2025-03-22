package com.example.bovara.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bovara.ganado.data.model.GanadoEntity
import com.example.bovara.ganado.domain.GanadoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val ganadoUseCase: GanadoUseCase
) : ViewModel() {

    val ganado: StateFlow<List<GanadoEntity>> =
        ganadoUseCase.getAllGanado()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _filteredGanado = MutableStateFlow<List<GanadoEntity>>(emptyList())
    val filteredGanado: StateFlow<List<GanadoEntity>> = _filteredGanado

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType: StateFlow<String?> = _selectedType

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive

    init {
        refreshData()
        // Observar los cambios en la consulta y actualizar los resultados filtrados
        viewModelScope.launch {
            combine(searchQuery, ganado) { query, ganadoList ->
                if (query.isBlank()) {
                    ganadoList
                } else {
                    ganadoList.filter {
                        it.numeroArete.contains(query, ignoreCase = true) ||
                                it.apodo?.contains(query, ignoreCase = true) == true
                    }
                }
            }.collect { filtered ->
                _filteredGanado.value = filtered
                _isSearchActive.value = _searchQuery.value.isNotBlank()
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun filterByType(type: String?) {
        _selectedType.value = type
    }

    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            // La lógica se ejecuta automáticamente a través de los StateFlows
            _isLoading.value = false
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _isSearchActive.value = false
    }
}