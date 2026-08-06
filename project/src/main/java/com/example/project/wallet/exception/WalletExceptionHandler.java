package com.example.project.wallet.exception;

import com.example.project.dto.ApiResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Global Exception Handler for Wallet Service
 * Provides consistent error responses across all endpoints
 */
@RestControllerAdvice
@Slf4j
public class WalletExceptionHandler {

    /**
     * Handle generic RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleRuntimeException(
            RuntimeException ex,
            WebRequest request) {

        log.error("RuntimeException occurred: {}", ex.getMessage(), ex);

        // Determine HTTP status based on exception message
        HttpStatus status = determineStatus(ex.getMessage());

        ApiResponseDTO<?> response = ApiResponseDTO.error(
                ex.getMessage(),
                status.value()
        );

        return new ResponseEntity<>(response, status);
    }

    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request) {

        log.error("IllegalArgumentException occurred: {}", ex.getMessage(), ex);

        ApiResponseDTO<?> response = ApiResponseDTO.error(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle IllegalStateException
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponseDTO<?>> handleIllegalStateException(
            IllegalStateException ex,
            WebRequest request) {

        log.error("IllegalStateException occurred: {}", ex.getMessage(), ex);

        ApiResponseDTO<?> response = ApiResponseDTO.error(
                ex.getMessage(),
                HttpStatus.CONFLICT.value()
        );

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    /**
     * Handle generic Exception (catch-all)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<?>> handleGenericException(
            Exception ex,
            WebRequest request) {

        log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);

        ApiResponseDTO<?> response = ApiResponseDTO.error(
                "An unexpected error occurred. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getClass().getSimpleName()
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Determine HTTP status based on error message
     */
    private HttpStatus determineStatus(String message) {
        if (message == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }

        if (message.contains("not found") || message.contains("not exist")) {
            return HttpStatus.NOT_FOUND;
        }

        if (message.contains("already exists")) {
            return HttpStatus.CONFLICT;
        }

        if (message.contains("Unable to verify") || message.contains("insufficient")) {
            return HttpStatus.BAD_REQUEST;
        }

        if (message.contains("Service unavailable")) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }

        return HttpStatus.BAD_REQUEST;
    }
}

