package com.example.project.feign;

import com.example.project.inventory.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Feign Client for Inventory Service
 * Used by Shop Service to call Inventory Service endpoints
 * Uses Eureka service discovery with client-side load balancing
 */
@FeignClient(
        name = "inventory-service",
        fallback = InventoryServiceFallback.class
)
public interface InventoryServiceClient {

    /**
     * Get product by ID
     * GET /api/v1/inventory/products/{productId}
     */
    @GetMapping("/api/v1/inventory/products/{productId}")
    ResponseEntity<ProductResponseDTO> getProduct(@PathVariable("productId") Long productId);

    /**
     * Get inventory level for a product
     * GET /api/v1/inventory/products/{productId}/level
     */
    @GetMapping("/api/v1/inventory/products/{productId}/level")
    ResponseEntity<InventoryLevelDTO> getInventoryLevel(@PathVariable("productId") Long productId);

    /**
     * Reserve quantity for a product
     * POST /api/v1/inventory/products/{productId}/reserve
     */
    @PostMapping("/api/v1/inventory/products/{productId}/reserve")
    ResponseEntity<ReserveResponseDTO> reserve(
            @PathVariable("productId") Long productId,
            @RequestBody ReserveRequestDTO request
    );

    /**
     * Release reserved quantity for a product
     * POST /api/v1/inventory/products/{productId}/release
     */
    @PostMapping("/api/v1/inventory/products/{productId}/release")
    ResponseEntity<ReserveResponseDTO> release(
            @PathVariable("productId") Long productId,
            @RequestBody ReserveRequestDTO request
    );

    /**
     * Adjust inventory level
     * POST /api/v1/inventory/products/{productId}/adjust
     */
    @PostMapping("/api/v1/inventory/products/{productId}/adjust")
    ResponseEntity<InventoryLevelDTO> adjust(
            @PathVariable("productId") Long productId,
            @RequestBody AdjustRequestDTO request
    );
}

