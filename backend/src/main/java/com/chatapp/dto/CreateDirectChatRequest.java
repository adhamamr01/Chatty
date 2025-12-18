package com.chatapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create or retrieve a direct chat with another user")
public class CreateDirectChatRequest {

    @NotNull(message = "Target user ID is required")
    @Schema(
            description = "ID of the user to start a direct chat with",
            example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long targetUserId;
}
