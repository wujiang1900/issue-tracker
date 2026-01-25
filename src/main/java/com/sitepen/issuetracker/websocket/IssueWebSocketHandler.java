package com.sitepen.issuetracker.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class IssueWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;

    // Store active WebSocket sessions
    // Key: sessionId, Value: WebSocketSession
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // Store project subscriptions
    // Key: projectId, Value: Set of sessionIds
    private final Map<String, Set<String>> projectSubscriptions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        log.info("WebSocket connection established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("Received message from {}: {}", session.getId(), payload);

        try {
            // Parse subscription message: {"action": "subscribe", "projectId": "123"}
            Map<String, String> request = objectMapper.readValue(payload, Map.class);
            String action = request.get("action");
            String projectId = request.get("projectId");

            if ("subscribe".equals(action) && projectId != null) {
                subscribeToProject(session.getId(), projectId);
                sendMessage(session, Map.of(
                        "type", "subscription",
                        "status", "success",
                        "projectId", projectId
                ));
            } else if ("unsubscribe".equals(action) && projectId != null) {
                unsubscribeFromProject(session.getId(), projectId);
                sendMessage(session, Map.of(
                        "type", "subscription",
                        "status", "unsubscribed",
                        "projectId", projectId
                ));
            }
        } catch (Exception e) {
            log.error("Error processing message from {}: {}", session.getId(), e.getMessage());
            sendMessage(session, Map.of(
                    "type", "error",
                    "message", "Invalid message format"
            ));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);

        // Remove from all project subscriptions
        projectSubscriptions.values().forEach(subscribers -> subscribers.remove(sessionId));

        log.info("WebSocket connection closed: {} with status: {}", sessionId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
        session.close(CloseStatus.SERVER_ERROR);
    }

    /**
     * Subscribe a session to project notifications
     */
    private void subscribeToProject(String sessionId, String projectId) {
        projectSubscriptions.computeIfAbsent(projectId, k -> ConcurrentHashMap.newKeySet())
                .add(sessionId);
        log.info("Session {} subscribed to project {}", sessionId, projectId);
    }

    /**
     * Unsubscribe a session from project notifications
     */
    private void unsubscribeFromProject(String sessionId, String projectId) {
        Set<String> subscribers = projectSubscriptions.get(projectId);
        if (subscribers != null) {
            subscribers.remove(sessionId);
            if (subscribers.isEmpty()) {
                projectSubscriptions.remove(projectId);
            }
        }
        log.info("Session {} unsubscribed from project {}", sessionId, projectId);
    }

    /**
     * Broadcast a message to all subscribers of a project
     */
    public void broadcastToProject(String projectId, Object message) {
        Set<String> subscribers = projectSubscriptions.get(projectId);
        if (subscribers == null || subscribers.isEmpty()) {
            log.debug("No subscribers for project {}", projectId);
            return;
        }

        log.info("Broadcasting to {} subscribers of project {}", subscribers.size(), projectId);

        subscribers.forEach(sessionId -> {
            WebSocketSession session = sessions.get(sessionId);
            if (session != null && session.isOpen()) {
                sendMessage(session, message);
            }
        });
    }

    /**
     * Send a message to all connected sessions
     */
    public void broadcastToAll(Object message) {
        log.info("Broadcasting to all {} connected sessions", sessions.size());

        sessions.values().forEach(session -> {
            if (session.isOpen()) {
                sendMessage(session, message);
            }
        });
    }

    /**
     * Send a message to a specific session
     */
    private void sendMessage(WebSocketSession session, Object message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("Error sending message to session {}: {}", session.getId(), e.getMessage());
        }
    }
}
