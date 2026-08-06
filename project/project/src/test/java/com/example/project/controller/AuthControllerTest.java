package com.example.project.controller;

import com.example.project.dto.LoginRequestDTO;
import com.example.project.dto.LoginResponseDTO;
import com.example.project.dto.UserRequestDTO;
import com.example.project.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void postRegisterReturnsCreated() {
        UserRequestDTO request = new UserRequestDTO(
                "newuser@example.com",
                "New",
                "User",
                "pass123",
                true
        );

        LoginResponseDTO responseBody = new LoginResponseDTO(
                "eyJhbGciOiJIUzI1NiJ9...",
                "Bearer",
                101L,
                "newuser@example.com",
                "New",
                "User",
                true
        );

        when(authService.register(any(UserRequestDTO.class))).thenReturn(responseBody);

        ResponseEntity<LoginResponseDTO> response = authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(101L, response.getBody().getId());
        assertEquals("newuser@example.com", response.getBody().getEmail());
        assertEquals("Bearer", response.getBody().getType());
    }

    @Test
    void postLoginReturnsOk() {
        LoginRequestDTO request = new LoginRequestDTO(
                "user@example.com",
                "pass123"
        );

        LoginResponseDTO responseBody = new LoginResponseDTO(
                "eyJhbGciOiJIUzI1NiJ9...",
                "Bearer",
                101L,
                "user@example.com",
                "John",
                "Doe",
                true
        );

        when(authService.login(any(LoginRequestDTO.class))).thenReturn(responseBody);

        ResponseEntity<LoginResponseDTO> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(101L, response.getBody().getId());
        assertEquals("user@example.com", response.getBody().getEmail());
        assertEquals("Bearer", response.getBody().getType());
    }
}

