package com.example.jardinemoi.messaging.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object MessagingRepository {

    private val firestore by lazy { FirebaseFirestore.getInstance() }

    // 🔥 Récupère toutes les conversations d’un utilisateur
    fun getConversationsForUser(uid: String): Flow<List<Pair<String, Conversation>>> = callbackFlow {
        if (uid.isBlank()) {
            trySend(emptyList())
            return@callbackFlow
        }

        val listener = firestore.collection("conversations")
            .whereArrayContains("participants", uid)
            .addSnapshotListener(Dispatchers.IO.asExecutor()) { snapshot, e ->
                if (e != null) {
                    Log.e("MessagingRepo", "Erreur lors de l'écoute des conversations", e)
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val conversations = snapshot.documents.mapNotNull { doc ->
                        try {
                            val conv = doc.toObject(Conversation::class.java)
                            if (conv != null) doc.id to conv else null
                        } catch (ex: Exception) {
                            Log.e("MessagingRepo", "Erreur de parsing conversation ${doc.id}", ex)
                            null
                        }
                    }.sortedByDescending { it.second.lastTimestamp } // Tri local pour éviter l'index composite
                    trySend(conversations)
                }
            }

        awaitClose { listener.remove() }
    }

    // 🔥 Récupère les messages d’une conversation
    fun listenMessages(conversationId: String): Flow<List<Message>> = callbackFlow {
        if (conversationId.isBlank()) {
            trySend(emptyList())
            return@callbackFlow
        }

        val listener = firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener(Dispatchers.IO.asExecutor()) { snapshot, e ->
                if (e != null) {
                    Log.e("MessagingRepo", "Erreur lors de l'écoute des messages", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Message::class.java)?.copy(id = doc.id)
                        } catch (ex: Exception) {
                            Log.e("MessagingRepo", "Erreur de parsing message ${doc.id}", ex)
                            null
                        }
                    }.reversed() // On inverse pour avoir l'ordre chrono dans la liste si on affiche du haut vers le bas
                    trySend(messages)
                }
            }

        awaitClose { listener.remove() }
    }

    // 🔥 Récupère la liste des utilisateurs pour démarrer une nouvelle conversation
    suspend fun getAllUsers(): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("users").get().await()
            snapshot.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                doc.id to name
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 🔥 Crée ou ouvre une conversation existante
    suspend fun createOrOpenConversation(uidA: String, uidB: String): String? = withContext(Dispatchers.IO) {
        try {
            val snap = firestore.collection("conversations")
                .whereArrayContains("participants", uidA)
                .get()
                .await()
            
            val existing = snap.documents.firstOrNull { doc ->
                val participants = doc.get("participants") as? List<*>
                participants?.contains(uidB) == true
            }

            if (existing != null) {
                return@withContext existing.id
            }

            val newConv = mapOf(
                "participants" to listOf(uidA, uidB),
                "lastMessage" to "",
                "lastTimestamp" to System.currentTimeMillis()
            )

            val doc = firestore.collection("conversations")
                .add(newConv)
                .await()
            
            doc.id
        } catch (e: Exception) {
            null
        }
    }

    // 🔥 Envoie un message
    suspend fun sendMessage(conversationId: String, senderId: String, text: String) = withContext(Dispatchers.IO) {
        val message = mapOf(
            "senderId" to senderId,
            "text" to text,
            "timestamp" to System.currentTimeMillis()
        )

        try {
            firestore.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .add(message)
                .await()

            firestore.collection("conversations")
                .document(conversationId)
                .update(
                    "lastMessage", text,
                    "lastTimestamp", System.currentTimeMillis()
                ).await()
        } catch (e: Exception) {
            // Log error or handle it
        }
    }
}
