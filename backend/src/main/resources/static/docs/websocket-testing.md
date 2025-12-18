# WebSocket Testing Guide

## Overview

The Chatty application uses **STOMP over WebSocket** for real-time messaging. This guide will help you test the WebSocket functionality.

---

## Connection Setup

### WebSocket Endpoint
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

After connecting, subscribe to these destinations to receive messages:

### 1. Personal Message Queue
**Destination:** `/user/queue/messages`

Receives all messages sent directly to you.

### 2. Chat Room Messages
**Destination:** `/topic/chat.{roomId}`

Example: `/topic/chat.1`

Receives all messages in a specific chat room.

### 3. Typing Indicators
**Destination:** `/topic/chat.{roomId}.typing`

Example: `/topic/chat.1.typing`

Receives typing indicators for a specific chat room.

### 4. Read Receipts
**Destination:** `/topic/chat.{roomId}.read`

Example: `/topic/chat.1.read`

Receives read receipt notifications for a specific chat room.

---

## Send Destinations

Use these destinations to send messages to the server:

### 1. Send Message
**Destination:** `/app/chat.send`

**Payload:**
```json
{
  "chatRoomId": 1,
  "content": "Hello, how are you?"
}
```

### 2. Send Typing Indicator
**Destination:** `/app/chat.typing`

**Payload:**
```json
{
  "chatRoomId": 1,
  "typing": true
}
```

Send `"typing": false` when user stops typing.

### 3. Send Read Receipt
**Destination:** `/app/chat.read`

**Payload (mark specific message):**
```json
{
  "chatRoomId": 1,
  "messageId": 123
}
```

**Payload (mark all messages):**
```json
{
  "chatRoomId": 1,
  "messageId": null
}
```

---

## Testing with JavaScript

### Step 1: Install STOMP.js

```bash
npm install @stomp/stompjs
```

### Step 2: Connect and Test

```javascript
import { Client } from '@stomp/stompjs';

// Get JWT token from login
const token = "your-jwt-token-here";
const chatRoomId = 1;

// Create STOMP client
const client = new Client({
  brokerURL: 'ws://localhost:8080/ws/chat',
  connectHeaders: {
    Authorization: `Bearer ${token}`
  },
  debug: function (str) {
    console.log('STOMP: ' + str);
  },
  reconnectDelay: 5000,
  heartbeatIncoming: 4000,
  heartbeatOutgoing: 4000,
});

// Handle connection
client.onConnect = function (frame) {
  console.log('Connected:', frame);

  // Subscribe to personal queue
  client.subscribe('/user/queue/messages', (message) => {
    const msg = JSON.parse(message.body);
    console.log('Personal message:', msg);
  });

  // Subscribe to chat room
  client.subscribe(`/topic/chat.${chatRoomId}`, (message) => {
    const msg = JSON.parse(message.body);
    console.log('Chat room message:', msg);
  });

  // Subscribe to typing indicators
  client.subscribe(`/topic/chat.${chatRoomId}.typing`, (message) => {
    const indicator = JSON.parse(message.body);
    console.log('Typing indicator:', indicator);
  });

  // Subscribe to read receipts
  client.subscribe(`/topic/chat.${chatRoomId}.read`, (message) => {
    const receipt = JSON.parse(message.body);
    console.log('Read receipt:', receipt);
  });
};

// Handle errors
client.onStompError = function (frame) {
  console.error('STOMP error:', frame);
};

// Activate connection
client.activate();

// Send a message (after connected)
function sendMessage(content) {
  client.publish({
    destination: '/app/chat.send',
    body: JSON.stringify({
      chatRoomId: chatRoomId,
      content: content
    })
  });
}

// Send typing indicator
function sendTyping(isTyping) {
  client.publish({
    destination: '/app/chat.typing',
    body: JSON.stringify({
      chatRoomId: chatRoomId,
      typing: isTyping
    })
  });
}

// Mark message as read
function markAsRead(messageId = null) {
  client.publish({
    destination: '/app/chat.read',
    body: JSON.stringify({
      chatRoomId: chatRoomId,
      messageId: messageId
    })
  });
}

// Usage examples:
setTimeout(() => {
  sendMessage("Hello from WebSocket!");
  sendTyping(true);
  setTimeout(() => sendTyping(false), 3000);
  markAsRead(); // Mark all as read
}, 2000);

// Disconnect
// client.deactivate();
```

---

## Testing with Browser Console

### Quick Test Script

1. Open http://localhost:8080/swagger-ui.html
2. Login to get JWT token
3. Open browser console (F12)
4. Paste this code:

```javascript
// Replace with your token
const token = "YOUR_JWT_TOKEN_HERE";
const chatRoomId = 1;

const socket = new WebSocket('ws://localhost:8080/ws/chat');

socket.onopen = () => {
  console.log('WebSocket connected');
  
  // Send CONNECT frame
  const connectFrame = `CONNECT
Authorization:Bearer ${token}
accept-version:1.2
heart-beat:10000,10000

\x00`;
  socket.send(connectFrame);
};

socket.onmessage = (event) => {
  console.log('Received:', event.data);
};

socket.onerror = (error) => {
  console.error('WebSocket error:', error);
};

socket.onclose = () => {
  console.log('WebSocket disconnected');
};

// Send message (after CONNECTED frame received)
function sendMsg(content) {
  const frame = `SEND
destination:/app/chat.send
content-type:application/json

${JSON.stringify({chatRoomId, content})}
\x00`;
  socket.send(frame);
}

// Usage:
// sendMsg("Hello from console!");
```

---

## Testing Flow

### Complete Test Scenario

1. **Register two users:**
   ```bash
   # User 1
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username":"alice","email":"alice@example.com","password":"password123"}'
   
   # User 2
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username":"bob","email":"bob@example.com","password":"password123"}'
   ```

2. **Login both users and save tokens:**
   ```bash
   # Login Alice
   TOKEN_ALICE=$(curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"alice","password":"password123"}' | jq -r '.data.token')
   
   # Login Bob
   TOKEN_BOB=$(curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"bob","password":"password123"}' | jq -r '.data.token')
   ```

3. **Create a chat room:**
   ```bash
   # Alice creates chat with Bob (get Bob's ID first)
   curl -X POST http://localhost:8080/api/chats/direct \
     -H "Authorization: Bearer $TOKEN_ALICE" \
     -H "Content-Type: application/json" \
     -d '{"targetUserId":2}'
   ```

4. **Connect both users via WebSocket using the JavaScript example above**

5. **Send messages and observe real-time delivery**

---

## Expected Behavior

### When Alice sends a message:
1. Message is saved to database
2. Alice receives confirmation via `/topic/chat.1`
3. Bob receives message via:
    - `/topic/chat.1` (if subscribed)
    - `/user/queue/messages` (personal queue)

### When Bob starts typing:
1. Bob sends typing indicator: `{"typing": true}`
2. Alice receives typing indicator via `/topic/chat.1.typing`
3. After 3 seconds of no typing, Bob sends: `{"typing": false}`

### When Alice marks message as read:
1. Alice sends read receipt
2. Bob receives notification via `/topic/chat.1.read`
3. Message status updated in database

---

## Common Issues & Solutions

### Issue: "Authentication failed"
- Check JWT token is valid
- Ensure token is in Authorization header
- Token must start with "Bearer "

### Issue: "Not receiving messages"
- Verify subscription destinations are correct
- Check chat room ID matches
- Ensure user is a member of the chat room

### Issue: "Connection timeout"
- Check backend is running: `curl http://localhost:8080/api/health`
- Verify WebSocket endpoint: `ws://localhost:8080/ws/chat`
- Check firewall/proxy settings

### Issue: "CORS errors"
- Verify allowed origins in application.properties
- Check browser console for specific CORS errors
- Ensure origin matches exactly (no trailing slash)

---

## Monitoring

### Server Logs
Watch server logs for WebSocket activity:
```bash
cd ~/IdeaProjects/Chatty/backend
./gradlew bootRun

# Look for:
# - "WebSocket connection established"
# - "Received message from user X"
# - "Message broadcasted"
# - "WebSocket connection closed"
```

### Chrome DevTools
1. Open DevTools (F12)
2. Go to Network tab
3. Filter by "WS" (WebSocket)
4. Click on connection to see frames
5. Monitor sent/received messages

---

## Production Considerations

### For Production Deployment:
- Use secure WebSocket: `wss://` instead of `ws://`
- Implement message persistence/retry logic
- Add rate limiting for messages
- Consider using external message broker (RabbitMQ, Redis)
- Add connection pooling and load balancing
- Implement reconnection logic with exponential backoff
- Add message acknowledgments
- Monitor connection health with heartbeats

---

## Next Steps

1. Test basic message sending
2. Test typing indicators
3. Test read receipts
4. Test with multiple clients
5. Test reconnection scenarios
6. Load test with many concurrent connections

Happy testing! 🚀# WebSocket Testing Guide

## Overview

The Chatty application uses **STOMP over WebSocket** for real-time messaging. This guide will help you test the WebSocket functionality.

---

## Connection Setup

### WebSocket Endpoint
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

After connecting, subscribe to these destinations to receive messages:

### 1. Personal Message Queue
**Destination:** `/user/queue/messages`

Receives all messages sent directly to you.

### 2. Chat Room Messages
**Destination:** `/topic/chat.{roomId}`

Example: `/topic/chat.1`

Receives all messages in a specific chat room.

### 3. Typing Indicators
**Destination:** `/topic/chat.{roomId}.typing`

Example: `/topic/chat.1.typing`

Receives typing indicators for a specific chat room.

### 4. Read Receipts
**Destination:** `/topic/chat.{roomId}.read`

Example: `/topic/chat.1.read`

Receives read receipt notifications for a specific chat room.

---

## Send Destinations

Use these destinations to send messages to the server:

### 1. Send Message
**Destination:** `/app/chat.send`

**Payload:**
```json
{
  "chatRoomId": 1,
  "content": "Hello, how are you?"
}
```

### 2. Send Typing Indicator
**Destination:** `/app/chat.typing`

**Payload:**
```json
{
  "chatRoomId": 1,
  "typing": true
}
```

Send `"typing": false` when user stops typing.

### 3. Send Read Receipt
**Destination:** `/app/chat.read`

**Payload (mark specific message):**
```json
{
  "chatRoomId": 1,
  "messageId": 123
}
```

**Payload (mark all messages):**
```json
{
  "chatRoomId": 1,
  "messageId": null
}
```

---

## Testing with JavaScript

### Step 1: Install STOMP.js

```bash
npm install @stomp/stompjs
```

### Step 2: Connect and Test

```javascript
import { Client } from '@stomp/stompjs';

// Get JWT token from login
const token = "your-jwt-token-here";
const chatRoomId = 1;

// Create STOMP client
const client = new Client({
  brokerURL: 'ws://localhost:8080/ws/chat',
  connectHeaders: {
    Authorization: `Bearer ${token}`
  },
  debug: function (str) {
    console.log('STOMP: ' + str);
  },
  reconnectDelay: 5000,
  heartbeatIncoming: 4000,
  heartbeatOutgoing: 4000,
});

// Handle connection
client.onConnect = function (frame) {
  console.log('Connected:', frame);

  // Subscribe to personal queue
  client.subscribe('/user/queue/messages', (message) => {
    const msg = JSON.parse(message.body);
    console.log('Personal message:', msg);
  });

  // Subscribe to chat room
  client.subscribe(`/topic/chat.${chatRoomId}`, (message) => {
    const msg = JSON.parse(message.body);
    console.log('Chat room message:', msg);
  });

  // Subscribe to typing indicators
  client.subscribe(`/topic/chat.${chatRoomId}.typing`, (message) => {
    const indicator = JSON.parse(message.body);
    console.log('Typing indicator:', indicator);
  });

  // Subscribe to read receipts
  client.subscribe(`/topic/chat.${chatRoomId}.read`, (message) => {
    const receipt = JSON.parse(message.body);
    console.log('Read receipt:', receipt);
  });
};

// Handle errors
client.onStompError = function (frame) {
  console.error('STOMP error:', frame);
};

// Activate connection
client.activate();

// Send a message (after connected)
function sendMessage(content) {
  client.publish({
    destination: '/app/chat.send',
    body: JSON.stringify({
      chatRoomId: chatRoomId,
      content: content
    })
  });
}

// Send typing indicator
function sendTyping(isTyping) {
  client.publish({
    destination: '/app/chat.typing',
    body: JSON.stringify({
      chatRoomId: chatRoomId,
      typing: isTyping
    })
  });
}

// Mark message as read
function markAsRead(messageId = null) {
  client.publish({
    destination: '/app/chat.read',
    body: JSON.stringify({
      chatRoomId: chatRoomId,
      messageId: messageId
    })
  });
}

// Usage examples:
setTimeout(() => {
  sendMessage("Hello from WebSocket!");
  sendTyping(true);
  setTimeout(() => sendTyping(false), 3000);
  markAsRead(); // Mark all as read
}, 2000);

// Disconnect
// client.deactivate();
```

---

## Testing with Browser Console

### Quick Test Script

1. Open http://localhost:8080/swagger-ui.html
2. Login to get JWT token
3. Open browser console (F12)
4. Paste this code:

```javascript
// Replace with your token
const token = "YOUR_JWT_TOKEN_HERE";
const chatRoomId = 1;

const socket = new WebSocket('ws://localhost:8080/ws/chat');

socket.onopen = () => {
  console.log('WebSocket connected');
  
  // Send CONNECT frame
  const connectFrame = `CONNECT
Authorization:Bearer ${token}
accept-version:1.2
heart-beat:10000,10000

\x00`;
  socket.send(connectFrame);
};

socket.onmessage = (event) => {
  console.log('Received:', event.data);
};

socket.onerror = (error) => {
  console.error('WebSocket error:', error);
};

socket.onclose = () => {
  console.log('WebSocket disconnected');
};

// Send message (after CONNECTED frame received)
function sendMsg(content) {
  const frame = `SEND
destination:/app/chat.send
content-type:application/json

${JSON.stringify({chatRoomId, content})}
\x00`;
  socket.send(frame);
}

// Usage:
// sendMsg("Hello from console!");
```

---

## Testing Flow

### Complete Test Scenario

1. **Register two users:**
   ```bash
   # User 1
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username":"alice","email":"alice@example.com","password":"password123"}'
   
   # User 2
   curl -X POST http://localhost:8080/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username":"bob","email":"bob@example.com","password":"password123"}'
   ```

2. **Login both users and save tokens:**
   ```bash
   # Login Alice
   TOKEN_ALICE=$(curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"alice","password":"password123"}' | jq -r '.data.token')
   
   # Login Bob
   TOKEN_BOB=$(curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"bob","password":"password123"}' | jq -r '.data.token')
   ```

3. **Create a chat room:**
   ```bash
   # Alice creates chat with Bob (get Bob's ID first)
   curl -X POST http://localhost:8080/api/chats/direct \
     -H "Authorization: Bearer $TOKEN_ALICE" \
     -H "Content-Type: application/json" \
     -d '{"targetUserId":2}'
   ```

4. **Connect both users via WebSocket using the JavaScript example above**

5. **Send messages and observe real-time delivery**

---

## Expected Behavior

### When Alice sends a message:
1. Message is saved to database
2. Alice receives confirmation via `/topic/chat.1`
3. Bob receives message via:
    - `/topic/chat.1` (if subscribed)
    - `/user/queue/messages` (personal queue)

### When Bob starts typing:
1. Bob sends typing indicator: `{"typing": true}`
2. Alice receives typing indicator via `/topic/chat.1.typing`
3. After 3 seconds of no typing, Bob sends: `{"typing": false}`

### When Alice marks message as read:
1. Alice sends read receipt
2. Bob receives notification via `/topic/chat.1.read`
3. Message status updated in database

---

## Common Issues & Solutions

### Issue: "Authentication failed"
- Check JWT token is valid
- Ensure token is in Authorization header
- Token must start with "Bearer "

### Issue: "Not receiving messages"
- Verify subscription destinations are correct
- Check chat room ID matches
- Ensure user is a member of the chat room

### Issue: "Connection timeout"
- Check backend is running: `curl http://localhost:8080/api/health`
- Verify WebSocket endpoint: `ws://localhost:8080/ws/chat`
- Check firewall/proxy settings

### Issue: "CORS errors"
- Verify allowed origins in application.properties
- Check browser console for specific CORS errors
- Ensure origin matches exactly (no trailing slash)

---

## Monitoring

### Server Logs
Watch server logs for WebSocket activity:
```bash
cd ~/IdeaProjects/Chatty/backend
./gradlew bootRun

# Look for:
# - "WebSocket connection established"
# - "Received message from user X"
# - "Message broadcasted"
# - "WebSocket connection closed"
```

### Chrome DevTools
1. Open DevTools (F12)
2. Go to Network tab
3. Filter by "WS" (WebSocket)
4. Click on connection to see frames
5. Monitor sent/received messages

---

## Production Considerations

### For Production Deployment:
- Use secure WebSocket: `wss://` instead of `ws://`
- Implement message persistence/retry logic
- Add rate limiting for messages
- Consider using external message broker (RabbitMQ, Redis)
- Add connection pooling and load balancing
- Implement reconnection logic with exponential backoff
- Add message acknowledgments
- Monitor connection health with heartbeats

---

## Next Steps

1. Test basic message sending
2. Test typing indicators
3. Test read receipts
4. Test with multiple clients
5. Test reconnection scenarios
6. Load test with many concurrent connections

Happy testing! 🚀