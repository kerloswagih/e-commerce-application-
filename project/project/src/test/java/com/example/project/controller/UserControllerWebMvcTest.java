package com.example.project.controller;

import com.example.project.dto.UserRequestDTO;
import com.example.project.dto.UserResponseDTO;
import com.example.project.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerWebMvcTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void postUserReturnsCreated() {
        UserRequestDTO request = new UserRequestDTO(
                "api.post@example.com",
                "Api",
                "Post",
                "pass123",
                true
        );

        UserResponseDTO responseBody = new UserResponseDTO(
                101L,
                "api.post@example.com",
                "Api",
                "Post",
                true,
                1L,
                1L
        );

        when(userService.createUser(any(UserRequestDTO.class))).thenReturn(responseBody);

        ResponseEntity<UserResponseDTO> response = userController.createUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(101L, response.getBody().getId());
        assertEquals("api.post@example.com", response.getBody().getEmail());
    }

    @Test
    void putUserReturnsUpdatedUser() {
        UserRequestDTO request = new UserRequestDTO(
                "ignored@example.com",
                "Updated",
                "Name",
                "ignored",
                false
        );

        UserResponseDTO responseBody = new UserResponseDTO(
                101L,
                "api.post@example.com",
                "Updated",
                "Name",
                false,
                1L,
                2L
        );

        when(userService.updateUser(eq(101L), any(UserRequestDTO.class))).thenReturn(responseBody);

        ResponseEntity<UserResponseDTO> response = userController.updateUser(101L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated", response.getBody().getFirstName());
        assertEquals(false, response.getBody().getActive());
    }

    @Test
    void deleteUserReturnsNoContent() {
        doNothing().when(userService).deleteUser(101L);

        ResponseEntity<Void> response = userController.deleteUser(101L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService).deleteUser(101L);
    }
}
