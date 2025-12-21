package com.chatapp.client.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: Long,
    val chatRoomId: Long,
    val senderId: Long,
    val senderUsername: String,
    val content: String,
    val status: MessageStatus,
    val createdAt: String
)

@Serializable
enum class MessageStatus {
    SENT, DELIVERED, READ
}