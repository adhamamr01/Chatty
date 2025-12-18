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
@Schema(description = "User information transfer object")
public class UserDTO {

    @Schema(
            description = "Unique identifier of the user",
            example = "1"
    )
    private Long id;

    @Schema(
            description = "Username of the user",
            example = "john_doe"
    )
    private String username;

    @Schema(
            description = "Email address of the user",
            example = "john.doe@example.com"
    )
    private String email;

    @Schema(
            description = "Display name of the user",
            example = "John Doe"
    )
    private String displayName;
}
