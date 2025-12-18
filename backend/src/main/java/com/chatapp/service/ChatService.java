package com.chatapp.service;

import com.chatapp.dto.ChatRoomDTO;
import com.chatapp.dto.MessageDTO;
import com.chatapp.dto.PageResponse;
import com.chatapp.dto.UserDTO;
import com.chatapp.model.*;
import com.chatapp.repository.ChatRoomMemberRepository;
import com.chatapp.repository.ChatRoomRepository;
import com.chatapp.repository.MessageRepository;
import com.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing chat rooms and messages.
 * Handles direct chat creation, message operations, and chat history.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * Creates or retrieves a direct chat room between current user and target user.
     *
     * @param targetUserId ID of the user to chat with
     * @return ChatRoomDTO with room information
     * @throws IllegalArgumentException if target user not found or trying to chat with self
     */
    @Transactional
    public ChatRoomDTO createOrGetDirectChat(Long targetUserId) {
        Long currentUserId = userService.getCurrentUserId();

        // Validate: Cannot create chat with self
        if (currentUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("Cannot create chat with yourself");
        }

        // Check if target user exists
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("Target user not found"));

        log.debug("Creating or getting direct chat between user {} and user {}", currentUserId, targetUserId);

        // Check if chat room already exists
        Optional<ChatRoom> existingRoom = chatRoomRepository.findDirectChatBetweenUsers(currentUserId, targetUserId);

        if (existingRoom.isPresent()) {
            log.debug("Direct chat already exists: {}", existingRoom.get().getId());
            return mapToDTO(existingRoom.get(), currentUserId);
        }

        // Create new chat room
        ChatRoom chatRoom = ChatRoom.builder().build();
        chatRoom = chatRoomRepository.save(chatRoom);

        // Add both users as members
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Current user not found"));

        ChatRoomMember member1 = ChatRoomMember.builder()
                .chatRoom(chatRoom)
                .user(currentUser)
                .build();

        ChatRoomMember member2 = ChatRoomMember.builder()
                .chatRoom(chatRoom)
                .user(targetUser)
                .build();

        chatRoomMemberRepository.save(member1);
        chatRoomMemberRepository.save(member2);

        log.info("Created new direct chat room: {}", chatRoom.getId());

        return mapToDTO(chatRoom, currentUserId);
    }

    /**
     * Gets all chat rooms for the current user.
     *
     * @return List of ChatRoomDTOs
     */
    @Transactional(readOnly = true)
    public List<ChatRoomDTO> getUserChatRooms() {
        Long currentUserId = userService.getCurrentUserId();

        log.debug("Retrieving chat rooms for user {}", currentUserId);

        List<ChatRoom> chatRooms = chatRoomRepository.findAllByUserId(currentUserId);

        return chatRooms.stream()
                .map(room -> mapToDTO(room, currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * Gets a chat room by ID.
     *
     * @param chatRoomId Chat room ID
     * @return ChatRoomDTO
     * @throws IllegalArgumentException if room not found or user not a member
     */
    @Transactional(readOnly = true)
    public ChatRoomDTO getChatRoom(Long chatRoomId) {
        Long currentUserId = userService.getCurrentUserId();

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found"));

        // Verify user is a member
        if (!isMemberOfChatRoom(currentUserId, chatRoomId)) {
            throw new IllegalArgumentException("You are not a member of this chat room");
        }

        log.debug("Retrieved chat room: {}", chatRoomId);

        return mapToDTO(chatRoom, currentUserId);
    }

    /**
     * Sends a message in a chat room.
     *
     * @param chatRoomId Chat room ID
     * @param content Message content
     * @return MessageDTO with sent message
     * @throws IllegalArgumentException if room not found or user not a member
     */
    @Transactional
    public MessageDTO sendMessage(Long chatRoomId, String content) {
        Long currentUserId = userService.getCurrentUserId();

        // Verify chat room exists
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found"));

        // Verify user is a member
        if (!isMemberOfChatRoom(currentUserId, chatRoomId)) {
            throw new IllegalArgumentException("You are not a member of this chat room");
        }

        // Get sender
        User sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Create message
        Message message = Message.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(content)
                .status(MessageStatus.SENT)
                .build();

        message = messageRepository.save(message);

        log.info("Message sent in chat room {}: {}", chatRoomId, message.getId());

        return mapToDTO(message);
    }

    /**
     * Gets messages for a chat room with pagination.
     * Messages are sorted by creation time in descending order (newest first).
     *
     * @param chatRoomId Chat room ID
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return PageResponse with messages
     * @throws IllegalArgumentException if room not found or user not a member
     */
    @Transactional(readOnly = true)
    public PageResponse<MessageDTO> getChatRoomMessages(Long chatRoomId, int page, int size) {
        Long currentUserId = userService.getCurrentUserId();

        // Verify user is a member
        if (!isMemberOfChatRoom(currentUserId, chatRoomId)) {
            throw new IllegalArgumentException("You are not a member of this chat room");
        }

        log.debug("Retrieving messages for chat room {} (page: {}, size: {})", chatRoomId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Message> messagePage = messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId, pageable);

        List<MessageDTO> messages = messagePage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return PageResponse.<MessageDTO>builder()
                .content(messages)
                .pageNumber(messagePage.getNumber())
                .pageSize(messagePage.getSize())
                .totalElements(messagePage.getTotalElements())
                .totalPages(messagePage.getTotalPages())
                .last(messagePage.isLast())
                .first(messagePage.isFirst())
                .empty(messagePage.isEmpty())
                .build();
    }

    /**
     * Marks a message as read.
     *
     * @param messageId Message ID
     * @throws IllegalArgumentException if message not found or user not authorized
     */
    @Transactional
    public void markMessageAsRead(Long messageId) {
        Long currentUserId = userService.getCurrentUserId();

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        // Only the recipient can mark message as read (not the sender)
        if (message.getSender().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("Cannot mark your own message as read");
        }

        // Verify user is member of the chat room
        if (!isMemberOfChatRoom(currentUserId, message.getChatRoom().getId())) {
            throw new IllegalArgumentException("You are not a member of this chat room");
        }

        message.setStatus(MessageStatus.READ);
        messageRepository.save(message);

        log.debug("Message {} marked as read by user {}", messageId, currentUserId);
    }

    /**
     * Marks all messages in a chat room as read.
     *
     * @param chatRoomId Chat room ID
     */
    @Transactional
    public void markAllMessagesAsRead(Long chatRoomId) {
        Long currentUserId = userService.getCurrentUserId();

        // Verify user is member of the chat room
        if (!isMemberOfChatRoom(currentUserId, chatRoomId)) {
            throw new IllegalArgumentException("You are not a member of this chat room");
        }

        messageRepository.updateMessageStatusForChatRoom(chatRoomId, currentUserId, MessageStatus.READ);

        log.debug("All messages in chat room {} marked as read by user {}", chatRoomId, currentUserId);
    }

    /**
     * Checks if a user is a member of a chat room.
     *
     * @param userId User ID
     * @param chatRoomId Chat room ID
     * @return true if user is a member, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isMemberOfChatRoom(Long userId, Long chatRoomId) {
        return chatRoomMemberRepository.existsByChatRoomIdAndUserId(chatRoomId, userId);
    }

    /**
     * Gets the other user in a direct chat (not the current user).
     *
     * @param chatRoom Chat room
     * @param currentUserId Current user ID
     * @return Other user
     */
    private User getOtherUser(ChatRoom chatRoom, Long currentUserId) {
        List<ChatRoomMember> members = chatRoomMemberRepository.findByChatRoomId(chatRoom.getId());

        return members.stream()
                .map(ChatRoomMember::getUser)
                .filter(user -> !user.getId().equals(currentUserId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets the last message in a chat room.
     *
     * @param chatRoomId Chat room ID
     * @return Last message or null if no messages
     */
    private Message getLastMessage(Long chatRoomId) {
        Pageable pageable = PageRequest.of(0, 1, Sort.by("createdAt").descending());
        Page<Message> page = messageRepository.findByChatRoomIdOrderByCreatedAtDesc(chatRoomId, pageable);

        return page.hasContent() ? page.getContent().get(0) : null;
    }

    /**
     * Maps ChatRoom entity to ChatRoomDTO.
     *
     * @param chatRoom Chat room entity
     * @param currentUserId Current user ID
     * @return ChatRoomDTO
     */
    private ChatRoomDTO mapToDTO(ChatRoom chatRoom, Long currentUserId) {
        User otherUser = getOtherUser(chatRoom, currentUserId);
        Message lastMessage = getLastMessage(chatRoom.getId());

        return ChatRoomDTO.builder()
                .id(chatRoom.getId())
                .otherUser(otherUser != null ? mapUserToDTO(otherUser) : null)
                .lastMessage(lastMessage != null ? mapToDTO(lastMessage) : null)
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }

    /**
     * Maps Message entity to MessageDTO.
     *
     * @param message Message entity
     * @return MessageDTO
     */
    private MessageDTO mapToDTO(Message message) {
        return MessageDTO.builder()
                .id(message.getId())
                .chatRoomId(message.getChatRoom().getId())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .content(message.getContent())
                .status(message.getStatus())
                .createdAt(message.getCreatedAt())
                .build();
    }

    /**
     * Maps User entity to UserDTO.
     *
     * @param user User entity
     * @return UserDTO
     */
    private UserDTO mapUserToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .build();
    }
}