package com.example.jardinemoi.messaging.data

import com.google.firebase.firestore.Exclude

data class Message(
    @get:Exclude val id: String = "",
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0
)
