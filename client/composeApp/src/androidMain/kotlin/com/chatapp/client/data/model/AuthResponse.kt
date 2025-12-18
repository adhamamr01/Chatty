package com.chatapp.client.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val type: String = "Bearer",
    val userId: Long,
    val username: String,
    val email: String,
    val displayName: String?
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: String? = null,
    val timestamp: String? = null
)