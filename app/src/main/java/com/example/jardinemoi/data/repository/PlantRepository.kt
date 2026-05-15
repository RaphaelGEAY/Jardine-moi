package com.example.jardinemoi.data.repository

import com.example.jardinemoi.data.model.PlantInfo
import com.example.jardinemoi.data.trefle.TrefleClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PlantRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val trefleApi = TrefleClient.api
    private val trefleToken = "usr-4etX1kCVL1hmpInC-j86YfZCDbh0PSOgTV4KXVN4WJw" // À remplacer par ton vrai token

    // 🔥 Récupération en temps réel de TOUTES les plantes
    fun getAllPlants(): Flow<List<PlantInfo>> = callbackFlow {
        val listener = firestore.collection("plants_info")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val plants = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(PlantInfo::class.java)?.copy(id = doc.id)
                    }
                    trySend(plants)
                }
            }

        awaitClose { listener.remove() }
    }

    // 🔥 Récupération d’une plante par son identifiant unique
    suspend fun getPlantById(id: String): PlantInfo? {
        // 1. On cherche d'abord dans notre catalogue local (Firestore) par ID de document
        try {
            val doc = firestore.collection("plants_info").document(id).get().await()
            if (doc.exists()) {
                val plant = doc.toObject(PlantInfo::class.java)
                return plant?.copy(id = doc.id)
            }
        } catch (e: Exception) {}

        // 2. Fallback : Si l'ID de document n'a rien donné, on cherche par le champ "id" interne
        try {
            val snapshot = firestore.collection("plants_info")
                .whereEqualTo("id", id)
                .get()
                .await()
            if (!snapshot.isEmpty) {
                val doc = snapshot.documents.first()
                return doc.toObject(PlantInfo::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {}

        // 3. Fallback ultime : On cherche par nom commun (pour réparer les erreurs d'ID)
        try {
            val snapshot = firestore.collection("plants_info")
                .whereEqualTo("commonName", id)
                .get()
                .await()
            if (!snapshot.isEmpty) {
                val doc = snapshot.documents.first()
                return doc.toObject(PlantInfo::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {}

        // 4. Si c'est un ID numérique, on cherche sur Trefle (en ligne)
        if (id.all { it.isDigit() }) {
            return try {
                val response = trefleApi.getPlant(id, trefleToken)
                val detail = response.data
                val species = detail.main_species
                PlantInfo(
                    id = detail.id.toString(),
                    commonName = detail.common_name ?: detail.scientific_name,
                    species = detail.scientific_name,
                    family = species?.family ?: "Inconnue",
                    genus = species?.genus ?: "Inconnu",
                    imageUrl = species?.image_url ?: "",
                    exposure = when (species?.growth?.light) {
                        in 0..3 -> "Ombre"
                        in 4..6 -> "Mi-ombre"
                        in 7..10 -> "Plein soleil"
                        else -> "Non spécifiée"
                    },
                    wateringFrequencyDays = 7
                )
            } catch (e: Exception) {
                null
            }
        }
        
        return null
    }

    suspend fun addToMyPlants(plant: PlantInfo): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Utilisateur non connecté"))
        val plantId = plant.id.ifEmpty { plant.commonName.filter { it.isLetterOrDigit() } }
        
        return try {
            val finalPlant = plant.copy(id = plantId)
            firestore.collection("users")
                .document(userId)
                .collection("my_plants")
                .document(plantId)
                .set(finalPlant)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchPlantsOnline(query: String): List<PlantInfo> {
        return try {
            val response = trefleApi.searchPlants(trefleToken, query)
            response.data.map { treflePlant ->
                PlantInfo(
                    id = treflePlant.id.toString(),
                    commonName = treflePlant.common_name ?: treflePlant.scientific_name,
                    species = treflePlant.scientific_name,
                    family = treflePlant.family ?: "Inconnue",
                    genus = treflePlant.genus ?: "Inconnu",
                    imageUrl = treflePlant.image_url ?: "",
                    exposure = "Non spécifiée",
                    wateringFrequencyDays = 7
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getMyPlants(): Flow<List<PlantInfo>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: return@callbackFlow
        val listener = firestore.collection("users")
            .document(userId)
            .collection("my_plants")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val plants = snapshot.documents.mapNotNull { doc ->
                        val plant = doc.toObject(PlantInfo::class.java)
                        // On s'assure que l'ID est bien celui du document
                        val finalPlant = plant?.copy(id = doc.id) ?: return@mapNotNull null
                        applyTimeDecay(finalPlant)
                    }
                    trySend(plants)
                }
            }
        awaitClose { listener.remove() }
    }

    // Récupérer une plante spécifiquement dans MA liste
    suspend fun getMyPlantById(plantId: String): PlantInfo? {
        val userId = auth.currentUser?.uid ?: return null
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .collection("my_plants")
                .document(plantId)
                .get()
                .await()
            doc.toObject(PlantInfo::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    // Calcule la perte de santé si la plante est assoiffée
    private fun applyTimeDecay(plant: PlantInfo): PlantInfo {
        val now = System.currentTimeMillis()
        val diffMs = now - plant.lastWateredDate
        val diffDays = diffMs / (1000 * 60 * 60 * 24)
        
        // Si on dépasse la fréquence d'arrosage, la santé baisse
        return if (diffDays > plant.wateringFrequencyDays) {
            val daysLate = (diffDays - plant.wateringFrequencyDays).toInt()
            val healthPenalty = daysLate * 5 // -5% par jour de retard
            plant.copy(healthLevel = (plant.healthLevel - healthPenalty).coerceAtLeast(0))
        } else {
            plant
        }
    }

    // 🔥 Marquer une plante comme arrosée et gagner des points
    suspend fun waterPlant(plantId: String): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Non connecté"))
        return try {
            val docRef = firestore.collection("users").document(userId)
                .collection("my_plants").document(plantId)
            
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentPoints = (snapshot.getLong("carePoints") ?: 0) + 10
                
                // Calcul de l'évolution
                val (newStage, progress) = when {
                    currentPoints >= 300 -> "Mature" to 1.0f
                    currentPoints >= 150 -> "Croissance" to (currentPoints - 150) / 150f
                    currentPoints >= 50 -> "Jeune pousse" to (currentPoints - 50) / 100f
                    else -> "Graine" to currentPoints / 50f
                }
                
                transaction.update(docRef, "lastWateredDate", System.currentTimeMillis())
                transaction.update(docRef, "carePoints", currentPoints)
                transaction.update(docRef, "currentStage", newStage)
                transaction.update(docRef, "growthProgress", progress)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromMyPlants(plantId: String): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Utilisateur non connecté"))
        if (plantId.isEmpty()) return Result.failure(Exception("ID de plante invalide"))

        return try {
            firestore.collection("users")
                .document(userId)
                .collection("my_plants")
                .document(plantId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fonction de secours pour tout nettoyer en cas de bug d'ID
    suspend fun deleteAllMyPlants(): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Non connecté"))
        return try {
            val colRef = firestore.collection("users").document(userId).collection("my_plants")
            val snapshot = colRef.get().await()
            for (doc in snapshot.documents) {
                doc.reference.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
