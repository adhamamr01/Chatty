# 💬 Chatty - Real-Time Chat Application

A modern, full-stack chat application built with **Spring Boot** (backend) and **Kotlin Compose Multiplatform** (frontend).

![Status](https://img.shields.io/badge/status-MVP%20Complete-success)
![Backend](https://img.shields.io/badge/backend-Spring%20Boot%203.2-green)
![Frontend](https://img.shields.io/badge/frontend-Compose%20Multiplatform-blue)

---

## 🚀 Features

### ✅ Completed (Phase 1 - MVP)
- **Authentication**: JWT-based user registration and login
- **Real-time Messaging**: Send and receive messages with auto-refresh
- **Chat Management**: View all conversations, create 1-on-1 chats
- **User Interface**: Clean Material 3 design on Android
- **Message History**: Paginated message loading
- **Read Receipts**: Track message delivery status
- **Secure API**: Protected endpoints with JWT tokens

### 🔄 In Progress (Phase 2)
- WebSocket/STOMP real-time updates (backend ready, client pending)
- Message status improvements (SENT → DELIVERED → READ)
- User search from app
- Typing indicators
- Push notifications

### 📋 Planned (Phase 3)
- Group chat support
- File/image sharing
- Message search
- Online status indicators
- Web client
- Admin dashboard

---

## 🏗️ Architecture

### Backend
- **Framework**: Spring Boot 3.2
- **Database**: PostgreSQL
- **Authentication**: JWT (24-hour expiration)
- **Real-time**: WebSocket with STOMP protocol
- **API Docs**: Swagger/OpenAPI
- **Security**: BCrypt password hashing

### Frontend
- **Framework**: Kotlin Compose Multiplatform
- **Platform**: Android (iOS/Web planned)
- **Networking**: Ktor Client
- **State Management**: Kotlin Flow & StateFlow
- **Navigation**: Voyager

---

## 📁 Project Structure

```
Chatty/
├── backend/                    # Spring Boot backend
│   ├── src/main/java/com/chatapp/
│   │   ├── config/            # Security, WebSocket config
│   │   ├── controller/        # REST & WebSocket controllers
│   │   ├── dto/               # Data Transfer Objects
│   │   ├── exception/         # Global exception handling
│   │   ├── model/             # JPA entities
│   │   ├── repository/        # Spring Data repositories
│   │   ├── security/          # JWT utilities, filters
│   │   ├── service/           # Business logic
│   │   └── websocket/         # WebSocket configuration
│   └── build.gradle
│
├── client/                     # Compose Multiplatform frontend
│   ├── composeApp/
│   │   ├── src/commonMain/kotlin/com/chatapp/client/
│   │   │   ├── data/
│   │   │   │   ├── model/    # Data models
│   │   │   │   └── remote/   # API & WebSocket clients
│   │   │   ├── ui/
│   │   │   │   └── screens/  # UI screens
│   │   │   └── App.kt
│   │   └── src/androidMain/
│   └── build.gradle.kts
│
└── postman/                    # API testing
    └── Chatty-API-MultiUser.postman_collection.json
```

---

## 🚀 Getting Started

### Prerequisites
- **Java 17+**
- **Gradle 8+**
- **PostgreSQL 14+**
- **Android Studio** (for mobile client)

### Backend Setup

1. **Create Database**
```sql
CREATE DATABASE chatty;
```

2. **Configure Application**

Create `backend/src/main/resources/application.properties`:
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/chatty
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

# JWT Secret (generate with: openssl rand -base64 64)
jwt.secret=YOUR_GENERATED_SECRET_HERE
jwt.expiration=86400000

# CORS
cors.allowed-origins=http://localhost:3000,http://localhost:8081
websocket.allowed-origins=http://localhost:3000,http://localhost:8081
```

3. **Run Backend**
```bash
cd backend
./gradlew bootRun
```

Backend will start on `http://localhost:8080`

4. **Access Swagger UI**
```
http://localhost:8080/swagger-ui.html
```

### Frontend Setup

1. **Open in Android Studio**
```bash
cd client
# Open in Android Studio
```

2. **Sync Gradle**
- Let Android Studio download dependencies

3. **Run on Emulator/Device**
- Click Run button or press Shift+F10

---

## 🧪 Testing

### Postman Collection
Import `postman/Chatty-API-MultiUser.postman_collection.json` for:
- Multi-user testing (Alice & Bob)
- Pre-configured requests with auto-saved tokens
- Two-user chat scenarios

### Manual Testing
1. **Register Two Users** (e.g., Alice & Bob)
2. **Create Chat** between them (via Postman or app)
3. **Send Messages** from both accounts
4. **Observe** auto-refresh picking up messages

---

## 📊 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token

### Users
- `GET /api/users/me` - Get current user profile
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/search?q={query}` - Search users

### Chats
- `GET /api/chats` - Get all user's chats
- `POST /api/chats/direct` - Create/get direct chat
- `GET /api/chats/{id}` - Get chat room details
- `GET /api/chats/{id}/messages` - Get messages (paginated)
- `POST /api/chats/{id}/messages` - Send message
- `PUT /api/chats/{id}/read` - Mark all messages as read

### Messages
- `PUT /api/messages/{id}/read` - Mark message as read

---

## 🐛 Known Issues

### Message Status
- ⚠️ Message status transitions (SENT → DELIVERED → READ) need refinement
- Currently all messages show as SENT initially
- Read receipts work but may not reflect immediately
- **Planned Fix**: Phase 2 with WebSocket implementation

### Auto-Refresh
- Uses 2-second polling instead of real-time WebSocket
- Can cause slight delay in message delivery
- **Planned Fix**: Implement STOMP client in Phase 2

---

## 🔐 Security

- **Password Hashing**: BCrypt with strength 10
- **JWT Tokens**: HS512 algorithm, 24-hour expiration
- **CORS**: Configured for development (restrict in production)
- **SQL Injection**: Protected via JPA/Hibernate
- **Authentication**: Required for all chat endpoints

⚠️ **For Production**: 
- Use HTTPS
- Store JWT secret in environment variables
- Implement token refresh mechanism
- Add rate limiting
- Enable CSRF protection

---

## 📈 Performance

- **Database**: Indexed on `chat_room_id` and `created_at`
- **Pagination**: Default 20 messages per page
- **Connection Pooling**: HikariCP (Spring Boot default)
- **Auto-refresh**: 2-second interval (configurable)

---

## 🤝 Contributing

This is a personal project, but suggestions are welcome!

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

---

## 📝 Development Notes

### Tech Stack Decisions
- **Spring Boot**: Mature, enterprise-ready framework
- **PostgreSQL**: Robust relational database
- **Compose Multiplatform**: Modern, declarative UI framework
- **Ktor**: Lightweight HTTP client for multiplatform

### Phase 1 Completion Date
December 2024

### Next Milestone
Phase 2 - Real-time WebSocket + Enhanced UX (Q1 2025)

---

## 📄 License

This project is for educational and portfolio purposes.

---

## 👤 Author

**Adham Amr**
- GitHub: [@adhamamr01](https://github.com/adhamamr01)

---

## 🙏 Acknowledgments

- Spring Framework Team
- JetBrains (Kotlin & Compose Multiplatform)
- Anthropic Claude (Development assistance)
