# Pulse Chat Application - Backend Server

A high-performance, real-time chat backend built with **Ktor WebSocket** server. This backend powers the Pulse Chat Application with real-time messaging, typing indicators, and multi-user session management.

## 🎯 Purpose

The Pulse Chat Application Backend is a WebSocket-based server that enables:
- Real-time message delivery between multiple connected clients
- Typing indicator broadcasting for real-time user presence awareness
- Server-side message confirmation with automatic ID and timestamp generation
- Concurrent session management with thread-safe operations
- Robust error handling and logging

## 🛠️ Tech Stack

### Core Framework
- **Kotlin 2.4.0** - Modern, expressive JVM language with null safety
- **Ktor 2.3.4** - Lightweight, asynchronous web framework
- **Java 17** - Target JVM version for better performance and features

### Key Dependencies
- **Ktor Server Core & Netty** - Async HTTP server engine
- **Ktor WebSockets** - Full-duplex WebSocket support
- **Ktor Content Negotiation** - Request/response serialization
- **kotlinx-serialization** - Type-safe JSON serialization/deserialization
- **Logback** - Structured logging framework

### Build System
- **Gradle 9.x** - Build automation with Kotlin DSL

## ✨ Features

- **WebSocket Communication** - Real-time bidirectional communication between clients and server
- **Message Handling** - Receives, processes, and broadcasts messages to all connected clients
- **Server-Side Confirmation** - Automatic message ID generation and timestamp attribution
- **Typing Indicators** - Real-time typing status broadcast to indicate when users are typing
- **Session Management** - Thread-safe tracking of active client connections
- **Error Resilience** - Graceful handling of malformed messages and connection failures
- **Comprehensive Logging** - Structured logging of all significant events (connections, errors, etc.)
- **Asynchronous Processing** - Non-blocking message broadcasting using Kotlin coroutines

## 📦 Installation

### Prerequisites
- Java 17 or higher
- Gradle 9.x (optional - can use the included Gradle wrapper)

### Setup Steps

1. **Clone the repository:**
```bash
git clone https://github.com/AdiPatil06/ChatApplicationBackend.git
cd ChatApplicationBackend
```

2. **Build the project:**
```bash
./gradlew build
```

3. **Run the server:**
```bash
./gradlew run
```

The server will start on `http://localhost:8080`

## 🔌 WebSocket API

### Connection Endpoint
```
ws://localhost:8080/chat?userId=<USER_ID>
```

**Query Parameters:**
- `userId` (optional) - Unique identifier for the user. If not provided, a UUID will be auto-generated.

### Message Format

#### Chat Message
Send and receive messages using the following JSON format:

```json
{
  "clientId": "client-generated-id",
  "conversationId": "conversation-id",
  "senderId": "sender-user-id",
  "content": "message content",
  "id": "server-generated-id",
  "timestamp": 1693468800000
}
```

**Fields:**
- `clientId` (optional) - Client-side unique identifier for the message
- `conversationId` - ID of the conversation/group chat
- `senderId` - ID of the user sending the message
- `content` - The actual message text
- `id` - Server-generated unique message ID (populated by server on confirmation)
- `timestamp` - Server-generated timestamp in milliseconds (populated by server on confirmation)

#### Typing Indicator
Broadcast typing status to other connected users:

```json
{
  "type": "typing",
  "userId": "user-id",
  "conversationId": "conversation-id",
  "isTyping": true
}
```

**Fields:**
- `type` - Must be `"typing"`
- `userId` - ID of the user typing
- `conversationId` - ID of the conversation
- `isTyping` - Boolean indicating typing status (true/false)

### Message Flow

1. **Send Message**
   - Client sends message JSON via WebSocket
   - Server receives and parses the message
   - Server generates unique `id` and `timestamp`

2. **Message Confirmation** (500ms delay)
   - Server sends confirmation back to sender with generated `id` and `timestamp`
   - Used to confirm message delivery and associate with server state

3. **Message Broadcast**
   - Server broadcasts the confirmed message to all other connected clients
   - Typing indicators are also broadcast to all except the sender

## 🏗️ Architecture

### Session Management
- Uses `ConcurrentHashMap<String, DefaultWebSocketServerSession>` for thread-safe session tracking
- Each connected user is identified by a unique `userId`
- Sessions are automatically cleaned up on disconnection

### Message Processing Pipeline
1. Incoming WebSocket frame parsing
2. JSON deserialization with error handling
3. Server-side enrichment (ID and timestamp generation)
4. Confirmation response to sender (with simulated 500ms latency)
5. Broadcast to all other connected sessions

### Error Handling
- Malformed JSON messages are logged but don't close the connection
- Failed message sends are logged with the recipient information
- WebSocket session errors are captured and logged for debugging

## 🚀 Running the Application

### Using Gradle Wrapper (Recommended)
```bash
./gradlew run
```

### Using Local Gradle Installation
```bash
gradle run
```

### Custom Configuration
To modify the server port or host, edit the `main()` function in `Application.kt`:
```kotlin
embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
    module()
}.start(wait = true)
```

## 📊 Logging

The application uses Logback for structured logging. Log configuration can be found in `src/main/resources/logback.xml`. Events logged include:
- New WebSocket connections with user ID
- Message reception and broadcast attempts
- Errors in message processing or delivery
- WebSocket disconnections

## 🔒 Security Considerations

- User IDs should be validated and authenticated on the client side
- Consider implementing:
  - Token-based authentication
  - Message rate limiting
  - User authorization checks
  - Input validation and sanitization
  - TLS/WSS encryption for production

## 📝 Development

### Code Structure
```
src/main/kotlin/
├── Application.kt     # Main server setup and WebSocket routing
└── MessageDto.kt      # Data class for message serialization
```

### Adding Features
- New endpoints can be added in the `routing` block in `Application.kt`
- Custom data models can be added as new Kotlin data classes with `@Serializable` annotation
- New message types can be handled with additional conditional logic in the message frame handler

## 🤝 Contributing

1. Create a feature branch
2. Make your changes
3. Run tests and validation: `./gradlew build`
4. Create a pull request

## 📄 License

This project is open source and available for personal and educational use.

## 📞 Support

For issues, questions, or contributions, please open an issue or contact the development team.
