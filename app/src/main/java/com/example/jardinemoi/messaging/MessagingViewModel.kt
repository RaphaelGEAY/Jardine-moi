package com.example.jardinemoi.messaging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jardinemoi.messaging.data.MessagingRepository
import com.example.jardinemoi.messaging.data.Message
import com.example.jardinemoi.messaging.data.Conversation
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MessagingViewModel : ViewModel() {

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

    private var conversationsJob: Job? = null
    private var lastLoadedUid: String? = null
    fun loadConversations(uid: String) {
        if (uid.isBlank() || (uid == lastLoadedUid && conversationsJob?.isActive == true)) return
        lastLoadedUid = uid
        
        conversationsJob?.cancel()
        conversationsJob = viewModelScope.launch {
            MessagingRepository.getConversationsForUser(uid).collect {
                _conversations.value = it
            }
        }
    }

    private var messagesJob: Job? = null
    private var lastLoadedConversationId: String? = null
    fun listenToConversation(conversationId: String) {
        if (conversationId.isBlank() || (conversationId == lastLoadedConversationId && messagesJob?.isActive == true)) return
        lastLoadedConversationId = conversationId
        
        messagesJob?.cancel()
        if (conversationId.startsWith("forum_")) {
            _messages.value = getFakeForumMessages(conversationId)
            return
        }
        messagesJob = viewModelScope.launch {
            MessagingRepository.listenMessages(conversationId).collect {
                _messages.value = it
            }
        }
    }

    private fun getFakeForumMessages(forumId: String): List<Message> {
        return when (forumId) {
            "forum_1" -> listOf(
                Message("f1_m1", "user1", "Bonjour ! Quelqu'un sait comment soigner un Monstera ?", 0),
                Message("f1_m2", "user2", "Il faut faire attention à ne pas trop l'arroser !", 1),
                Message("f1_m3", "user3", "Et évite le soleil direct sur les feuilles.", 2)
            )
            "forum_2" -> listOf(
                Message("f2_m1", "user4", "J'ai des graines de tomates cerises à échanger !", 0),
                Message("f2_m2", "user5", "Ça m'intéresse ! Tu cherches quoi en échange ?", 1)
            )
            "forum_3" -> listOf(
                Message("f3_m1", "user6", "Regardez ma récolte de ce matin !", 0),
                Message("f3_m2", "user1", "Magnifique ! Quelle est ton secret ?", 1)
            )
            else -> emptyList()
        }
    }

    fun sendMessage(conversationId: String, senderId: String, text: String) {
        if (conversationId.startsWith("forum_")) {
            val newMessage = Message(java.util.UUID.randomUUID().toString(), senderId, text, System.currentTimeMillis())
            _messages.value = _messages.value + newMessage
            return
        }
        viewModelScope.launch {
            MessagingRepository.sendMessage(conversationId, senderId, text)
        }
    }

    private val _users = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val users = _users.asStateFlow()

    fun loadUsers() {
        viewModelScope.launch {
            _users.value = MessagingRepository.getAllUsers()
        }
    }

    fun startConversation(uidA: String, uidB: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val id = MessagingRepository.createOrOpenConversation(uidA, uidB)
            if (id != null) onResult(id)
        }
    }
}
