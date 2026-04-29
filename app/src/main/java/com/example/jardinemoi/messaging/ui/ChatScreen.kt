package com.example.jardinemoi.messaging.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jardinemoi.messaging.MessagingViewModel
import com.example.jardinemoi.messaging.ui.components.MessageBubble
import com.example.jardinemoi.messaging.ui.components.MessageInput
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.Modifier


@Composable
fun ChatScreen(conversationId: String, vm: MessagingViewModel = viewModel()) {
    val messages by vm.messages.collectAsState()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    LaunchedEffect(conversationId) {
        vm.listenToConversation(conversationId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) { msg ->
                MessageBubble(text = msg.text, isMe = msg.senderId == uid)
            }
        }

        MessageInput { text ->
            vm.sendMessage(conversationId, uid, text)
        }
    }
}
