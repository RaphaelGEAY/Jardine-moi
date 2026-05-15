package com.example.jardinemoi.data.model

data class PlantInfo(
    val id: String = "",
    val species: String = "",
    val commonName: String = "",
    val imageUrl: String = "",
    val family: String = "",
    val genus: String = "",
    val category: String = "Plante",
    
    // Paramètres de soin
    val wateringFrequencyDays: Int = 7,
    val exposure: String = "Mi-ombre",
    val potType: String = "Plastique",
    val season: String = "Printemps",
    
    // Suivi et Gamification
    val lastWateredDate: Long = System.currentTimeMillis(),
    val carePoints: Int = 0,
    val healthLevel: Int = 100,
    val currentStage: String = "Graine",
    val growthProgress: Float = 0f // 0.0 to 1.0
)
