package com.example.project.service;

import com.example.project.dto.LoginRequestDTO;
import com.example.project.dto.LoginResponseDTO;
import com.example.project.dto.UserRequestDTO;
import com.example.project.dto.UserResponseDTO;
import com.example.project.entity.User;
import com.example.project.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerCreatesUserAndReturnsToken() {
        UserRequestDTO request = new UserRequestDTO(
                "new@example.com",
                "New",
                "User",
                "pass123",
                true
        );

        UserResponseDTO userResponse = new UserResponseDTO(
                101L,
                "new@example.com",
                "New",
                "User",
                true,
                1000L,
                1000L
        );

        when(userService.createUser(any(UserRequestDTO.class))).thenReturn(userResponse);
        when(jwtUtil.generateToken(101L, "new@example.com")).thenReturn("mock-token-123");

        LoginResponseDTO response = authService.register(request);

        assertEquals("mock-token-123", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(101L, response.getId());
        assertEquals("new@example.com", response.getEmail());
        assertEquals("New", response.getFirstName());
        assertEquals("User", response.getLastName());
    }

    @Test
    void loginAuthenticatesUserAndReturnsToken() {
        LoginRequestDTO request = new LoginRequestDTO(
                "user@example.com",
                "pass123"
        );

        User user = new User(
                101L,
                "user@example.com",
                "John",
                "Doe",
                "encoded-password",
                true,
                1000L,
                1000L
        );

        when(userService.authenticateUser("user@example.com", "pass123")).thenReturn(user);
        when(jwtUtil.generateToken(101L, "user@example.com")).thenReturn("mock-token-123");

        LoginResponseDTO response = authService.login(request);

        assertEquals("mock-token-123", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(101L, response.getId());
        assertEquals("user@example.com", response.getEmail());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
    }
}

