# Chat Application - Backend

Spring Boot backend for the Chat Application with REST API and WebSocket support.

## 📋 Prerequisites

- Java 17 or higher
- PostgreSQL 14 or higher
- Gradle 8.x (wrapper included)

## 🚀 Quick Start

### 1. Database Setup

Create a PostgreSQL database:

```bash
createdb chatapp
```

Or using psql:

```sql
CREATE DATABASE chatapp;
```

### 2. Configuration

Update `src/main/resources/application.properties` with your database credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/chatapp
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Run the Application

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

Once the application is running, access the interactive API documentation:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI Spec:** http://localhost:8080/v3/api-docs

### Additional Documentation

- **API Overview:** http://localhost:8080/docs/api-overview.md
- **WebSocket Guide:** http://localhost:8080/docs/websocket-api.md

## 🏗️ Project Structure

```
backend/
├── src/main/java/com/chatapp/
│   ├── ChatApplication.java          # Main application class
│   ├── config/                        # Configuration classes
│   │   ├── OpenApiConfig.java        # Swagger/OpenAPI config
│   │   └── SecurityConfig.java       # Security configuration
│   ├── controller/                    # REST controllers
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   └── ChatController.java
│   ├── websocket/                     # WebSocket configuration
│   │   ├── WebSocketConfig.java
│   │   └── ChatWebSocketController.java
│   ├── service/                       # Business logic
│   │   ├── AuthService.java
│   │   ├── UserService.java
│   │   └── ChatService.java
│   ├── repository/                    # Data access
│   │   ├── UserRepository.java
│   │   ├── ChatRoomRepository.java
│   │   ├── ChatRoomMemberRepository.java
│   │   └── MessageRepository.java
│   ├── model/                         # Domain entities
│   │   ├── User.java
│   │   ├── ChatRoom.java
│   │   ├── ChatRoomMember.java
│   │   ├── Message.java
│   │   └── MessageStatus.java
│   ├── dto/                           # Data transfer objects
│   │   ├── AuthResponse.java
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── UserDTO.java
│   │   ├── ChatRoomDTO.java
│   │   ├── MessageDTO.java
│   │   └── ...
│   └── security/                      # Security components
│       ├── JwtUtil.java
│       ├── JwtAuthenticationFilter.java
│       └── SecurityConfig.java
│
└── src/main/resources/
    ├── application.properties         # Application configuration
    └── static/docs/                   # Static documentation
        ├── api-overview.md
        └── websocket-api.md
```

## 🔧 Development

### Build

```bash
./gradlew build
```

### Run Tests

```bash
./gradlew test
```

### Clean Build

```bash
./gradlew clean build
```

## 🔐 Authentication

The API uses JWT (JSON Web Tokens) for authentication.

1. Register a user: `POST /api/auth/register`
2. Login: `POST /api/auth/login` (returns JWT token)
3. Include token in subsequent requests: `Authorization: Bearer <token>`

## 🌐 WebSocket Connection

Connect to: `ws://localhost:8080/ws/chat`

Include JWT token in connection headers:
```javascript
{
  "Authorization": "Bearer <your-jwt-token>"
}
```

See [WebSocket Documentation](http://localhost:8080/docs/websocket-api.md) for details.

## 📊 Database Schema

### Users Table
- `id` - Primary key
- `username` - Unique username
- `email` - Unique email
- `password_hash` - Hashed password
- `display_name` - Display name
- `created_at` - Account creation timestamp

### Chat Rooms Table
- `id` - Primary key
- `created_at` - Room creation timestamp

### Chat Room Members Table
- `id` - Primary key
- `chat_room_id` - Foreign key to chat_rooms
- `user_id` - Foreign key to users
- `joined_at` - Membership timestamp

### Messages Table
- `id` - Primary key
- `chat_room_id` - Foreign key to chat_rooms
- `sender_id` - Foreign key to users
- `content` - Message text
- `status` - SENT, DELIVERED, or READ
- `created_at` - Message timestamp

## 🛠️ Key Technologies

- **Spring Boot 3.2.x** - Application framework
- **Spring Security** - Authentication & authorization
- **Spring WebSocket** - Real-time messaging
- **Spring Data JPA** - Database operations
- **PostgreSQL** - Database
- **JWT** - Token-based authentication
- **Swagger/OpenAPI** - API documentation
- **Lombok** - Boilerplate reduction

## 📝 Environment Variables

You can override configuration using environment variables:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/chatapp
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=your-secret-key
export JWT_EXPIRATION=86400000
export SERVER_PORT=8080
```

## 🐳 Docker Support (Future)

```bash
# Build image
docker build -t chatapp-backend .

# Run container
docker run -p 8080:8080 chatapp-backend
```

## 🤝 Contributing

1. Create a feature branch
2. Make your changes
3. Write/update tests
4. Submit a pull request

## 📄 License

MIT License

## 🆘 Troubleshooting

### Database Connection Issues
- Verify PostgreSQL is running: `pg_isready`
- Check credentials in `application.properties`
- Ensure database exists: `psql -l`

### Port Already in Use
Change the port in `application.properties`:
```properties
server.port=8081
```

### JWT Token Errors
- Ensure token is included in Authorization header
- Check token hasn't expired (default: 24 hours)
- Verify JWT secret is configured correctly

## 📞 Support

For issues and questions:
- Create an issue in the repository
- Contact: support@chatapp.com