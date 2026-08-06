package com.example.project.feign;

import com.example.project.dto.UserResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Fallback class for UserServiceClient
 * Provides default responses when User Service is unavailable
 */
@Component
@Slf4j
public class UserServiceFallback implements UserServiceClient {

    @Override
    public ResponseEntity<UserResponseDTO> getUserById(Long id) {
        log.warn("User Service is unavailable - returning fallback response for getUserById: {}", id);
        return ResponseEntity.status(503).build();
    }

    @Override
    public ResponseEntity<UserResponseDTO> getUserByEmail(String email) {
        log.warn("User Service is unavailable - returning fallback response for getUserByEmail: {}", email);
        return ResponseEntity.status(503).build();
    }
}

