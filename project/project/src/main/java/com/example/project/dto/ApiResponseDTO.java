package com.example.project.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standardized API Response DTO for all endpoints
 * Provides consistent response format across the application
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDTO<T> {
    private T data;
    private String message;
    private int status;
    private String error;
    private long timestamp;

    /**
     * Success response
     */
    public static <T> ApiResponseDTO<T> success(T data, String message, int status) {
        return ApiResponseDTO.<T>builder()
                .data(data)
                .message(message)
                .status(status)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * Error response
     */
    public static <T> ApiResponseDTO<T> error(String message, int status, String error) {
        return ApiResponseDTO.<T>builder()
                .data(null)
                .message(message)
                .status(status)
                .error(error)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * Error response without error details
     */
    public static <T> ApiResponseDTO<T> error(String message, int status) {
        return ApiResponseDTO.<T>builder()
                .data(null)
                .message(message)
                .status(status)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}

