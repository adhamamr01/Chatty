package com.chatapp.websocket;

import com.chatapp.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Collections;
import java.util.List;

/**
 * WebSocket configuration for real-time messaging using STOMP protocol.
 * Configures message broker, endpoints, and JWT authentication for WebSocket connections.
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtUtil jwtUtil;

    @Value("${websocket.allowed-origins}")
    private String allowedOrigins;

    /**
     * Configures message broker for handling messages.
     * - /topic: for broadcasting messages (pub-sub)
     * - /queue: for point-to-point messages
     * - /app: application destination prefix
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable simple in-memory message broker
        // Messages sent to /topic will be broadcast to all subscribers
        // Messages sent to /queue will be sent to specific users
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefix for messages bound for @MessageMapping methods
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix for user-specific messages
        registry.setUserDestinationPrefix("/user");

        log.info("Message broker configured with prefixes: /topic, /queue, /app");
    }

    /**
     * Registers STOMP endpoints for WebSocket connections.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/chat")
                .setAllowedOrigins(allowedOrigins.split(","))
                .withSockJS(); // Enable SockJS fallback for browsers that don't support WebSocket

        registry.addEndpoint("/ws/chat")
                .setAllowedOrigins(allowedOrigins.split(","));

        log.info("STOMP endpoint registered at /ws/chat with allowed origins: {}", allowedOrigins);
    }

    /**
     * Configures client inbound channel for JWT authentication.
     * Intercepts CONNECT frames to validate JWT tokens.
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                        message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Extract JWT token from headers
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);

                        try {
                            // Validate token
                            if (jwtUtil.validateToken(token)) {
                                // Extract user information
                                String username = jwtUtil.extractUsername(token);
                                Long userId = jwtUtil.extractUserId(token);

                                // Create authentication object
                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                userId,
                                                null,
                                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                                        );

                                // Set user in session
                                accessor.setUser(authentication);

                                log.info("WebSocket connection authenticated for user: {} (ID: {})", username, userId);
                            } else {
                                log.warn("Invalid JWT token in WebSocket connection");
                                throw new IllegalArgumentException("Invalid token");
                            }
                        } catch (Exception e) {
                            log.error("WebSocket authentication failed: {}", e.getMessage());
                            throw new IllegalArgumentException("Authentication failed");
                        }
                    } else {
                        log.warn("WebSocket connection attempt without JWT token");
                        throw new IllegalArgumentException("Missing authentication token");
                    }
                }

                return message;
            }
        });
    }
}