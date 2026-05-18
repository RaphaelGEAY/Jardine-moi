package com.example.jardinemoi.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jardinemoi.data.model.PlantInfo
import com.example.jardinemoi.data.repository.PlantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: PlantRepository = PlantRepository()
) : ViewModel() {

    private val _myPlants = MutableStateFlow<List<PlantInfo>>(emptyList())
    val myPlants: StateFlow<List<PlantInfo>> = _myPlants

    init {
        viewModelScope.launch {
            repository.getMyPlants().collectLatest { list ->
                _myPlants.value = list
            }
        }
    }

    fun waterPlant(plantId: String) {
        viewModelScope.launch {
            repository.waterPlant(plantId)
        }
    }

    fun getTotalCarePoints(): Int {
        return _myPlants.value.sumOf { it.carePoints }
    }
}
