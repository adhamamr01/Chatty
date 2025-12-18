# Chat Application API - Complete Documentation

## Table of Contents
1. [Overview](#overview)
2. [Authentication](#authentication)
3. [REST API Endpoints](#rest-api-endpoints)
4. [WebSocket API](#websocket-api)
5. [Data Models](#data-models)
6. [Error Handling](#error-handling)

---

## Overview

**Base URL:** `http://localhost:8080`  
**API Version:** v1  
**Documentation:** `http://localhost:8080/swagger-ui.html`

The Chat Application provides a REST API for user management and chat operations, combined with WebSocket/STOMP for real-time messaging.

---

## Authentication

### JWT Token-Based Authentication

Most endpoints require a JWT token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

### Obtaining a Token

**POST** `/api/auth/login`

**Request:**
```json
{
  "username": "john_doe",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "userId": 1,
    "username": "john_doe",
    "email": "john.doe@example.com",
    "displayName": "John Doe"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## REST API Endpoints

### Authentication Endpoints

#### Register New User
**POST** `/api/auth/register`

**Request Body:**
```json
{
  "username": "john_doe",
  "email": "john.doe@example.com",
  "password": "securePassword123",
  "displayName": "John Doe"
}
```

**Response:** `201 Created`
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "userId": 1,
    "username": "john_doe",
    "email": "john.doe@example.com",
    "displayName": "John Doe"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

#### Login
**POST** `/api/auth/login`

See [Authentication](#authentication) section above.

---

### User Endpoints

#### Get Current User Profile
**GET** `/api/users/me`

**Headers:** `Authorization: Bearer <token>`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "john_doe",
    "email": "john.doe@example.com",
    "displayName": "John Doe"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

#### Search Users
**GET** `/api/users/search?q={query}`

**Headers:** `Authorization: Bearer <token>`

**Query Parameters:**
- `q` (required): Search query (minimum 2 characters)

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": 2,
      "username": "jane_doe",
      "email": "jane.doe@example.com",
      "displayName": "Jane Doe"
    }
  ],
  "timestamp": "2024-01-15T10:30:00"
}
```

---

### Chat Room Endpoints

#### Get All User's Chats
**GET** `/api/chats`

**Headers:** `Authorization: Bearer <token>`

**Response:** `200 OK`
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "otherUser": {
        "id": 2,
        "username": "jane_doe",
        "email": "jane.doe@example.com",
        "displayName": "Jane Doe"
      },
      "lastMessage": {
        "id": 123,
        "chatRoomId": 1,
        "senderId": 2,
        "senderUsername": "jane_doe",
        "content": "Hello!",
        "status": "READ",
        "createdAt": "2024-01-15T10:30:00"
      },
      "createdAt": "2024-01-15T10:00:00"
    }
  ],
  "timestamp": "2024-01-15T10:30:00"
}
```

---

#### Create/Get Direct Chat
**POST** `/api/chats/direct`

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "targetUserId": 2
}
```

**Response:** `200 OK` (if chat exists) or `201 Created` (if new chat)
```json
{
  "success": true,
  "message": "Chat room created successfully",
  "data": {
    "id": 1,
    "otherUser": {
      "id": 2,
      "username": "jane_doe",
      "email": "jane.doe@example.com",
      "displayName": "Jane Doe"
    },
    "lastMessage": null,
    "createdAt": "2024-01-15T10:00:00"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

#### Get Chat Messages
**GET** `/api/chats/{chatRoomId}/messages?page={page}&size={size}`

**Headers:** `Authorization: Bearer <token>`

**Path Parameters:**
- `chatRoomId` (required): ID of the chat room

**Query Parameters:**
- `page` (optional, default: 0): Page number (0-indexed)
- `size` (optional, default: 20): Number of messages per page

**Response:** `200 OK`
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 123,
        "chatRoomId": 1,
        "senderId": 2,
        "senderUsername": "jane_doe",
        "content": "Hello!",
        "status": "READ",
        "createdAt": "2024-01-15T10:30:00"
      }
    ],
    "pageNumber": 0,
    "pageSize": 20,
    "totalElements": 50,
    "totalPages": 3,
    "last": false,
    "first": true,
    "empty": false
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

---

#### Mark Message as Read
**PUT** `/api/messages/{messageId}/read`

**Headers:** `Authorization: Bearer <token>`

**Path Parameters:**
- `messageId` (required): ID of the message to mark as read

**Response:** `200 OK`
```json
{
  "success": true,
  "message": "Message marked as read",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## WebSocket API

See the separate [WebSocket API Documentation](websocket_docs) for complete details on:
- Connection setup
- Subscribe destinations
- Send destinations
- Message formats
- Typing indicators
- Read receipts

**Quick Reference:**
- **Connect:** `ws://localhost:8080/ws/chat`
- **Personal Messages:** `/user/queue/messages`
- **Chat Room:** `/topic/chat.{roomId}`
- **Send Message:** `/app/chat.send`
- **Typing:** `/app/chat.typing`
- **Read Receipt:** `/app/chat.read`

---

## Data Models

### User
```typescript
{
  id: number;              // Unique identifier
  username: string;        // Unique username
  email: string;          // Email address
  displayName: string;    // Display name (optional)
}
```

### ChatRoom
```typescript
{
  id: number;                    // Unique identifier
  otherUser: User;              // The other participant
  lastMessage: Message | null;  // Most recent message
  createdAt: string;            // ISO 8601 timestamp
}
```

### Message
```typescript
{
  id: number;              // Unique identifier
  chatRoomId: number;      // Chat room ID
  senderId: number;        // Sender user ID
  senderUsername: string;  // Sender username
  content: string;         // Message content
  status: MessageStatus;   // SENT, DELIVERED, or READ
  createdAt: string;       // ISO 8601 timestamp
}
```

### MessageStatus
```typescript
enum MessageStatus {
  SENT = "SENT",           // Message sent but not delivered
  DELIVERED = "DELIVERED", // Message delivered but not read
  READ = "READ"           // Message has been read
}
```

---

## Error Handling

### Standard Error Response
```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Validation failed for request",
  "path": "/api/auth/register",
  "fieldErrors": [
    {
      "field": "email",
      "rejectedValue": "invalid-email",
      "message": "Email must be valid"
    }
  ],
  "timestamp": "2024-01-15T10:30:00"
}
```

### Common HTTP Status Codes

| Code | Meaning | Description |
|------|---------|-------------|
| 200 | OK | Request successful |
| 201 | Created | Resource created successfully |
| 400 | Bad Request | Invalid request format or validation error |
| 401 | Unauthorized | Missing or invalid authentication token |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource conflict (e.g., username already exists) |
| 500 | Internal Server Error | Server error |

### Common Error Types

- `VALIDATION_ERROR` - Request validation failed
- `AUTHENTICATION_ERROR` - Authentication failed
- `AUTHORIZATION_ERROR` - Insufficient permissions
- `RESOURCE_NOT_FOUND` - Requested resource not found
- `DUPLICATE_RESOURCE` - Resource already exists
- `INTERNAL_ERROR` - Unexpected server error

---

## Testing the API

### Using Swagger UI
Navigate to `http://localhost:8080/swagger-ui.html` for interactive API documentation and testing.

### Using cURL

**Register:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john.doe@example.com",
    "password": "securePassword123",
    "displayName": "John Doe"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securePassword123"
  }'
```

**Get User Profile:**
```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer <your-token>"
```

---

## Rate Limiting

- **REST API:** 100 requests per minute per user
- **WebSocket Messages:** 60 messages per minute per user
- **Typing Indicators:** 10 per minute per chat room

Exceeding rate limits will result in HTTP 429 (Too Many Requests) responses.

---

## Notes

1. All timestamps are in ISO 8601 format (UTC)
2. All request/response bodies are JSON
3. Pagination is 0-indexed
4. Default page size is 20 items
5. Maximum page size is 100 items