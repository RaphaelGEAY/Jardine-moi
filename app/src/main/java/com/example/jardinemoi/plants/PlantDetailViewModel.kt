package com.example.jardinemoi.plants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jardinemoi.data.model.PlantInfo
import com.example.jardinemoi.data.repository.PlantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch




class PlantDetailViewModel(
    private val repository: PlantRepository = PlantRepository()
) : ViewModel() {

    private val _plant = MutableStateFlow<PlantInfo?>(null)
    val plant: StateFlow<PlantInfo?> = _plant

    fun loadPlant(id: String) {
        viewModelScope.launch {
            _plant.value = repository.getPlantById(id)
        }
    }
}
