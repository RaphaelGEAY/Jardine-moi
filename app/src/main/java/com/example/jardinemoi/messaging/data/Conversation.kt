package com.example.jardinemoi.messaging.data

data class Conversation(
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastTimestamp: Long = 0
)
