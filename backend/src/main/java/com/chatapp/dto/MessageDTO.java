package com.chatapp.dto;

import com.chatapp.model.MessageStatus;
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
@Schema(description = "Message information transfer object")
public class MessageDTO {

    @Schema(
            description = "Unique identifier of the message",
            example = "1"
    )
    private Long id;

    @Schema(
            description = "ID of the chat room this message belongs to",
            example = "1"
    )
    private Long chatRoomId;

    @Schema(
            description = "ID of the user who sent the message",
            example = "1"
    )
    private Long senderId;

    @Schema(
            description = "Username of the user who sent the message",
            example = "john_doe"
    )
    private String senderUsername;

    @Schema(
            description = "Content of the message",
            example = "Hello, how are you?"
    )
    private String content;

    @Schema(
            description = "Delivery status of the message",
            example = "READ",
            allowableValues = {"SENT", "DELIVERED", "READ"}
    )
    private MessageStatus status;

    @Schema(
            description = "Timestamp when the message was created",
            example = "2024-01-15T10:30:00"
    )
    private LocalDateTime createdAt;
}
