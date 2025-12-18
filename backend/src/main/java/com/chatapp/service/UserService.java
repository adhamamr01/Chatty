package com.chatapp.service;

import com.chatapp.dto.UserDTO;
import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import com.chatapp.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing user-related operations.
 * Handles user profile retrieval, search, and user management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Gets the current authenticated user's profile.
     *
     * @return UserDTO with current user information
     * @throws IllegalStateException if no user is authenticated
     */
    @Transactional(readOnly = true)
    public UserDTO getCurrentUser() {
        CustomUserDetails userDetails = getCurrentUserDetails();

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        log.debug("Retrieved current user: {}", user.getUsername());
        return mapToDTO(user);
    }

    /**
     * Gets a user by ID.
     *
     * @param userId User ID
     * @return UserDTO with user information
     * @throws IllegalArgumentException if user not found
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        log.debug("Retrieved user by ID: {}", user.getUsername());
        return mapToDTO(user);
    }

    /**
     * Gets a user by username.
     *
     * @param username Username
     * @return UserDTO with user information
     * @throws IllegalArgumentException if user not found
     */
    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

        log.debug("Retrieved user by username: {}", username);
        return mapToDTO(user);
    }

    /**
     * Searches for users by username (case-insensitive, partial match).
     * Excludes the current user from results.
     *
     * @param query Search query (minimum 2 characters)
     * @return List of matching users
     * @throws IllegalArgumentException if query is too short
     */
    @Transactional(readOnly = true)
    public List<UserDTO> searchUsers(String query) {
        if (query == null || query.trim().length() < 2) {
            throw new IllegalArgumentException("Search query must be at least 2 characters");
        }

        Long currentUserId = getCurrentUserDetails().getId();

        log.debug("Searching users with query: {}", query);

        List<User> users = userRepository.findByUsernameContainingIgnoreCase(query.trim());

        // Filter out current user and map to DTOs
        return users.stream()
                .filter(user -> !user.getId().equals(currentUserId))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Gets all users except the current user.
     *
     * @return List of all users
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        Long currentUserId = getCurrentUserDetails().getId();

        log.debug("Retrieving all users");

        return userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(currentUserId))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Checks if a user exists by ID.
     *
     * @param userId User ID
     * @return true if user exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean userExists(Long userId) {
        return userRepository.existsById(userId);
    }

    /**
     * Gets the current authenticated user's details from SecurityContext.
     *
     * @return CustomUserDetails of authenticated user
     * @throws IllegalStateException if no user is authenticated
     */
    public CustomUserDetails getCurrentUserDetails() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        }

        throw new IllegalStateException("No authenticated user found");
    }

    /**
     * Gets the current authenticated user's ID.
     *
     * @return Current user ID
     */
    public Long getCurrentUserId() {
        return getCurrentUserDetails().getId();
    }

    /**
     * Maps User entity to UserDTO.
     *
     * @param user User entity
     * @return UserDTO
     */
    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .build();
    }
}