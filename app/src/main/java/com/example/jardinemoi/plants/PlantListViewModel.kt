package com.example.jardinemoi.plants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jardinemoi.data.model.PlantInfo
import com.example.jardinemoi.data.repository.PlantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlantListViewModel(
    private val repository: PlantRepository = PlantRepository()
) : ViewModel() {

    private val _plants = MutableStateFlow<List<PlantInfo>>(emptyList())
    val plants: StateFlow<List<PlantInfo>> = _plants

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    init {
        loadFirestorePlants()
    }

    private fun loadFirestorePlants() {
        viewModelScope.launch {
            repository.getAllPlants().collectLatest { list ->
                if (_searchQuery.value.isEmpty()) {
                    _plants.value = list
                }
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
        if (newQuery.length >= 3) {
            searchOnline(newQuery)
        } else if (newQuery.isEmpty()) {
            loadFirestorePlants()
        }
    }

    private fun searchOnline(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            val results = repository.searchPlantsOnline(query)
            _plants.value = results
            _isSearching.value = false
        }
    }
}
