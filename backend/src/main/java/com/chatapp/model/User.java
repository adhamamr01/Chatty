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
 * Entity representing a user in the chat application.
 * Users can authenticate, send messages, and participate in chat rooms.
 */
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User entity representing a registered user in the system")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the user", example = "1")
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    @Schema(description = "Unique username for authentication", example = "john_doe")
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    @Schema(description = "Unique email address", example = "john.doe@example.com")
    private String email;

    @Column(nullable = false)
    @Schema(description = "Hashed password (never exposed in API responses)", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String passwordHash;

    @Column(length = 100)
    @Schema(description = "Display name shown to other users", example = "John Doe")
    private String displayName;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @Schema(description = "Timestamp when the user account was created", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
}
