package com.chatapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Typing indicator payload for WebSocket communication")
public class TypingIndicatorDTO {

    @Schema(
            description = "ID of the chat room where typing is occurring",
            example = "1"
    )
    private Long chatRoomId;

    @Schema(
            description = "ID of the user who is typing",
            example = "1"
    )
    private Long userId;

    @Schema(
            description = "Username of the user who is typing",
            example = "john_doe"
    )
    private String username;

    @Schema(
            description = "Whether the user is currently typing or has stopped",
            example = "true"
    )
    private boolean typing;
}
