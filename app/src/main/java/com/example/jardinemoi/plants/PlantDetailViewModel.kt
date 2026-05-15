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

    private var isOwnedLocal: Boolean = false

    fun loadPlant(id: String, isOwned: Boolean = false) {
        this.isOwnedLocal = isOwned
        // On réinitialise à null pour éviter d'afficher l'ancienne plante pendant le chargement
        _plant.value = null 

        viewModelScope.launch {
            // 1. Chercher d'abord dans "Mes plantes" (important)
            val ownedPlant = repository.getMyPlantById(id)
            if (ownedPlant != null) {
                _plant.value = ownedPlant
                this@PlantDetailViewModel.isOwnedLocal = true
                return@launch
            }
            
            // 2. Si pas trouvé dans le jardin, chercher dans le catalogue global ou Trefle
            val globalPlant = repository.getPlantById(id)
            if (globalPlant != null) {
                _plant.value = globalPlant
            }
        }
    }

    fun updatePotType(potType: String) {
        _plant.value = _plant.value?.copy(potType = potType)
    }

    fun updateSeason(season: String) {
        _plant.value = _plant.value?.copy(season = season)
    }

    fun updateExposure(exposure: String) {
        _plant.value = _plant.value?.copy(exposure = exposure)
    }

    fun addCurrentPlantToMyPlants(onComplete: (Boolean) -> Unit) {
        val current = _plant.value ?: return
        viewModelScope.launch {
            // On initialise la date du dernier arrosage au moment de l'ajout
            val finalPlant = current.copy(lastWateredDate = System.currentTimeMillis())
            val result = repository.addToMyPlants(finalPlant)
            onComplete(result.isSuccess)
        }
    }

    fun waterPlant() {
        val current = _plant.value ?: return
        viewModelScope.launch {
            repository.waterPlant(current.id)
            // On recharge avec le bon flag isOwned
            loadPlant(current.id, isOwnedLocal)
        }
    }

    fun removePlant(onComplete: (Boolean) -> Unit) {
        val current = _plant.value ?: return
        viewModelScope.launch {
            val result = repository.removeFromMyPlants(current.id)
            onComplete(result.isSuccess)
        }
    }
}
