package com.example.jardinemoi.plants

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jardinemoi.data.model.PlantInfo
import com.example.jardinemoi.data.repository.PlantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job

class PlantDetailViewModel(
    private val repository: PlantRepository = PlantRepository()
) : ViewModel() {

    private val _plant = MutableStateFlow<PlantInfo?>(null)
    val plant: StateFlow<PlantInfo?> = _plant

    init {
        // Ticker temps réel pour la croissance : on réduit la fréquence à 5 secondes
        // car la croissance n'est pas visible à la seconde près.
        viewModelScope.launch {
            while (true) {
                delay(5000)
                _plant.value?.let { current ->
                    // On ne force la mise à jour que si on est sur l'écran
                    _plant.value = current.copy()
                }
            }
        }
    }

    private val _extractedColors = MutableStateFlow<List<Color>>(listOf(Color(0xFF4CAF50), Color(0xFF8BC34A)))
    val extractedColors: StateFlow<List<Color>> = _extractedColors

    private var loadJob: Job? = null
    private var isOwnedLocal: Boolean = false

    fun loadPlant(id: String, isOwned: Boolean = false, context: Context? = null) {
        // Éviter de recharger si c'est déjà la même plante
        if (_plant.value?.id == id && this.isOwnedLocal == isOwned) return
        
        this.isOwnedLocal = isOwned
        _plant.value = null
        loadJob?.cancel()

        loadJob = viewModelScope.launch {
            val ownedPlant = repository.getMyPlantById(id)
            if (ownedPlant != null) {
                _plant.value = ownedPlant
                this@PlantDetailViewModel.isOwnedLocal = true
                context?.let { extractColorsFromImage(it, ownedPlant.imageUrl) }
                return@launch
            }

            val globalPlant = repository.getPlantById(id)
            if (globalPlant != null) {
                _plant.value = globalPlant
                context?.let { extractColorsFromImage(it, globalPlant.imageUrl) }
            }
        }
    }

    private fun extractColorsFromImage(context: Context, imageUrl: String) {
        if (imageUrl.isEmpty()) return
        val appContext = context.applicationContext

        viewModelScope.launch {
            val loader = ImageLoader(appContext)
            val request = ImageRequest.Builder(appContext)
                .data(imageUrl)
                .allowHardware(false) // Nécessaire pour extraire le bitmap
                .build()

            val result = loader.execute(request)
            if (result is SuccessResult) {
                val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                bitmap?.let {
                    Palette.from(it).generate { palette ->
                        val colors = mutableListOf<Color>()
                        palette?.vibrantSwatch?.let { colors.add(Color(it.rgb)) }
                        palette?.mutedSwatch?.let { colors.add(Color(it.rgb)) }
                        palette?.dominantSwatch?.let { colors.add(Color(it.rgb)) }

                        if (colors.isNotEmpty()) {
                            _extractedColors.value = colors
                        }
                    }
                }
            }
        }
    }

    fun updatePotType(potType: String) {
        _plant.value = _plant.value?.copy(potType = potType)
    }

    fun updateExposure(exposure: String) {
        _plant.value = _plant.value?.copy(exposure = exposure)
    }

    fun addCurrentPlantToMyPlants(onComplete: (Boolean) -> Unit) {
        val current = _plant.value ?: return
        viewModelScope.launch {
            // On initialise la plante au stade de graine lors de l'ajout
            val finalPlant = current.copy(
                lastWateredDate = System.currentTimeMillis(),
                plantedAt = System.currentTimeMillis(),
                carePoints = 0,
                healthLevel = 100
            )
            val result = repository.addToMyPlants(finalPlant)
            onComplete(result.isSuccess)
        }
    }

    fun waterPlant() {
        val current = _plant.value ?: return
        viewModelScope.launch {
            repository.waterPlant(current.id)
            // On recharge la plante pour voir les changements de stade
            val updatedPlant = repository.getMyPlantById(current.id)
            if (updatedPlant != null) {
                _plant.value = updatedPlant
            }
        }
    }

    fun debugAccelerateGrowth() {
        val current = _plant.value ?: return
        viewModelScope.launch {
            repository.debugAccelerateGrowth(current.id)
            // Recharger pour voir l'effet immédiatement
            val updated = repository.getMyPlantById(current.id)
            if (updated != null) {
                _plant.value = updated
            }
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
