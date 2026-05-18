package com.example.jardinemoi.messaging.data

data class Conversation(
    val title: String? = null,
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastTimestamp: Long = 0,
    val isForum: Boolean = false
)
