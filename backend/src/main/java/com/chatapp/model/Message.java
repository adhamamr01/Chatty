package com.chatapp.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a message sent in a chat room.
 * Messages are sent by a user in the context of a specific chat room.
 * Each message has a status (SENT, DELIVERED, READ) for tracking delivery.
 */
@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_messages_chat_room", columnList = "chat_room_id,created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Message entity representing a text message in a chat room")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the message", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    @Schema(description = "The chat room this message belongs to")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    @Schema(description = "The user who sent this message")
    private User sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    @Schema(description = "Content of the message", example = "Hello, how are you?")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    @Schema(
            description = "Delivery status of the message",
            example = "READ",
            allowableValues = {"SENT", "DELIVERED", "READ"}
    )
    private MessageStatus status = MessageStatus.SENT;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @Schema(description = "Timestamp when the message was sent", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
}
