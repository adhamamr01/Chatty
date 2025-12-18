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
 * Entity representing the many-to-many relationship between users and chat rooms.
 * Each record indicates a user's membership in a specific chat room.
 * For direct chats, each room has exactly 2 members.
 */
@Entity
@Table(name = "chat_room_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"chat_room_id", "user_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Junction entity linking users to chat rooms they are members of")
public class ChatRoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the membership record", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    @Schema(description = "The chat room this membership refers to")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "The user who is a member of this chat room")
    private User user;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @Schema(description = "Timestamp when the user joined this chat room", example = "2024-01-15T10:30:00")
    private LocalDateTime joinedAt;
}
