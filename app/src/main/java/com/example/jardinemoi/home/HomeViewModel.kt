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

    private val _plantOfTheDay = MutableStateFlow<PlantInfo?>(null)
    val plantOfTheDay: StateFlow<PlantInfo?> = _plantOfTheDay

    private val _totalCarePoints = MutableStateFlow(0)
    val totalCarePointsState: StateFlow<Int> = _totalCarePoints

    init {
        viewModelScope.launch {
            repository.getMyPlants().collectLatest { list ->
                _myPlants.value = list
            }
        }
        viewModelScope.launch {
            repository.getTotalCarePoints().collectLatest { points ->
                _totalCarePoints.value = points
            }
        }
        loadPlantOfTheDay()
    }

    private fun loadPlantOfTheDay() {
        viewModelScope.launch {
            repository.getAllPlants().collectLatest { allPlants ->
                val filteredPlants = allPlants.filter { it.commonName != "Monstera" }
                if (filteredPlants.isNotEmpty()) {
                    _plantOfTheDay.value = filteredPlants.random()
                }
            }
        }
    }

    fun waterPlant(plantId: String) {
        viewModelScope.launch {
            repository.waterPlant(plantId)
        }
    }

    val totalCarePoints: Int
        get() = _totalCarePoints.value

    val gardenerLevel: Int
        get() = (totalCarePoints / 100) + 1

    val levelProgress: Float
        get() = (totalCarePoints % 100) / 100f

    val thirstyCount: Int
        get() = _myPlants.value.count { plant ->
            val now = System.currentTimeMillis()
            val diffMs = now - plant.lastWateredDate
            val diffDays = diffMs / (1000 * 60 * 60 * 24)
            diffDays >= plant.wateringFrequencyDays
        }

    val readyToEvolveCount: Int
        get() = _myPlants.value.count { it.growthProgress >= 1f && it.currentStage != "Mature" }

}
