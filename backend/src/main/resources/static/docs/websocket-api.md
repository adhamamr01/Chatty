# WebSocket API Documentation

## Overview

The Chat Application uses **STOMP over WebSocket** for real-time messaging. This allows bidirectional communication between clients and the server for instant message delivery, typing indicators, and read receipts.

---

## Connection

### Endpoint
```
ws://localhost:8080/ws/chat
```

### Authentication
Include JWT token in connection headers:
```javascript
{
  "Authorization": "Bearer <your-jwt-token>"
}
```

---

## Subscribe Destinations

Clients must subscribe to these destinations to receive messages:

### 1. Personal Message Queue
**Destination:** `/user/queue/messages`

**Description:** Receives all messages sent directly to the authenticated user

**Message Format:**
```json
{
  "id": 123,
  "chatRoomId": 1,
  "senderId": 2,
  "senderUsername": "jane_doe",
  "content": "Hello, how are you?",
  "status": "SENT",
  "createdAt": "2024-01-15T10:30:00"
}
```

---

### 2. Chat Room Topic
**Destination:** `/topic/chat.{roomId}`

**Description:** Receives all messages in a specific chat room (replace `{roomId}` with actual room ID)

**Example:** `/topic/chat.1`

**Message Format:** Same as Personal Message Queue

---

### 3. Typing Indicators
**Destination:** `/user/queue/typing.{roomId}`

**Description:** Receives typing indicators for a specific chat room

**Example:** `/user/queue/typing.1`

**Message Format:**
```json
{
  "chatRoomId": 1,
  "userId": 2,
  "username": "jane_doe",
  "typing": true
}
```

---

## Send Destinations

Use these destinations to send messages to the server:

### 1. Send Message
**Destination:** `/app/chat.send`

**Description:** Send a new message to a chat room

**Request Payload:**
```json
{
  "chatRoomId": 1,
  "content": "Hello, how are you?"
}
```

**Response:** Message is broadcast to all chat room members via their subscribed topics

---

### 2. Typing Indicator
**Destination:** `/app/chat.typing`

**Description:** Notify other users when you start/stop typing

**Request Payload:**
```json
{
  "chatRoomId": 1,
  "typing": true
}
```

**Notes:**
- Send `"typing": true` when user starts typing
- Send `"typing": false` when user stops typing
- Typing indicators are broadcast to other members in the chat room

---

### 3. Mark as Read
**Destination:** `/app/chat.read`

**Description:** Mark messages as read in a chat room

**Request Payload:**
```json
{
  "chatRoomId": 1,
  "messageId": null
}
```

**Notes:**
- If `messageId` is `null`, all unread messages in the room are marked as read
- If `messageId` is specified, only that message is marked as read
- Read receipts are sent to the message sender

---

## Message Flow Example

### Sending a Message

1. **Client A sends message:**
   ```
   SEND /app/chat.send
   {
     "chatRoomId": 1,
     "content": "Hello!"
   }
   ```

2. **Server saves message and broadcasts:**
    - To Client A: `/user/queue/messages`
    - To Client B: `/user/queue/messages`
    - To topic: `/topic/chat.1`

3. **Clients receive:**
   ```json
   {
     "id": 123,
     "chatRoomId": 1,
     "senderId": 1,
     "senderUsername": "john_doe",
     "content": "Hello!",
     "status": "SENT",
     "createdAt": "2024-01-15T10:30:00"
   }
   ```

---

### Typing Indicator Flow

1. **Client A starts typing:**
   ```
   SEND /app/chat.typing
   {
     "chatRoomId": 1,
     "typing": true
   }
   ```

2. **Client B receives:**
   ```json
   {
     "chatRoomId": 1,
     "userId": 1,
     "username": "john_doe",
     "typing": true
   }
   ```

3. **Client A stops typing (after 3 seconds of inactivity):**
   ```
   SEND /app/chat.typing
   {
     "chatRoomId": 1,
     "typing": false
   }
   ```

---

### Read Receipt Flow

1. **Client B marks message as read:**
   ```
   SEND /app/chat.read
   {
     "chatRoomId": 1,
     "messageId": 123
   }
   ```

2. **Server updates message status to READ**

3. **Client A receives updated status** (via WebSocket or when fetching messages)

---

## Error Handling

### Connection Errors
- **401 Unauthorized**: Invalid or missing JWT token
- **403 Forbidden**: Token expired or invalid

### Message Errors
- **400 Bad Request**: Invalid message format
- **404 Not Found**: Chat room not found or user not a member
- **500 Internal Server Error**: Server error processing message

---

## Client Implementation Example

### JavaScript (STOMP.js)

```javascript
import { Client } from '@stomp/stompjs';

// Create STOMP client
const client = new Client({
  brokerURL: 'ws://localhost:8080/ws/chat',
  connectHeaders: {
    Authorization: `Bearer ${token}`
  },
  onConnect: () => {
    console.log('Connected to WebSocket');
    
    // Subscribe to personal messages
    client.subscribe('/user/queue/messages', (message) => {
      const msg = JSON.parse(message.body);
      console.log('Received message:', msg);
    });
    
    // Subscribe to chat room
    client.subscribe('/topic/chat.1', (message) => {
      const msg = JSON.parse(message.body);
      console.log('Chat room message:', msg);
    });
    
    // Subscribe to typing indicators
    client.subscribe('/user/queue/typing.1', (message) => {
      const indicator = JSON.parse(message.body);
      console.log('Typing indicator:', indicator);
    });
  },
  onStompError: (frame) => {
    console.error('STOMP error:', frame);
  }
});

// Connect
client.activate();

// Send message
function sendMessage(chatRoomId, content) {
  client.publish({
    destination: '/app/chat.send',
    body: JSON.stringify({
      chatRoomId,
      content
    })
  });
}

// Send typing indicator
function sendTypingIndicator(chatRoomId, typing) {
  client.publish({
    destination: '/app/chat.typing',
    body: JSON.stringify({
      chatRoomId,
      typing
    })
  });
}

// Mark as read
function markAsRead(chatRoomId, messageId = null) {
  client.publish({
    destination: '/app/chat.read',
    body: JSON.stringify({
      chatRoomId,
      messageId
    })
  });
}
```

---

## Best Practices

1. **Reconnection**: Implement automatic reconnection with exponential backoff
2. **Heartbeat**: Enable heartbeats to detect connection issues
3. **Message Queuing**: Queue messages locally when disconnected
4. **Typing Debounce**: Debounce typing indicators (send after 300ms of typing)
5. **Typing Timeout**: Stop typing indicator after 3 seconds of inactivity
6. **Error Handling**: Handle all error scenarios gracefully
7. **Unsubscribe**: Always unsubscribe when leaving a chat room

---

## Rate Limits

- **Messages**: 60 messages per minute per user
- **Typing Indicators**: 10 per minute per chat room
- **Read Receipts**: 30 per minute per user

Exceeding rate limits will result in temporary message rejection.