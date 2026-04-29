package com.example.jardinemoi.messaging.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.jardinemoi.messaging.MessagingViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add





@Composable
fun ConversationListScreen(nav: NavController, vm: MessagingViewModel = viewModel()) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val conversations by vm.conversations.collectAsState()

    LaunchedEffect(uid) {
        vm.loadConversations(uid)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(conversations) { (id, conv) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { nav.navigate("chat/$id") }
                        .padding(16.dp)
                ) {
                    Text("Participants: ${conv.participants}")
                    Text("Dernier message: ${conv.lastMessage}")
                }
            }
        }

        // ⭐ Bouton flottant "Nouveau message"
        FloatingActionButton(
            onClick = { nav.navigate("newMessage") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nouveau message")
        }
    }
}
