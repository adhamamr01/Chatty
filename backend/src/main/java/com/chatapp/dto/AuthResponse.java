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
@Schema(description = "Response containing authentication token and user information")
public class AuthResponse {

    @Schema(
            description = "JWT authentication token to be used in Authorization header",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String token;

    @Schema(
            description = "Type of token (always 'Bearer')",
            example = "Bearer",
            defaultValue = "Bearer"
    )
    private String type = "Bearer";

    @Schema(
            description = "Unique identifier of the authenticated user",
            example = "1"
    )
    private Long userId;

    @Schema(
            description = "Username of the authenticated user",
            example = "john_doe"
    )
    private String username;

    @Schema(
            description = "Email of the authenticated user",
            example = "john.doe@example.com"
    )
    private String email;

    @Schema(
            description = "Display name of the authenticated user",
            example = "John Doe"
    )
    private String displayName;
}
