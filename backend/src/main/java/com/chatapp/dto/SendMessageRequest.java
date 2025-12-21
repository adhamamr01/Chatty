package com.chatapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to send a message")
public class SendMessageRequest {

    @NotBlank(message = "Message content cannot be empty")
    @Size(max = 5000, message = "Message content cannot exceed 5000 characters")
    @Schema(
            description = "Content of the message",
            example = "Hello! How are you?"
    )
    private String content;
}