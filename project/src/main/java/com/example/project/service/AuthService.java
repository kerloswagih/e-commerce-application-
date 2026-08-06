package com.example.project.service;

import com.example.project.dto.LoginRequestDTO;
import com.example.project.dto.LoginResponseDTO;
import com.example.project.dto.UserRequestDTO;
import com.example.project.dto.UserResponseDTO;
import com.example.project.entity.User;
import com.example.project.feign.WalletServiceClient;
import com.example.project.util.JwtUtil;
import com.example.project.wallet.dto.WalletRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service layer for authentication operations (login, registration)
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final WalletServiceClient walletServiceClient;

    /**
     * Register a new user
     */
    public LoginResponseDTO register(UserRequestDTO requestDTO) {
        UserResponseDTO userResponse = userService.createUser(requestDTO);

        WalletRequestDTO walletRequest = new WalletRequestDTO(userResponse.getId(), "USD");
        walletServiceClient.createWallet(walletRequest);

        String token = jwtUtil.generateToken(userResponse.getId(), userResponse.getEmail());

        return new LoginResponseDTO(
                token,
                "Bearer",
                userResponse.getId(),
                userResponse.getEmail(),
                userResponse.getFirstName(),
                userResponse.getLastName(),
                userResponse.getActive()
        );
    }

    /**
     * Login user and return JWT token
     */
    public LoginResponseDTO login(LoginRequestDTO requestDTO) {
        User user = userService.authenticateUser(requestDTO.getEmail(), requestDTO.getPassword());

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        return new LoginResponseDTO(
                token,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getActive()
        );
    }
}

