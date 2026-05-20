package com.example.jardinemoi.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import androidx.annotation.Keep

@Keep
data class PlantInfo(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("species") @set:PropertyName("species") var species: String = "",
    @get:PropertyName("commonName") @set:PropertyName("commonName") var commonName: String = "",
    @get:PropertyName("imageUrl") @set:PropertyName("imageUrl") var imageUrl: String = "",
    @get:PropertyName("family") @set:PropertyName("family") var family: String = "",
    @get:PropertyName("genus") @set:PropertyName("genus") var genus: String = "",
    @get:PropertyName("category") @set:PropertyName("category") var category: String = "Plante",
    
    // Paramètres de soin
    @get:PropertyName("wateringFrequencyDays") @set:PropertyName("wateringFrequencyDays") var wateringFrequencyDays: Int = 7,
    @get:PropertyName("exposure") @set:PropertyName("exposure") var exposure: String = "Mi-ombre",
    @get:PropertyName("potType") @set:PropertyName("potType") var potType: String = "Plastique",
    
    // Suivi et Gamification
    @get:PropertyName("plantedAt") @set:PropertyName("plantedAt") var plantedAt: Long = System.currentTimeMillis(),
    @get:PropertyName("lastWateredDate") @set:PropertyName("lastWateredDate") var lastWateredDate: Long = System.currentTimeMillis(),
    @get:PropertyName("healthLevel") @set:PropertyName("healthLevel") var healthLevel: Int = 100,
    @get:PropertyName("carePoints") @set:PropertyName("carePoints") var carePoints: Int = 0,
    @get:PropertyName("completed") @set:PropertyName("completed") var completed: Boolean = false
) {
    // Fallback pour une éventuelle erreur de frappe dans la DB (espace à la fin)
    @get:PropertyName("wateringFrequencyDays ") @set:PropertyName("wateringFrequencyDays ")
    var wateringFrequencyDaysFallback: Int
        @Exclude get() = wateringFrequencyDays
        set(value) { wateringFrequencyDays = value }

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

    @get:Exclude
    val growthMultiplier: Float
        get() {
            val calendar = java.util.Calendar.getInstance()
            val month = calendar.get(java.util.Calendar.MONTH) + 1 // 0-indexed to 1-12
            
            val realSeason = when (month) {
                3, 4, 5 -> "Printemps"
                6, 7, 8 -> "Été"
                9, 10, 11 -> "Automne"
                else -> "Hiver"
            }
            
            return when (realSeason) {
                "Printemps" -> 1.5f
                "Été" -> 1.2f
                "Automne" -> 0.8f
                "Hiver" -> 0.4f
                else -> 1.0f
            }
        }

    fun calculateGrowthProgress(now: Long): Float {
        val totalDurationMs = (wateringFrequencyDays * 24 * 60 * 60 * 1000L) / 2
        val effectiveElapsed = (now - plantedAt) * growthMultiplier
        return (effectiveElapsed / totalDurationMs).coerceIn(0f, 1f)
    }

    @get:Exclude
    val timeToNextStageMs: Long?
        get() = calculateTimeToNextStageMs(System.currentTimeMillis())

    fun calculateTimeToNextStageMs(now: Long): Long? {
        val totalDurationMs = (wateringFrequencyDays * 24 * 60 * 60 * 1000L) / 2
        val effectiveElapsed = (now - plantedAt) * growthMultiplier
        val progress = effectiveElapsed / totalDurationMs

        val nextThreshold = when {
            progress < 0.2f -> 0.2f
            progress < 0.6f -> 0.6f
            progress < 1.0f -> 1.0f
            else -> return null // Déjà mature
        }

        val remainingEffectiveMs = (nextThreshold * totalDurationMs) - effectiveElapsed
        return (remainingEffectiveMs / growthMultiplier).toLong().coerceAtLeast(0L)
    }
}
