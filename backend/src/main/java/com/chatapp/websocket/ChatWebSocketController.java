package com.chatapp.websocket;

import com.chatapp.dto.ChatMessageDTO;
import com.chatapp.dto.MessageDTO;
import com.chatapp.dto.ReadReceiptDTO;
import com.chatapp.dto.TypingIndicatorDTO;
import com.chatapp.service.ChatService;
import com.chatapp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket controller for handling real-time chat operations.
 * Processes incoming WebSocket messages and broadcasts to appropriate destinations.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UserService userService;

    /**
     * Handles incoming chat messages via WebSocket.
     * Saves the message and broadcasts it to the chat room.
     *
     * Destination: /app/chat.send
     *
     * @param chatMessage Message to send
     * @param principal Authenticated user
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDTO chatMessage, Principal principal) {
        try {
            Long userId = getUserIdFromPrincipal(principal);
            log.info("Received message from user {} for chat room {}", userId, chatMessage.getChatRoomId());

            // Save message using service
            MessageDTO savedMessage = chatService.sendMessage(
                    chatMessage.getChatRoomId(),
                    chatMessage.getContent()
            );

            // Broadcast to chat room topic
            messagingTemplate.convertAndSend(
                    "/topic/chat." + chatMessage.getChatRoomId(),
                    savedMessage
            );

            // Send to each member's personal queue
            // This ensures users receive messages even if not subscribed to topic
            sendToRoomMembers(chatMessage.getChatRoomId(), savedMessage);

            log.info("Message broadcasted: {}", savedMessage.getId());

        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage(), e);
            // Could send error back to user here
        }
    }

    /**
     * Handles typing indicator messages.
     * Broadcasts typing status to other chat room members.
     *
     * Destination: /app/chat.typing
     *
     * @param typingIndicator Typing indicator
     * @param principal Authenticated user
     */
    @MessageMapping("/chat.typing")
    public void handleTypingIndicator(@Payload TypingIndicatorDTO typingIndicator, Principal principal) {
        try {
            Long userId = getUserIdFromPrincipal(principal);
            log.debug("Typing indicator from user {} in chat room {}: {}",
                    userId, typingIndicator.getChatRoomId(), typingIndicator.isTyping());

            // Get username
            String username = userService.getUserById(userId).getUsername();

            // Set user info in typing indicator
            typingIndicator.setUserId(userId);
            typingIndicator.setUsername(username);

            // Broadcast to chat room (other members will filter out their own)
            messagingTemplate.convertAndSend(
                    "/topic/chat." + typingIndicator.getChatRoomId() + ".typing",
                    typingIndicator
            );

        } catch (Exception e) {
            log.error("Error handling typing indicator: {}", e.getMessage());
        }
    }

    /**
     * Handles read receipt messages.
     * Updates message status and notifies the sender.
     *
     * Destination: /app/chat.read
     *
     * @param readReceipt Read receipt
     * @param principal Authenticated user
     */
    @MessageMapping("/chat.read")
    public void handleReadReceipt(@Payload ReadReceiptDTO readReceipt, Principal principal) {
        try {
            Long userId = getUserIdFromPrincipal(principal);
            log.debug("Read receipt from user {} for chat room {}", userId, readReceipt.getChatRoomId());

            readReceipt.setUserId(userId);

            // Mark message(s) as read
            if (readReceipt.getMessageId() != null) {
                // Mark specific message as read
                chatService.markMessageAsRead(readReceipt.getMessageId());
            } else {
                // Mark all messages in chat room as read
                chatService.markAllMessagesAsRead(readReceipt.getChatRoomId());
            }

            // Broadcast read receipt to chat room
            messagingTemplate.convertAndSend(
                    "/topic/chat." + readReceipt.getChatRoomId() + ".read",
                    readReceipt
            );

            log.debug("Read receipt processed for chat room {}", readReceipt.getChatRoomId());

        } catch (Exception e) {
            log.error("Error handling read receipt: {}", e.getMessage());
        }
    }

    /**
     * Sends message to all members of a chat room via their personal queues.
     *
     * @param chatRoomId Chat room ID
     * @param message Message to send
     */
    private void sendToRoomMembers(Long chatRoomId, MessageDTO message) {
        try {
            // Get chat room to find members
            var chatRoom = chatService.getChatRoom(chatRoomId);

            // Send to other user's personal queue
            if (chatRoom.getOtherUser() != null) {
                messagingTemplate.convertAndSendToUser(
                        chatRoom.getOtherUser().getId().toString(),
                        "/queue/messages",
                        message
                );
            }
        } catch (Exception e) {
            log.error("Error sending to room members: {}", e.getMessage());
        }
    }

    /**
     * Extracts user ID from Principal.
     *
     * @param principal Principal object
     * @return User ID
     */
    private Long getUserIdFromPrincipal(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
            return (Long) auth.getPrincipal();
        }
        throw new IllegalStateException("Invalid principal type");
    }
}