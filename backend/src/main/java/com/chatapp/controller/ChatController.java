package com.chatapp.controller;

import com.chatapp.dto.*;
import com.chatapp.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for chat and messaging operations.
 * Handles chat room management and message operations.
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Chat", description = "Endpoints for chat room and message management")
public class ChatController {

    private final ChatService chatService;

    /**
     * Gets all chat rooms for the current user.
     *
     * @return ApiResponse with list of chat rooms
     */
    @GetMapping("/chats")
    @Operation(
            summary = "Get all user's chats",
            description = "Returns all chat rooms where the current user is a member. " +
                    "Each chat room includes information about the other participant and the last message."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Chat rooms retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<List<ChatRoomDTO>>> getUserChats() {
        log.info("GET /api/chats - Getting user's chat rooms");

        List<ChatRoomDTO> chatRooms = chatService.getUserChatRooms();

        return ResponseEntity.ok(ApiResponse.success(chatRooms));
    }

    /**
     * Creates or retrieves a direct chat with another user.
     *
     * @param request Request containing target user ID
     * @return ApiResponse with chat room information
     */
    @PostMapping("/chats/direct")
    @Operation(
            summary = "Create or get direct chat",
            description = "Creates a new direct (1-on-1) chat with the specified user, " +
                    "or returns the existing chat if it already exists. " +
                    "Cannot create a chat with yourself."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Chat room already exists",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "New chat room created",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request (e.g., trying to chat with yourself)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Target user not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<ChatRoomDTO>> createOrGetDirectChat(
            @Valid @RequestBody CreateDirectChatRequest request) {
        log.info("POST /api/chats/direct - Creating/getting direct chat with user {}", request.getTargetUserId());

        ChatRoomDTO chatRoom = chatService.createOrGetDirectChat(request.getTargetUserId());

        // Return 201 for new chat, 200 for existing
        boolean isNewChat = chatRoom.getLastMessage() == null;
        HttpStatus status = isNewChat ? HttpStatus.CREATED : HttpStatus.OK;
        String message = isNewChat ? "Chat room created successfully" : "Chat room already exists";

        return ResponseEntity
                .status(status)
                .body(ApiResponse.success(message, chatRoom));
    }

    /**
     * Gets a specific chat room by ID.
     *
     * @param chatRoomId Chat room ID
     * @return ApiResponse with chat room information
     */
    @GetMapping("/chats/{chatRoomId}")
    @Operation(
            summary = "Get chat room by ID",
            description = "Returns information about a specific chat room. " +
                    "User must be a member of the chat room."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Chat room retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Not a member of this chat room",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Chat room not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<ChatRoomDTO>> getChatRoom(
            @Parameter(description = "ID of the chat room", required = true)
            @PathVariable Long chatRoomId) {
        log.info("GET /api/chats/{} - Getting chat room", chatRoomId);

        ChatRoomDTO chatRoom = chatService.getChatRoom(chatRoomId);

        return ResponseEntity.ok(ApiResponse.success(chatRoom));
    }

    /**
     * Gets messages for a chat room with pagination.
     *
     * @param chatRoomId Chat room ID
     * @param page Page number (0-indexed)
     * @param size Page size (default: 20, max: 100)
     * @return ApiResponse with paginated messages
     */
    @GetMapping("/chats/{chatRoomId}/messages")
    @Operation(
            summary = "Get chat messages",
            description = "Returns paginated messages for a chat room, ordered by creation time (newest first). " +
                    "User must be a member of the chat room."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Messages retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Not a member of this chat room",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Chat room not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<PageResponse<MessageDTO>>> getChatMessages(
            @Parameter(description = "ID of the chat room", required = true)
            @PathVariable Long chatRoomId,
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max: 100)", example = "20")
            @RequestParam(defaultValue = "20") int size) {

        // Validate page size
        if (size > 100) {
            size = 100;
        }

        log.info("GET /api/chats/{}/messages?page={}&size={} - Getting messages", chatRoomId, page, size);

        PageResponse<MessageDTO> messages = chatService.getChatRoomMessages(chatRoomId, page, size);

        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    /**
     * Marks a specific message as read.
     *
     * @param messageId Message ID
     * @return ApiResponse with success message
     */
    @PutMapping("/messages/{messageId}/read")
    @Operation(
            summary = "Mark message as read",
            description = "Marks a specific message as read. " +
                    "Only the recipient can mark a message as read (not the sender)."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Message marked as read",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Cannot mark own message as read",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Not a member of this chat room",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Message not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<Void>> markMessageAsRead(
            @Parameter(description = "ID of the message to mark as read", required = true)
            @PathVariable Long messageId) {
        log.info("PUT /api/messages/{}/read - Marking message as read", messageId);

        chatService.markMessageAsRead(messageId);

        return ResponseEntity.ok(ApiResponse.success("Message marked as read"));
    }

    /**
     * Marks all messages in a chat room as read.
     *
     * @param chatRoomId Chat room ID
     * @return ApiResponse with success message
     */
    @PutMapping("/chats/{chatRoomId}/read")
    @Operation(
            summary = "Mark all messages as read",
            description = "Marks all unread messages in the chat room as read. " +
                    "Only marks messages that were sent by other users."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "All messages marked as read",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Not a member of this chat room",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Chat room not found",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Invalid or missing token",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<Void>> markAllMessagesAsRead(
            @Parameter(description = "ID of the chat room", required = true)
            @PathVariable Long chatRoomId) {
        log.info("PUT /api/chats/{}/read - Marking all messages as read", chatRoomId);

        chatService.markAllMessagesAsRead(chatRoomId);

        return ResponseEntity.ok(ApiResponse.success("All messages marked as read"));
    }
}