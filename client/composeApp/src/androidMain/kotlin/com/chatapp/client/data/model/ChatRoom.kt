package com.chatapp.client.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatRoom(
    val id: Long,
    val otherUser: User,
    val lastMessage: Message? = null,
    val createdAt: String
)