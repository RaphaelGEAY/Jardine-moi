package com.example.jardinemoi.messaging.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jardinemoi.messaging.MessagingViewModel
import com.example.jardinemoi.messaging.ui.components.MessageBubble
import com.example.jardinemoi.messaging.ui.components.MessageInput
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ChatScreen(conversationId: String, vm: MessagingViewModel = viewModel()) {
    val messages by vm.messages.collectAsStateWithLifecycle()
    val uid = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    LaunchedEffect(conversationId) {
        vm.listenToConversation(conversationId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = false // On peut mettre true si on veut les derniers messages en bas
        ) {
            items(messages, key = { it.id }) { msg ->
                MessageBubble(text = msg.text, isMe = msg.senderId == uid)
            }
        }

        MessageInput { text ->
            vm.sendMessage(conversationId, uid, text)
        }
    }
}
