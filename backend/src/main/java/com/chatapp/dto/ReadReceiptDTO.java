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
@Schema(description = "Read receipt payload for WebSocket communication")
public class ReadReceiptDTO {

    @Schema(
            description = "ID of the chat room containing the messages",
            example = "1"
    )
    private Long chatRoomId;

    @Schema(
            description = "ID of the specific message being marked as read (optional, null means all messages)",
            example = "42"
    )
    private Long messageId;

    @Schema(
            description = "ID of the user who read the message(s)",
            example = "1"
    )
    private Long userId;
}
