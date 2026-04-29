package com.example.jardinemoi.messaging.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

fun createOrOpenConversation(uidA: String, uidB: String, onResult: (String) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()

    firestore.collection("conversations")
        .whereArrayContains("participants", uidA)
        .get()
        .addOnSuccessListener { snap ->
            // Vérifier si une conversation existe déjà
            val existing = snap.documents.firstOrNull { doc ->
                val participants = doc.get("participants") as? List<*>
                participants?.contains(uidB) == true
            }

            if (existing != null) {
                onResult(existing.id)
                return@addOnSuccessListener
            }

            // Sinon créer une nouvelle conversation
            val newConv = mapOf(
                "participants" to listOf(uidA, uidB),
                "lastMessage" to "",
                "lastTimestamp" to System.currentTimeMillis()
            )

            firestore.collection("conversations")
                .add(newConv)
                .addOnSuccessListener { doc ->
                    onResult(doc.id)
                }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMessageScreen(nav: NavController) {
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var users by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    // 🔥 Charger les utilisateurs depuis Firestore
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .get()
            .addOnSuccessListener { snap ->
                users = snap.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val uid = doc.id
                    if (uid != currentUid) uid to name else null
                }
            }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Nouveau message") }) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(users) { (uid, name) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            createOrOpenConversation(currentUid, uid) { conversationId ->
                                nav.navigate("chat/$conversationId")
                            }
                        }
                        .padding(16.dp)
                ) {
                    Text(name)
                }
            }
        }
    }
}
