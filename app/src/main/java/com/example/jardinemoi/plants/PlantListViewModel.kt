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

    init {
        viewModelScope.launch {
            repository.getAllPlants().collectLatest { list ->
                _plants.value = list
            }
        }
    }
}
