package com.example.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for User creation/update requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private Boolean active;
}

