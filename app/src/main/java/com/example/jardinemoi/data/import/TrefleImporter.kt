package com.example.jardinemoi.data.import

import com.example.jardinemoi.data.trefle.TrefleClient
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TrefleImporter {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun importAllPlants(token: String) = withContext(Dispatchers.IO) {

        var page = 1
        var hasMore = true

        while (hasMore) {
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
            }

            page++
        }
    }
}
