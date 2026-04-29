package com.example.jardinemoi.messaging.data

data class Message(
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = 0
)
