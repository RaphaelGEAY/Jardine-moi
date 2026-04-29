package com.example.jardinemoi.messaging.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MessagingRepository {

    private val firestore = FirebaseFirestore.getInstance()

    // 🔥 Récupère toutes les conversations d’un utilisateur
    fun getConversationsForUser(uid: String): Flow<List<Pair<String, Conversation>>> = callbackFlow {
        val listener = firestore.collection("conversations")
            .whereArrayContains("participants", uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val conversations = snapshot.documents.mapNotNull { doc ->
                        val conv = doc.toObject(Conversation::class.java)
                        if (conv != null) doc.id to conv else null
                    }
                    trySend(conversations)
                }
            }

        awaitClose { listener.remove() }
    }

    // 🔥 Récupère les messages d’une conversation
    fun listenMessages(conversationId: String): Flow<List<Message>> = callbackFlow {
        val listener = firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val messages = snapshot.toObjects(Message::class.java)
                    trySend(messages)
                }
            }

        awaitClose { listener.remove() }
    }

    // 🔥 Envoie un message
    suspend fun sendMessage(conversationId: String, senderId: String, text: String) {
        val message = mapOf(
            "senderId" to senderId,
            "text" to text,
            "timestamp" to System.currentTimeMillis()
        )

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
            )
    }
}
