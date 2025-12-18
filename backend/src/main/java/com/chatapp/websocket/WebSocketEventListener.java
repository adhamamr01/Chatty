package com.chatapp.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocket event listener for handling connection and disconnection events.
 * Logs user connections and disconnections for monitoring and debugging.
 */
@Slf4j
@Component
public class WebSocketEventListener {

    /**
     * Handles WebSocket connection events.
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        log.info("WebSocket connection established - Session ID: {}", sessionId);
    }

    /**
     * Handles WebSocket disconnection events.
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        if (headerAccessor.getUser() != null) {
            log.info("WebSocket connection closed - Session ID: {}, User: {}",
                    sessionId, headerAccessor.getUser().getName());
        } else {
            log.info("WebSocket connection closed - Session ID: {}", sessionId);
        }
    }
}