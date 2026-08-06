package com.example.project.service;

import com.example.project.dto.UserRequestDTO;
import com.example.project.dto.UserResponseDTO;
import com.example.project.entity.User;
import com.example.project.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void updateUserUpdatesAllFields() {
        User existingUser = new User(
                4L,
                "old@example.com",
                "Old",
                "Name",
                "old-pass",
                false,
                1000L,
                1000L
        );

        UserRequestDTO request = new UserRequestDTO(
                "ahmed.updated@example.com",
                "Ahmed",
                "Updated",
                "pass123",
                true
        );

        when(userRepository.findById(4L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("ahmed.updated@example.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded-pass123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO response = userService.updateUser(4L, request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("ahmed.updated@example.com", savedUser.getEmail());
        assertEquals("Ahmed", savedUser.getFirstName());
        assertEquals("Updated", savedUser.getLastName());
        assertEquals("encoded-pass123", savedUser.getPassword());
        assertEquals(true, savedUser.getActive());
        assertEquals(4L, response.getId());
        assertEquals("ahmed.updated@example.com", response.getEmail());
        assertEquals("Ahmed", response.getFirstName());
        assertEquals("Updated", response.getLastName());
        assertEquals(true, response.getActive());
    }

    @Test
    void updateUserRejectsDuplicateEmail() {
        User existingUser = new User(
                4L,
                "old@example.com",
                "Old",
                "Name",
                "old-pass",
                true,
                1000L,
                1000L
        );

        UserRequestDTO request = new UserRequestDTO(
                "other@example.com",
                "Ahmed",
                "Updated",
                "pass123",
                true
        );

        when(userRepository.findById(4L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("other@example.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.updateUser(4L, request));

        assertEquals("User with email other@example.com already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}

