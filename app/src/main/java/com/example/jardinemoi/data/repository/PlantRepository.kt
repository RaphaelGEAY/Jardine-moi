package com.example.jardinemoi.data.repository

import com.example.jardinemoi.data.model.PlantInfo
import com.example.jardinemoi.data.trefle.TrefleClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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
    suspend fun getPlantById(id: String): PlantInfo? = withContext(Dispatchers.IO) {
        // 1. On cherche d'abord dans notre catalogue local (Firestore) par ID de document
        try {
            val doc = firestore.collection("plants_info").document(id).get().await()
            if (doc.exists()) {
                val plant = doc.toObject(PlantInfo::class.java)
                return@withContext plant?.copy(id = doc.id)
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
                return@withContext doc.toObject(PlantInfo::class.java)?.copy(id = doc.id)
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
                return@withContext doc.toObject(PlantInfo::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {}

        // 4. Si c'est un ID numérique, on cherche sur Trefle (en ligne)
        if (id.all { it.isDigit() }) {
            try {
                val response = trefleApi.getPlant(id, trefleToken)
                val detail = response.data
                val species = detail.main_species
                return@withContext PlantInfo(
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
                    wateringFrequencyDays = calculateWateringFrequency(detail.common_name, detail.scientific_name, species?.growth)
                )
            } catch (e: Exception) {
                return@withContext null
            }
        }
        
        null
    }

    private fun calculateWateringFrequency(commonName: String?, scientificName: String?, growth: com.example.jardinemoi.data.trefle.TrefleGrowth?): Int {
        // 1. Priorité à la donnée officielle Trefle : MOISTURE USE
        growth?.moisture_use?.let {
            return when (it.lowercase()) {
                "high" -> 3   // Grand consommateur d'eau
                "medium" -> 6 // Consommation moyenne
                "low" -> 12   // Très sobre (Cactus, etc.)
                else -> 7
            }
        }

        // 2. Secours : Données de précipitations
        val minPrecipitation = growth?.minimum_precipitation?.get("mm")
        if (minPrecipitation != null) {
            return when {
                minPrecipitation < 300 -> 14
                minPrecipitation < 600 -> 9
                minPrecipitation < 1000 -> 6
                else -> 3
            }
        }

        // 3. Secours ultime : Mots-clés (Heuristique)
        val name = (commonName ?: scientificName ?: "").lowercase()
        val base = when {
            name.contains("cactus") || name.contains("succulent") || name.contains("aloe") || name.contains("crassula") -> 15
            name.contains("fern") || name.contains("fougère") || name.contains("pothos") || name.contains("tropical") -> 3
            name.contains("rose") || name.contains("lavender") || name.contains("lavande") -> 5
            else -> 7
        }

        // 4. Variation unique pour éviter l'effet "copier-coller"
        val variation = (scientificName?.length ?: 0) % 3 - 1
        return (base + variation).coerceIn(1, 21)
    }

    suspend fun addToMyPlants(plant: PlantInfo): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Utilisateur non connecté"))
        val plantId = plant.id.ifEmpty { plant.commonName.filter { it.isLetterOrDigit() } }
        
        try {
            val userRef = firestore.collection("users").document(userId)
            val plantRef = userRef.collection("my_plants").document(plantId)
            
            val finalPlant = plant.copy(
                id = plantId,
                plantedAt = System.currentTimeMillis() // Fixe le début de la croissance en temps réel
            )
            
            firestore.runTransaction { transaction ->
                // 1. On ajoute la plante
                transaction.set(plantRef, finalPlant)
                
                // 2. On incrémente le compteur global de plantes cultivées (Trophées)
                transaction.update(userRef, "lifetime_plants_count", com.google.firebase.firestore.FieldValue.increment(1))
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            // Si l'update échoue parce que le document user n'existe pas, on le crée
            try {
                val finalPlant = plant.copy(
                    id = plantId,
                    plantedAt = System.currentTimeMillis()
                )
                firestore.collection("users").document(userId).set(mapOf("lifetime_plants_count" to 1), com.google.firebase.firestore.SetOptions.merge()).await()
                firestore.collection("users").document(userId).collection("my_plants").document(plantId).set(finalPlant).await()
                Result.success(Unit)
            } catch (e2: Exception) {
                Result.failure(e2)
            }
        }
    }

    // 🔥 Récupération du nombre total de plantes cultivées pour les trophées
    fun getLifetimePlantsCount(): Flow<Int> = callbackFlow {
        val userId = auth.currentUser?.uid ?: return@callbackFlow
        val listener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, _ ->
                val count = snapshot?.getLong("lifetime_plants_count")?.toInt() ?: 0
                trySend(count)
            }
        awaitClose { listener.remove() }
    }

    suspend fun searchPlantsOnline(query: String): List<PlantInfo> = withContext(Dispatchers.IO) {
        try {
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
                    wateringFrequencyDays = calculateWateringFrequency(treflePlant.common_name, treflePlant.scientific_name, null)
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
    suspend fun getMyPlantById(plantId: String): PlantInfo? = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext null
        try {
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

    // 🔥 Marquer une plante comme arrosée (+10 pts)
    suspend fun waterPlant(plantId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
        val userRef = firestore.collection("users").document(userId)
        val plantRef = userRef.collection("my_plants").document(plantId)
        
        try {
            firestore.runTransaction { transaction ->
                // 1. Update de la plante (Date + Points individuels)
                transaction.update(plantRef, "lastWateredDate", System.currentTimeMillis())
                transaction.update(plantRef, "carePoints", com.google.firebase.firestore.FieldValue.increment(10))
                
                // 2. Update du score global du compte (pour le Niveau de Jardinier)
                transaction.update(userRef, "total_care_points", com.google.firebase.firestore.FieldValue.increment(10))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔥 Récupération du score global pour le niveau
    fun getTotalCarePoints(): Flow<Int> = callbackFlow {
        val userId = auth.currentUser?.uid ?: return@callbackFlow
        val listener = firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, _ ->
                val points = snapshot?.getLong("total_care_points")?.toInt() ?: 0
                trySend(points)
            }
        awaitClose { listener.remove() }
    }

    // 🔥 Debug : Accélérer la croissance (Saut direct au prochain stade)
    suspend fun debugAccelerateGrowth(plantId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
        try {
            val docRef = firestore.collection("users").document(userId)
                .collection("my_plants").document(plantId)
            
            val doc = docRef.get().await()
            val plant = doc.toObject(PlantInfo::class.java) ?: return@withContext Result.failure(Exception("Plante introuvable"))
            
            // On récupère le temps restant avant le prochain stade
            val timeToNext = plant.calculateTimeToNextStageMs(System.currentTimeMillis())
            
            if (timeToNext != null) {
                // On recule la date de plantation du temps restant + 1 seconde pour être sûr de passer le seuil
                val newPlantedAt = plant.plantedAt - (timeToNext + 1000)
                docRef.update("plantedAt", newPlantedAt).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Déjà au stade maximum"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeFromMyPlants(plantId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Utilisateur non connecté"))
        if (plantId.isEmpty()) return@withContext Result.failure(Exception("ID de plante invalide"))

        try {
            // Vérifier si la plante est complétée avant de la supprimer
            val plantDoc = firestore.collection("users")
                .document(userId)
                .collection("my_plants")
                .document(plantId)
                .get()
                .await()

            val plant = plantDoc.toObject(PlantInfo::class.java)

            // Si la plante est complétée (Mature) ou au stade Mature, l'ajouter à la collection "completed_plants"
            if (plant != null && (plant.completed || plant.currentStage == "Mature")) {
                firestore.collection("users")
                    .document(userId)
                    .collection("completed_plants")
                    .document(plantId)
                    .set(mapOf(
                        "plantId" to plantId,
                        "completedAt" to System.currentTimeMillis(),
                        "commonName" to plant.commonName
                    ))
                    .await()
            }

            // Supprimer la plante de "my_plants"
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
    suspend fun deleteAllMyPlants(): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Non connecté"))
        try {
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

    // 🌟 Récupération en temps réel des plantes complétées (cultivées jusqu'au stade Mature)
    fun getCompletedPlantIds(): Flow<Set<String>> = callbackFlow {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            // Utilisateur non connecté: émettre vide
            trySend(emptySet())
            close()
            return@callbackFlow
        }

        var completedCollectionIds = emptySet<String>()
        var matureMyPlantsIds = emptySet<String>()

        fun emitCombined() {
            trySend(completedCollectionIds + matureMyPlantsIds)
        }

        // Écouter la collection completed_plants pour cet utilisateur (anciennes plantes complétées supprimées)
        val completedListener = firestore.collection("users")
            .document(userId)
            .collection("completed_plants")
            .addSnapshotListener { snapshot, _ ->
                completedCollectionIds = snapshot?.documents?.map { it.id }?.toSet() ?: emptySet()
                emitCombined()
            }

        // Écouter la collection my_plants pour cet utilisateur (plantes en cours mais déjà matures/complétées)
        val myPlantsListener = firestore.collection("users")
            .document(userId)
            .collection("my_plants")
            .addSnapshotListener { snapshot, _ ->
                matureMyPlantsIds = snapshot?.documents?.mapNotNull { doc ->
                    val plant = doc.toObject(PlantInfo::class.java)?.copy(id = doc.id)
                    if (plant != null && (plant.completed || plant.currentStage == "Mature")) {
                        doc.id
                    } else {
                        null
                    }
                }?.toSet() ?: emptySet()
                emitCombined()
            }

        awaitClose {
            completedListener.remove()
            myPlantsListener.remove()
        }
    }

    // 🌟 Marquer une plante comme complétée (Mature)
    suspend fun markPlantAsCompleted(plantId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Utilisateur non connecté"))
        if (plantId.isEmpty()) return@withContext Result.failure(Exception("ID de plante invalide"))

        try {
            val docRef = firestore.collection("users")
                .document(userId)
                .collection("my_plants")
                .document(plantId)

            val doc = docRef.get().await()
            val plant = doc.toObject(PlantInfo::class.java) ?: return@withContext Result.failure(Exception("Plante introuvable"))

            // Si la plante n'est pas déjà marquée comme complétée et qu'elle est Mature
            if (!plant.completed && plant.currentStage == "Mature") {
                docRef.update("completed", true).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
