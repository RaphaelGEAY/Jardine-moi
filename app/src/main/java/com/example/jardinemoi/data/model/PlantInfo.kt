package com.example.jardinemoi.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class PlantInfo(
    val id: String = "",
    val species: String = "",
    val commonName: String = "",
    val imageUrl: String = "",
    val family: String = "",
    val genus: String = "",
    val category: String = "Plante",
    
    // Paramètres de soin
    @PropertyName("wateringFrequencyDays")
    var wateringFrequencyDays: Int = 7,
    val exposure: String = "Mi-ombre",
    val potType: String = "Plastique",
    val season: String = "Printemps",
    
    // Suivi et Gamification
    val plantedAt: Long = System.currentTimeMillis(),
    val lastWateredDate: Long = System.currentTimeMillis(),
    val healthLevel: Int = 100,
    val carePoints: Int = 0
) {
    // Calcul dynamique du stade et du progrès en temps réel
    @get:Exclude
    val currentStage: String
        get() {
            val progress = calculateGrowthProgress(System.currentTimeMillis())
            return when {
                progress >= 1f -> "Mature"
                progress >= 0.6f -> "Croissance"
                progress >= 0.2f -> "Jeune pousse"
                else -> "Graine"
            }
        }

    @get:Exclude
    val growthProgress: Float
        get() = calculateGrowthProgress(System.currentTimeMillis())

    fun calculateGrowthProgress(now: Long): Float {
        val totalDurationMs = (wateringFrequencyDays * 24 * 60 * 60 * 1000L) / 2
        val elapsed = now - plantedAt
        return (elapsed.toFloat() / totalDurationMs).coerceIn(0f, 1f)
    }

    @get:Exclude
    val timeToNextStageMs: Long?
        get() = calculateTimeToNextStageMs(System.currentTimeMillis())

    fun calculateTimeToNextStageMs(now: Long): Long? {
        val totalDurationMs = (wateringFrequencyDays * 24 * 60 * 60 * 1000L) / 2
        val elapsed = now - plantedAt
        val progress = elapsed.toFloat() / totalDurationMs

        val nextThreshold = when {
            progress < 0.2f -> 0.2f
            progress < 0.6f -> 0.6f
            progress < 1.0f -> 1.0f
            else -> return null // Déjà mature
        }

        return ((nextThreshold * totalDurationMs) - elapsed).toLong().coerceAtLeast(0L)
    }
}
