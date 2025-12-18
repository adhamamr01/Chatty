package com.chatapp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Detailed error response for validation and application errors.
 * Includes error code, message, and optional field-level errors.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Detailed error response for failed requests")
public class ErrorResponse {

    @Schema(
            description = "HTTP status code",
            example = "400"
    )
    private int status;

    @Schema(
            description = "Error type or code",
            example = "VALIDATION_ERROR"
    )
    private String error;

    @Schema(
            description = "Human-readable error message",
            example = "Validation failed for request"
    )
    private String message;

    @Schema(
            description = "API endpoint path where the error occurred",
            example = "/api/auth/register"
    )
    private String path;

    @Schema(
            description = "List of field-level validation errors"
    )
    private List<FieldError> fieldErrors;

    @Schema(
            description = "Timestamp when the error occurred",
            example = "2024-01-15T10:30:00"
    )
    private String timestamp;

    /**
     * Custom builder method to automatically format timestamp
     */
    public static class ErrorResponseBuilder {
        public ErrorResponseBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return this;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Field-level validation error")
    public static class FieldError {

        @Schema(
                description = "Name of the field that failed validation",
                example = "email"
        )
        private String field;

        @Schema(
                description = "Rejected value",
                example = "invalid-email"
        )
        private Object rejectedValue;

        @Schema(
                description = "Validation error message",
                example = "Email must be valid"
        )
        private String message;
    }
}
