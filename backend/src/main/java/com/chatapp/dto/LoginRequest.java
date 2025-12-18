package com.chatapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for user authentication")
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @Schema(
            description = "Username of the account",
            example = "john_doe",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(
            description = "Password of the account",
            example = "securePassword123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;
}