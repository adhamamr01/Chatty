package com.chatapp.controller;

import com.chatapp.dto.ApiResponse;
import com.chatapp.dto.UserDTO;
import com.chatapp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for user-related operations.
 * Handles user profile retrieval and user search.
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Users", description = "Endpoints for user management and search")
public class UserController {

    private final UserService userService;

    /**
     * Gets the current authenticated user's profile.
     *
     * @return ApiResponse with current user information
     */
    @GetMapping("/me")
    @Operation(
            summary = "Get current user profile",
            description = "Returns the profile information of the currently authenticated user. " +
                    "Requires valid JWT token in Authorization header."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        log.info("GET /api/users/me - Getting current user profile");

        UserDTO user = userService.getCurrentUser();

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * Gets a user by ID.
     *
     * @param userId User ID
     * @return ApiResponse with user information
     */
    @GetMapping("/{userId}")
    @Operation(
            summary = "Get user by ID",
            description = "Returns user information for the specified user ID."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(
            @Parameter(description = "ID of the user to retrieve", required = true)
            @PathVariable Long userId) {
        log.info("GET /api/users/{} - Getting user by ID", userId);

        UserDTO user = userService.getUserById(userId);

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * Searches for users by username.
     *
     * @param q Search query (minimum 2 characters)
     * @return ApiResponse with list of matching users
     */
    @GetMapping("/search")
    @Operation(
            summary = "Search users",
            description = "Search for users by username (case-insensitive, partial match). " +
                    "Minimum query length is 2 characters. " +
                    "Current user is excluded from results."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Search completed successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid query (too short)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<List<UserDTO>>> searchUsers(
            @Parameter(
                    description = "Search query (minimum 2 characters)",
                    required = true,
                    example = "john"
            )
            @RequestParam String q) {
        log.info("GET /api/users/search?q={} - Searching users", q);

        List<UserDTO> users = userService.searchUsers(q);

        return ResponseEntity.ok(ApiResponse.success(users));
    }
}