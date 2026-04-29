package com.example.jardinemoi.data.repository

import com.example.jardinemoi.data.model.PlantInfo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PlantRepository {

    private val firestore = FirebaseFirestore.getInstance()

    // 🔥 Récupération en temps réel de TOUTES les plantes
    fun getAllPlants(): Flow<List<PlantInfo>> = callbackFlow {
        val listener = firestore.collection("plants_info")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val plants = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(PlantInfo::class.java)
                    }
                    trySend(plants)
                }
            }

        awaitClose { listener.remove() }
    }

    // 🔥 Récupération d’une plante par son champ "id" (pas par l’ID Firestore auto-généré)
    suspend fun getPlantById(id: String): PlantInfo? {
        val snapshot = firestore.collection("plants_info")
            .whereEqualTo("id", id)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject(PlantInfo::class.java)
    }
}
