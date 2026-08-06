package com.example.project.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard API Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private Object data;
    private String message;
    private int status;

    public ApiResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.data = null;
    }
}

