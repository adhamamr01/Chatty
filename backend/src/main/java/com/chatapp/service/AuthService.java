package com.chatapp.service;

import com.chatapp.dto.AuthResponse;
import com.chatapp.dto.LoginRequest;
import com.chatapp.dto.RegisterRequest;
import com.chatapp.dto.UserDTO;
import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import com.chatapp.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for handling user authentication operations including registration and login.
 * Manages JWT token generation and user credential validation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user in the system.
     *
     * @param request Registration request containing user details
     * @return AuthResponse with JWT token and user information
     * @throws IllegalArgumentException if username or email already exists
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.debug("Attempting to register user: {}", request.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: Username '{}' already exists", request.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email '{}' already exists", request.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName() != null
                        ? request.getDisplayName()
                        : request.getUsername())
                .build();

        // Save user
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());

        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser.getUsername(), savedUser.getId());

        // Build response
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .displayName(savedUser.getDisplayName())
                .build();
    }

    /**
     * Authenticates a user and generates a JWT token.
     *
     * @param request Login request containing username and password
     * @return AuthResponse with JWT token and user information
     * @throws AuthenticationException if credentials are invalid
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.debug("Attempting to authenticate user: {}", request.getUsername());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Get user details
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            log.info("User authenticated successfully: {}", user.getUsername());

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getUsername(), user.getId());

            // Build response
            return AuthResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .displayName(user.getDisplayName())
                    .build();

        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {}", request.getUsername());
            throw new IllegalArgumentException("Invalid username or password");
        }
    }

    /**
     * Validates a JWT token.
     *
     * @param token JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            return jwtUtil.validateToken(token);
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts user ID from JWT token.
     *
     * @param token JWT token
     * @return User ID from token
     */
    public Long getUserIdFromToken(String token) {
        return jwtUtil.extractUserId(token);
    }

    /**
     * Extracts username from JWT token.
     *
     * @param token JWT token
     * @return Username from token
     */
    public String getUsernameFromToken(String token) {
        return jwtUtil.extractUsername(token);
    }
}