package com.example.jardinemoi.messaging.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.jardinemoi.messaging.MessagingViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMessageScreen(nav: NavController, viewModel: MessagingViewModel = viewModel()) {
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val users by viewModel.users.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nouveau message") },
                navigationIcon = {
                    TextButton(onClick = { nav.popBackStack() }) {
                        Text("Retour")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(
                items = users.filter { it.first != currentUid },
                key = { it.first }
            ) { (uid, name) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.startConversation(currentUid, uid) { conversationId ->
                                nav.navigate("chat/$conversationId") {
                                    popUpTo("newMessage") { inclusive = true }
                                }
                            }
                        }
                        .padding(16.dp)
                ) {
                    Text(name, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
