package com.chatapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Chat room information with the other participant and last message")
public class ChatRoomDTO {

    @Schema(
            description = "Unique identifier of the chat room",
            example = "1"
    )
    private Long id;

    @Schema(
            description = "Information about the other user in this direct chat"
    )
    private UserDTO otherUser;

    @Schema(
            description = "The most recent message in this chat room (null if no messages)"
    )
    private MessageDTO lastMessage;

    @Schema(
            description = "Timestamp when the chat room was created",
            example = "2024-01-15T10:30:00"
    )
    private LocalDateTime createdAt;
}
