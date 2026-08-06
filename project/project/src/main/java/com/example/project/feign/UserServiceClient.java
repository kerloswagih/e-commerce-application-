package com.example.project.feign;

import com.example.project.dto.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign Client for User Service (Auth Service)
 * Used by Wallet and Shop services to call User Service endpoints
 * Uses Eureka service discovery with client-side load balancing
 */
@FeignClient(
        name = "auth-service",
        fallback = UserServiceFallback.class
)
public interface UserServiceClient {

    /**
     * Get user by ID
     * GET /api/v1/users/{id}
     */
    @GetMapping("/api/v1/users/{id}")
    ResponseEntity<UserResponseDTO> getUserById(@PathVariable("id") Long id);

    /**
     * Get user by email
     * GET /api/v1/users/email/{email}
     */
    @GetMapping("/api/v1/users/email/{email}")
    ResponseEntity<UserResponseDTO> getUserByEmail(@PathVariable("email") String email);
}

