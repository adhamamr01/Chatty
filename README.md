# Chatty - Real-time Chat Application

A modern real-time chat application built with Spring Boot backend and Kotlin Compose Multiplatform frontend.

## 🏗️ Project Structure

```
Chatty/
├── backend/          # Spring Boot REST API + WebSocket server
├── client/           # Kotlin Compose Multiplatform (Android + Web)
└── docs/             # Additional documentation
```

## ✨ Features

### MVP Features (Current)
- ✅ User authentication (Register/Login with JWT)
- ✅ Real-time 1-on-1 messaging via WebSocket
- ✅ Message history with pagination
- ✅ Typing indicators
- ✅ Read receipts
- ✅ User search

### Future Features
- Group chats
- Online status indicators
- Push notifications
- Media sharing (images, files)
- Message reactions
- Voice messages

## 🚀 Quick Start

### Prerequisites
- Java 17+
- PostgreSQL 14+
- Node.js 16+ (for web frontend)
- Android Studio (for Android development)

### Backend Setup

1. **Create Database**
   ```bash
   createdb chatty
   ```

2. **Configure Application**
   ```bash
   cd backend/src/main/resources
   cp application.properties.example application.properties
   # Edit application.properties with your database credentials and JWT secret
   ```

3. **Generate JWT Secret**
   ```bash
   openssl rand -base64 64
   ```
   Copy the output to `jwt.secret` in `application.properties`

4. **Run Backend**
   ```bash
   cd backend
   ./gradlew bootRun
   ```

   Backend will start on http://localhost:8080

### Frontend Setup

Coming soon...

## 📚 Documentation

### API Documentation
Once the backend is running:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Overview**: http://localhost:8080/docs/api-overview.md
- **WebSocket Guide**: http://localhost:8080/docs/websocket-api.md

### Project Documentation
- [Backend README](backend/README.md) - Backend setup and development guide
- [Configuration Guide](backend/CONFIGURATION.md) - Detailed configuration options
- [Git Setup Guide](GIT_SETUP.md) - Git workflow and best practices

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.2
- **Language**: Java 17
- **Database**: PostgreSQL
- **Authentication**: JWT (JSON Web Tokens)
- **Real-time**: WebSocket (STOMP)
- **Documentation**: Swagger/OpenAPI
- **Build Tool**: Gradle

### Frontend
- **Framework**: Compose Multiplatform
- **Language**: Kotlin
- **Platforms**: Android, Web
- **HTTP Client**: Ktor Client
- **WebSocket**: Ktor WebSocket
- **Local Storage**: SQLDelight
- **DI**: Koin

## 🏃 Development

### Backend Development

```bash
# Run backend
cd backend
./gradlew bootRun

# Run tests
./gradlew test

# Build
./gradlew build

# Clean build
./gradlew clean build
```

### Running Both Backend and Frontend

```bash
# Terminal 1: Backend
cd backend
./gradlew bootRun

# Terminal 2: Frontend (when ready)
cd client
# Commands will be added later
```

## 🌳 Git Workflow

### Branch Strategy
```
main
  └── develop
      ├── feature/auth-service
      ├── feature/chat-service
      ├── feature/websocket
      └── feature/frontend-ui
```

### Commit Convention
```bash
feat(backend): add user authentication
feat(frontend): implement chat list screen
fix(backend): resolve JWT token expiration
docs: update API documentation
```

## 📝 Environment Variables

For production, use environment variables instead of `application.properties`:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/chatty
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
export JWT_SECRET=your_generated_secret
export CORS_ORIGINS=https://yourdomain.com
```

## 🧪 Testing

### Backend Tests
```bash
cd backend
./gradlew test
```

### Frontend Tests
Coming soon...

## 📦 Deployment

### Backend Deployment

**Using Docker:**
```bash
cd backend
docker build -t chatty-backend .
docker run -p 8080:8080 chatty-backend
```

**Using JAR:**
```bash
cd backend
./gradlew bootJar
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar
```

### Frontend Deployment
Coming soon...

## 🤝 Contributing

1. Create a feature branch from `develop`
2. Make your changes
3. Write/update tests
4. Ensure all tests pass
5. Submit a pull request to `develop`

## 📄 License

MIT License

## 👥 Authors

- Your Name - Initial work

## 🆘 Support

For issues and questions:
- Create an issue in the repository
- Check documentation in `docs/` folder
- Review API documentation at http://localhost:8080/swagger-ui.html

## 🗺️ Roadmap

### Phase 1: MVP (Current)
- [x] Backend setup with Spring Boot
- [x] JWT authentication
- [x] WebSocket for real-time messaging
- [x] Basic REST API endpoints
- [ ] Service layer implementation
- [ ] WebSocket controllers
- [ ] Frontend setup
- [ ] Basic UI screens

### Phase 2: Enhancement
- [ ] Group chat support
- [ ] Online status indicators
- [ ] Push notifications
- [ ] File sharing
- [ ] Message search

### Phase 3: Polish
- [ ] Performance optimization
- [ ] Enhanced UI/UX
- [ ] Admin panel
- [ ] Analytics dashboard
- [ ] Deployment automation

## 📊 Project Status

**Current Phase**: MVP Development - Backend Implementation

**Completed**:
- ✅ Project structure
- ✅ Database schema
- ✅ Domain models
- ✅ Repository layer
- ✅ Security configuration
- ✅ JWT authentication
- ✅ DTOs and response wrappers
- ✅ API documentation setup

**In Progress**:
- 🔄 Service layer
- 🔄 REST controllers
- 🔄 WebSocket configuration

**Next Up**:
- ⏳ Frontend setup
- ⏳ UI implementation
- ⏳ End-to-end testing

---

**Last Updated**: December 2024