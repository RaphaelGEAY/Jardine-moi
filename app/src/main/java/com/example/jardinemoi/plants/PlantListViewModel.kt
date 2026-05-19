package com.example.jardinemoi.plants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jardinemoi.data.model.PlantInfo
import com.example.jardinemoi.data.repository.PlantRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class PlantListViewModel(
    private val repository: PlantRepository = PlantRepository()
) : ViewModel() {

    private val _plants = MutableStateFlow<List<PlantInfo>>(emptyList())
    val plants: StateFlow<List<PlantInfo>> = _plants

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private var loadJob: Job? = null
    private var searchJob: Job? = null

    init {
        loadFirestorePlants()
    }

    private fun loadFirestorePlants() {
        searchJob?.cancel()
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            repository.getAllPlants().collectLatest { list ->
                if (_searchQuery.value.isEmpty()) {
                    _plants.value = list.filter { it.commonName != "Monstera" }
                }
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
        searchJob?.cancel()
        if (newQuery.length >= 3) {
            searchJob = viewModelScope.launch {
                delay(500) // Debounce de 500ms
                searchOnline(newQuery)
            }
        } else if (newQuery.isEmpty()) {
            loadFirestorePlants()
        }
    }

    private suspend fun searchOnline(query: String) {
        _isSearching.value = true
        try {
            val results = repository.searchPlantsOnline(query)
            _plants.value = results.filter { it.commonName != "Monstera" }
        } catch (e: Exception) {
            // Gérer l'erreur si besoin
        } finally {
            _isSearching.value = false
        }
    }
}
