package com.example.jardinemoi.messaging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jardinemoi.messaging.data.MessagingRepository
import com.example.jardinemoi.messaging.data.Message
import com.example.jardinemoi.messaging.data.Conversation
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MessagingViewModel : ViewModel() {

    private val repo = MessagingRepository()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _conversations = MutableStateFlow<List<Pair<String, Conversation>>>(emptyList())
    val conversations = _conversations.asStateFlow()

    fun loadConversations(uid: String) {
        viewModelScope.launch {
            repo.getConversationsForUser(uid).collect {
                _conversations.value = it
            }
        }
    }

    fun listenToConversation(conversationId: String) {
        viewModelScope.launch {
            repo.listenMessages(conversationId).collect {
                _messages.value = it
            }
        }
    }

    fun sendMessage(conversationId: String, senderId: String, text: String) {
        viewModelScope.launch {
            repo.sendMessage(conversationId, senderId, text)
        }
    }
}
