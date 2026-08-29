package com.example

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.server.request.uri
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import java.util.concurrent.ConcurrentHashMap
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json { prettyPrint = false; isLenient = true })
    }

    install(WebSockets)

    // Sessions map: userId -> session
    val sessions = ConcurrentHashMap<String, DefaultWebSocketServerSession>()

    routing {
        webSocket("/chat") {
            // `this` is DefaultWebSocketServerSession
            val currentSession = this

            // Identify user by query parameter `userId`. If not provided, generate one.
            val userId = call.request.queryParameters["userId"] ?: UUID.randomUUID().toString()
            environment?.log?.info("New websocket connect: userId=$userId, uri=${call.request.uri}")

            // register session
            sessions[userId] = currentSession

            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        
                        if (text.contains("\"type\":\"typing\"")) {
                            // Handle typing indicator broadcast
                            val jsonElement = try { Json.parseToJsonElement(text) } catch (_: Exception) { null }
                            val isTyping = jsonElement?.jsonObject?.get("isTyping")?.jsonPrimitive?.boolean ?: false
                            val conversationId = jsonElement?.jsonObject?.get("conversationId")?.jsonPrimitive?.content ?: ""

                            val broadcastJson = buildJsonObject {
                                put("type", "typing")
                                put("userId", userId)
                                put("conversationId", conversationId)
                                put("isTyping", isTyping)
                            }.toString()

                            sessions.forEach { (otherUserId, session) ->
                                if (otherUserId == userId) return@forEach
                                launch {
                                    try {
                                        session.send(Frame.Text(broadcastJson))
                                    } catch (_: Exception) {
                                        environment?.log?.error("Failed to send typing indicator to $otherUserId")
                                    }
                                }
                            }
                        } else {
                            // Parse incoming JSON to MessageDto. If malformed, log and continue.
                            val incomingMsg = try {
                                Json.decodeFromString(MessageDto.serializer(), text)
                            } catch (t: Throwable) {
                                environment?.log?.error("Malformed message from user $userId: ${t.message}")
                                continue
                            }

                            // Generate server id and timestamp
                            val serverId = UUID.randomUUID().toString()
                            val timestamp = System.currentTimeMillis()
                            val confirmed = incomingMsg.copy(id = serverId, timestamp = timestamp)

                            // Simulated latency for confirmation (500ms)
                            launch {
                                delay(500.milliseconds)
                                try {
                                    currentSession.send(Frame.Text(Json.encodeToString(MessageDto.serializer(), confirmed)))
                                } catch (t: Throwable) {
                                    environment?.log?.error("Failed to send confirmation to $userId: ${t.message}")
                                }
                            }

                            // Broadcast to all other connected sessions (non-blocking)
                            val broadcastText = Json.encodeToString(MessageDto.serializer(), confirmed)
                            sessions.forEach { (otherUserId, session) ->
                                if (otherUserId == userId) return@forEach
                                launch {
                                    try {
                                        session.send(Frame.Text(broadcastText))
                                    } catch (t: Throwable) {
                                        environment?.log?.error("Failed to broadcast to $otherUserId: ${t.message}")
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (t: Throwable) {
                environment?.log?.error("Websocket session error for user $userId: ${t.message}")
            } finally {
                // cleanup
                sessions.remove(userId)
                environment?.log?.info("Websocket disconnected: userId=$userId")
            }
        }
    }
}
