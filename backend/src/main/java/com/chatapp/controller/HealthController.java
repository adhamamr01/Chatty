package com.chatapp.controller;

import com.chatapp.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for monitoring application status.
 * Provides endpoints to verify the API is running.
 * These endpoints are publicly accessible (no authentication required).
 */
@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "Health check and status endpoints (public, no authentication required)")
public class HealthController {

    /**
     * Health check endpoint.
     *
     * @return ApiResponse with health status
     */
    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Returns the health status of the API. " +
                    "Use this endpoint to verify the API is running and accessible. " +
                    "This endpoint is publicly accessible and does not require authentication."
    )
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        log.debug("GET /api/health - Health check");

        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        health.put("service", "Chatty API");
        health.put("version", "1.0.0");

        return ResponseEntity.ok(ApiResponse.success("Service is healthy", health));
    }

    /**
     * Simple ping endpoint.
     *
     * @return Simple pong response
     */
    @GetMapping("/ping")
    @Operation(
            summary = "Ping",
            description = "Simple ping endpoint that returns 'pong'. " +
                    "Useful for quick connectivity tests. " +
                    "This endpoint is publicly accessible and does not require authentication."
    )
    public ResponseEntity<ApiResponse<String>> ping() {
        log.debug("GET /api/ping - Ping request");

        return ResponseEntity.ok(ApiResponse.success("pong"));
    }
}