package com.chatapp.client.data.remote

import com.chatapp.client.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ApiClient {
    private val baseUrl = "http://10.0.2.2:8080" // Android emulator localhost
    // Use "http://localhost:8080" for desktop/web

    private var authToken: String? = null

    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }

        install(WebSockets)

        install(DefaultRequest) {
            url(baseUrl)
            contentType(ContentType.Application.Json)
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 30000
        }
    }

    fun setAuthToken(token: String) {
        authToken = token
    }

    fun clearAuthToken() {
        authToken = null
    }

    // Auth endpoints
    suspend fun login(username: String, password: String): Result<AuthResponse> {
        return try {
            val response: ApiResponse<AuthResponse> = client.post("/api/auth/login") {
                setBody(mapOf(
                    "username" to username,
                    "password" to password
                ))
            }.body()

            if (response.success && response.data != null) {
                setAuthToken(response.data.token)
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        displayName: String
    ): Result<AuthResponse> {
        return try {
            val response: ApiResponse<AuthResponse> = client.post("/api/auth/register") {
                setBody(mapOf(
                    "username" to username,
                    "email" to email,
                    "password" to password,
                    "displayName" to displayName
                ))
            }.body()

            if (response.success && response.data != null) {
                setAuthToken(response.data.token)
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // User endpoints
    suspend fun getCurrentUser(): Result<User> {
        return try {
            val response: ApiResponse<User> = client.get("/api/users/me") {
                authToken?.let { bearerAuth(it) }
            }.body()

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to get user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Chat endpoints
    suspend fun getChats(): Result<List<ChatRoom>> {
        return try {
            val response: ApiResponse<List<ChatRoom>> = client.get("/api/chats") {
                authToken?.let { bearerAuth(it) }
            }.body()

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to get chats"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMessages(chatRoomId: Long, page: Int = 0, size: Int = 50): Result<List<Message>> {
        return try {
            val response: ApiResponse<PageResponse> = client.get("/api/chats/$chatRoomId/messages") {
                authToken?.let { bearerAuth(it) }
                parameter("page", page)
                parameter("size", size)
            }.body()

            if (response.success && response.data != null) {
                Result.success(response.data.content)
            } else {
                Result.failure(Exception(response.message ?: "Failed to get messages"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(chatRoomId: Long, content: String): Result<Message> {
        return try {
            val response: ApiResponse<Message> = client.post("/api/chats/$chatRoomId/messages") {
                authToken?.let { bearerAuth(it) }
                setBody(mapOf("content" to content))
            }.body()

            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Failed to send message"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper data class for pagination
    @kotlinx.serialization.Serializable
    data class PageResponse(
        val content: List<Message>,
        val pageNumber: Int,
        val pageSize: Int,
        val totalElements: Long,
        val totalPages: Int
    )
}