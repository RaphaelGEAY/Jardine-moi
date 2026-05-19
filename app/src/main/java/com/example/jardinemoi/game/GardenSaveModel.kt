package com.example.jardinemoi.game

import com.google.firebase.firestore.Exclude

/**
 * Version simplifiée de l'état du jeu pour Firebase
 */
data class GardenSaveData(
    val coins: Int = 240,
    val gems: Int = 14,
    val xp: Int = 0,
    val day: Int = 1,
    val compost: Int = 2,
    val rainTotems: Int = 1,
    val totalHarvests: Int = 0,
    val totalWaterings: Int = 0,
    val totalPlantsPlanted: Int = 0,
    val slots: List<GardenSlotData> = emptyList(),
    val inventory: Map<String, Int> = emptyMap(),
    val upgrades: Map<String, Int> = emptyMap(),
    val lastUpdate: Long = System.currentTimeMillis() // Ajout de l'horodatage
)

data class GardenSlotData(
    val id: Int = 0,
    val isUnlocked: Boolean = false,
    val plantName: String = "VIDE",
    val progress: Long = 0,
    val water: Float = 0.7f,
    val fertilizer: Float = 0f,
    val starvationSeconds: Long = 0L,
    // Les champs suivants sont cruciaux pour la synchronisation
    val plantedAt: Long = 0L,
    val lastUpdatedAt: Long = 0L
)
