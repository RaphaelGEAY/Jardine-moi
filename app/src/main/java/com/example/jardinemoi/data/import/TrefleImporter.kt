package com.example.jardinemoi.data.import

import android.util.Log
import com.example.jardinemoi.data.trefle.TrefleClient
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object TrefleImporter {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun importAllPlants(token: String, maxPages: Int = 5) = withContext(Dispatchers.IO) {

        var page = 1
        var hasMore = true

        while (hasMore && page <= maxPages) {
            try {
                val response = TrefleClient.api.getPlants(token, page)

                if (response.data.isEmpty()) {
                    hasMore = false
                    break
                }

                response.data.forEach { plant ->
                    val docId = plant.scientific_name
                        .lowercase()
                        .replace(" ", "_")
                        .replace(".", "_")

                    val data = mapOf(
                        "id" to docId, // Ajout d'un ID explicite pour faciliter la recherche
                        "species" to plant.scientific_name,
                        "commonName" to (plant.common_name ?: ""),
                        "family" to (plant.family ?: ""),
                        "genus" to (plant.genus ?: ""),
                        "imageUrl" to (plant.image_url ?: ""),
                        "category" to "Plante",
                        "wateringFrequencyDays" to 7,
                        "exposure" to "Mi-ombre"
                    )

                    firestore.collection("plants_info")
                        .document(docId)
                        .set(data)
                        .await() // Attendre la fin de l'écriture
                }
                Log.d("TrefleImporter", "Page $page importée avec succès")
                page++
            } catch (e: Exception) {
                Log.e("TrefleImporter", "Erreur lors de l'importation de la page $page: ${e.message}")
                hasMore = false
            }
        }
    }
}
