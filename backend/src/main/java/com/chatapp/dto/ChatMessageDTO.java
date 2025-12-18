package com.chatapp.dto;

import com.chatapp.model.MessageStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Message payload for WebSocket communication (sending/receiving messages)")
public class ChatMessageDTO {

    @Schema(
            description = "ID of the chat room to send the message to",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long chatRoomId;

    @Schema(
            description = "Content of the message to send",
            example = "Hello, how are you?",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String content;

    @Schema(
            description = "Status of the message (set by server)",
            example = "SENT",
            allowableValues = {"SENT", "DELIVERED", "READ"}
    )
    private MessageStatus status;
}