package com.example.project.feign;

import com.example.project.inventory.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Fallback class for InventoryServiceClient
 * Provides default responses when Inventory Service is unavailable
 */
@Component
@Slf4j
public class InventoryServiceFallback implements InventoryServiceClient {

    @Override
    public ResponseEntity<ProductResponseDTO> getProduct(Long productId) {
        log.warn("Inventory Service is unavailable - returning fallback response for getProduct: {}", productId);
        return ResponseEntity.status(503).build();
    }

    @Override
    public ResponseEntity<InventoryLevelDTO> getInventoryLevel(Long productId) {
        log.warn("Inventory Service is unavailable - returning fallback response for getInventoryLevel: {}", productId);
        return ResponseEntity.status(503).build();
    }

    @Override
    public ResponseEntity<ReserveResponseDTO> reserve(Long productId, ReserveRequestDTO request) {
        log.warn("Inventory Service is unavailable - returning fallback response for reserve: {}", productId);
        return ResponseEntity.ok(new ReserveResponseDTO(false, productId, 0, "Inventory Service unavailable"));
    }

    @Override
    public ResponseEntity<ReserveResponseDTO> release(Long productId, ReserveRequestDTO request) {
        log.warn("Inventory Service is unavailable - returning fallback response for release: {}", productId);
        return ResponseEntity.ok(new ReserveResponseDTO(false, productId, 0, "Inventory Service unavailable"));
    }

    @Override
    public ResponseEntity<InventoryLevelDTO> adjust(Long productId, AdjustRequestDTO request) {
        log.warn("Inventory Service is unavailable - returning fallback response for adjust: {}", productId);
        return ResponseEntity.status(503).build();
    }
}

