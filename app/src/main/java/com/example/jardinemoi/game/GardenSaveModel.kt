package com.example.jardinemoi.game

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import androidx.annotation.Keep

/**
 * Version simplifiée de l'état du jeu pour Firebase
 */
@Keep
data class GardenSaveData(
    @get:PropertyName("coins") @set:PropertyName("coins") var coins: Int = 240,
    @get:PropertyName("gems") @set:PropertyName("gems") var gems: Int = 14,
    @get:PropertyName("xp") @set:PropertyName("xp") var xp: Int = 0,
    @get:PropertyName("day") @set:PropertyName("day") var day: Int = 1,
    @get:PropertyName("compost") @set:PropertyName("compost") var compost: Int = 2,
    @get:PropertyName("rainTotems") @set:PropertyName("rainTotems") var rainTotems: Int = 1,
    @get:PropertyName("totalHarvests") @set:PropertyName("totalHarvests") var totalHarvests: Int = 0,
    @get:PropertyName("totalWaterings") @set:PropertyName("totalWaterings") var totalWaterings: Int = 0,
    @get:PropertyName("totalPlantsPlanted") @set:PropertyName("totalPlantsPlanted") var totalPlantsPlanted: Int = 0,
    @get:PropertyName("slots") @set:PropertyName("slots") var slots: List<GardenSlotData> = emptyList(),
    @get:PropertyName("inventory") @set:PropertyName("inventory") var inventory: Map<String, Int> = emptyMap(),
    @get:PropertyName("upgrades") @set:PropertyName("upgrades") var upgrades: Map<String, Int> = emptyMap(),
    @get:PropertyName("lastUpdate") @set:PropertyName("lastUpdate") var lastUpdate: Long = System.currentTimeMillis()
)

@Keep
data class GardenSlotData(
    @get:PropertyName("id") @set:PropertyName("id") var id: Int = 0,
    @get:PropertyName("isUnlocked") @set:PropertyName("isUnlocked") var isUnlocked: Boolean = false,
    @get:PropertyName("plantName") @set:PropertyName("plantName") var plantName: String = "VIDE",
    @get:PropertyName("progress") @set:PropertyName("progress") var progress: Long = 0,
    @get:PropertyName("water") @set:PropertyName("water") var water: Float = 0.7f,
    @get:PropertyName("fertilizer") @set:PropertyName("fertilizer") var fertilizer: Float = 0f,
    @get:PropertyName("starvationSeconds") @set:PropertyName("starvationSeconds") var starvationSeconds: Long = 0L,
    @get:PropertyName("plantedAt") @set:PropertyName("plantedAt") var plantedAt: Long = 0L,
    @get:PropertyName("lastUpdatedAt") @set:PropertyName("lastUpdatedAt") var lastUpdatedAt: Long = 0L
) {
    // Fallback pour le nom de champ "unlocked" parfois utilisé par Firebase pour les boéleens "is..."
    @get:PropertyName("unlocked") @set:PropertyName("unlocked")
    var unlockedFallback: Boolean
        @Exclude get() = isUnlocked
        set(value) { isUnlocked = value }
}
