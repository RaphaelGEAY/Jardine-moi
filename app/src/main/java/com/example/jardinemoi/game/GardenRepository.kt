package com.example.jardinemoi.game

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

object GardenRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun getUserDoc() = auth.currentUser?.uid?.let { uid ->
        db.collection("users").document(uid).collection("game").document("state")
    }

    suspend fun saveGame(state: GardenGameState): Boolean {
        val doc = getUserDoc()
        if (doc == null) {
            Log.e("GardenRepo", "ERREUR: Utilisateur non connecté, impossible de sauvegarder")
            return false
        }
        
        val saveData = GardenSaveData(
            coins = state.coins,
            gems = state.gems,
            xp = state.xp,
            day = state.day,
            compost = state.compost,
            rainTotems = state.rainTotems,
            totalHarvests = state.totalHarvests,
            totalWaterings = state.totalWaterings,
            totalPlantsPlanted = state.totalPlantsPlanted,
            slots = state.gardenSlots.map { slot ->
                GardenSlotData(
                    id = slot.id,
                    isUnlocked = slot.isUnlocked,
                    plantName = slot.plant.name,
                    progress = slot.progress,
                    water = slot.water,
                    fertilizer = slot.fertilizer,
                    starvationSeconds = slot.starvationSeconds,
                    plantedAt = slot.plantedAt,
                    lastUpdatedAt = slot.lastUpdatedAt
                )
            },
            inventory = state.produceInventory.mapKeys { it.key.name },
            upgrades = state.upgrades.mapKeys { it.key.name },
            lastUpdate = System.currentTimeMillis()
        )

        try {
            doc.set(saveData, SetOptions.merge()).await()
            Log.d("GardenRepo", "✅ Sauvegarde réussie pour: ${auth.currentUser?.email}")
            return true
        } catch (e: Exception) {
            Log.e("GardenRepo", "❌ ÉCHEC de sauvegarde: ${e.message}")
            return false
        }
    }

    suspend fun loadGame(): GardenSaveData? {
        val doc = getUserDoc() ?: return null
        return try {
            val snapshot = try {
                doc.get(Source.SERVER).await()
            } catch (serverError: Exception) {
                Log.w("GardenRepo", "⚠️ Chargement serveur impossible, fallback cache: ${serverError.message}")
                doc.get(Source.CACHE).await()
            }

            val data = snapshot.toObject(GardenSaveData::class.java)
            if (data != null) Log.d("GardenRepo", "✅ Données chargées avec succès")
            data
        } catch (e: Exception) {
            Log.e("GardenRepo", "❌ Erreur de chargement: ${e.message}")
            null
        }
    }
}
