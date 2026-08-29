package com.example

import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    val id: String? = null,
    val clientId: String? = null,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val timestamp: Long? = null
)

