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

    private val _forums = MutableStateFlow<List<Pair<String, Conversation>>>(
        listOf(
            "forum_1" to Conversation(
                title = "🌱 Entraide & Conseils",
                lastMessage = "Quelqu'un sait comment soigner un Monstera ?",
                isForum = true
            ),
            "forum_2" to Conversation(
                title = "🤝 Troc de Graines",
                lastMessage = "J'ai des graines de tomates cerises à échanger !",
                isForum = true
            ),
            "forum_3" to Conversation(
                title = "📸 Photos de vos Jardins",
                lastMessage = "Regardez ma récolte de ce matin !",
                isForum = true
            )
        )
    )
    val forums = _forums.asStateFlow()

    fun loadConversations(uid: String) {
        viewModelScope.launch {
            repo.getConversationsForUser(uid).collect {
                _conversations.value = it
            }
        }
    }

    fun listenToConversation(conversationId: String) {
        if (conversationId.startsWith("forum_")) {
            _messages.value = getFakeForumMessages(conversationId)
            return
        }
        viewModelScope.launch {
            repo.listenMessages(conversationId).collect {
                _messages.value = it
            }
        }
    }

    private fun getFakeForumMessages(forumId: String): List<Message> {
        return when (forumId) {
            "forum_1" -> listOf(
                Message("user1", "Bonjour ! Quelqu'un sait comment soigner un Monstera ?", 0),
                Message("user2", "Il faut faire attention à ne pas trop l'arroser !", 1),
                Message("user3", "Et évite le soleil direct sur les feuilles.", 2)
            )
            "forum_2" -> listOf(
                Message("user4", "J'ai des graines de tomates cerises à échanger !", 0),
                Message("user5", "Ça m'intéresse ! Tu cherches quoi en échange ?", 1)
            )
            "forum_3" -> listOf(
                Message("user6", "Regardez ma récolte de ce matin !", 0),
                Message("user1", "Magnifique ! Quelle est ton secret ?", 1)
            )
            else -> emptyList()
        }
    }

    fun sendMessage(conversationId: String, senderId: String, text: String) {
        if (conversationId.startsWith("forum_")) {
            // Dans un "faux" forum, on ajoute juste le message localement pour la démo
            val newMessage = Message(senderId, text, System.currentTimeMillis())
            _messages.value = _messages.value + newMessage
            return
        }
        viewModelScope.launch {
            repo.sendMessage(conversationId, senderId, text)
        }
    }
}
