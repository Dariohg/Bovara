package com.example.bovara.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bovara.ganado.data.model.GanadoEntity
import com.example.bovara.ganado.domain.GanadoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedType = MutableStateFlow<String?>(null)
    val selectedType: StateFlow<String?> = _selectedType

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _isLoading.value = true
            // La lógica se ejecuta automáticamente a través de los StateFlows
            _isLoading.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun filterByType(type: String?) {
        _selectedType.value = type
    }

    /**
     * Factory para crear instancias de HomeViewModel con las dependencias necesarias
     */
    class Factory(private val ganadoUseCase: GanadoUseCase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(ganadoUseCase) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}