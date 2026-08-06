package com.example.project.inventory.controller;

import com.example.project.inventory.dto.*;
import com.example.project.inventory.service.InventoryService;
import com.example.project.exception.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/v1/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/products")
    public ResponseEntity<?> createProduct(@RequestBody ProductRequestDTO request) {
        try {
            log.info("API: Create product name={}", request.getName());
            ProductResponseDTO response = inventoryService.createProduct(request);
            return ResponseEntity.created(URI.create("/v1/inventory/products/" + response.getId())).body(response);
        } catch (RuntimeException ex) {
            log.error("Error creating product: {}", ex.getMessage(), ex);
            ApiResponse errorResponse = new ApiResponse(
                    null,
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST.value()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductResponseDTO> getProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getProduct(productId));
    }

    @GetMapping("/products/{productId}/level")
    public ResponseEntity<InventoryLevelDTO> getInventoryLevel(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getInventoryLevel(productId));
    }

    @PostMapping("/products/{productId}/reserve")
    public ResponseEntity<ReserveResponseDTO> reserve(@PathVariable Long productId, @RequestBody ReserveRequestDTO request) {
        request.setProductId(productId);
        return ResponseEntity.ok(inventoryService.reserve(request));
    }

    @PostMapping("/products/{productId}/release")
    public ResponseEntity<ReserveResponseDTO> release(@PathVariable Long productId, @RequestBody ReserveRequestDTO request) {
        request.setProductId(productId);
        return ResponseEntity.ok(inventoryService.release(request));
    }

    @PostMapping("/products/{productId}/adjust")
    public ResponseEntity<InventoryLevelDTO> adjust(@PathVariable Long productId, @RequestBody AdjustRequestDTO request) {
        request.setProductId(productId);
        return ResponseEntity.ok(inventoryService.adjust(request));
    }
}
