package com.example.project.inventory.service;

import com.example.project.inventory.dto.*;
import com.example.project.inventory.entity.InventoryLevel;
import com.example.project.inventory.entity.Product;
import com.example.project.inventory.repository.InventoryLevelRepository;
import com.example.project.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryLevelRepository inventoryLevelRepository;

    public ProductResponseDTO createProduct(ProductRequestDTO request) {
        log.info("Creating product with name: {}, quantity: {}", request.getName(), request.getQuantity());

        // Auto-generate SKU if not provided
        String sku = request.getSku();
        if (sku == null || sku.trim().isEmpty()) {
            sku = "SKU-" + System.currentTimeMillis();
            log.info("Auto-generated SKU: {}", sku);
        }

        if (productRepository.existsBySku(sku)) {
            throw new RuntimeException("Product with SKU already exists: " + sku);
        }

        Product p = new Product();
        p.setSku(sku);
        p.setName(request.getName());
        p.setDescription(request.getDescription() != null ? request.getDescription() : "");

        Product saved = productRepository.save(p);

        // create inventory level record with provided quantity
        InventoryLevel level = new InventoryLevel();
        level.setProduct(saved);
        level.setQuantityAvailable(request.getQuantity() != null ? request.getQuantity() : 0);
        level.setQuantityReserved(0);
        level.setReorderLevel(10);
        level.setLastUpdated(OffsetDateTime.now());
        inventoryLevelRepository.save(level);

        log.info("Product created successfully: id={}, sku={}, quantity={}", saved.getId(), sku, level.getQuantityAvailable());

        return new ProductResponseDTO(saved.getId(), saved.getSku(), saved.getName(), saved.getDescription(), saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO getProduct(Long productId) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        return new ProductResponseDTO(p.getId(), p.getSku(), p.getName(), p.getDescription(), p.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public InventoryLevelDTO getInventoryLevel(Long productId) {
        InventoryLevel level = inventoryLevelRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory level not found for product: " + productId));

        return new InventoryLevelDTO(level.getId(), level.getProduct().getId(), level.getQuantityAvailable(), level.getQuantityReserved(), level.getReorderLevel(), level.getLastUpdated());
    }

    /**
     * Reserve quantity for an order. Uses optimistic locking via @Version on InventoryLevel.
     */
    public ReserveResponseDTO reserve(ReserveRequestDTO request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RuntimeException("Invalid quantity for reservation");
        }

        InventoryLevel level = inventoryLevelRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Inventory level not found for product: " + request.getProductId()));

        log.info("Attempting to reserve {} units of product {} (available={})", request.getQuantity(), request.getProductId(), level.getQuantityAvailable());

        if (level.getQuantityAvailable() < request.getQuantity()) {
            return new ReserveResponseDTO(false, request.getProductId(), 0, "Insufficient stock");
        }

        level.setQuantityAvailable(level.getQuantityAvailable() - request.getQuantity());
        level.setQuantityReserved(level.getQuantityReserved() + request.getQuantity());
        level.setLastUpdated(OffsetDateTime.now());
        inventoryLevelRepository.save(level);

        return new ReserveResponseDTO(true, request.getProductId(), request.getQuantity(), "Reserved");
    }

    public ReserveResponseDTO release(ReserveRequestDTO request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RuntimeException("Invalid quantity for release");
        }

        InventoryLevel level = inventoryLevelRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Inventory level not found for product: " + request.getProductId()));

        int reserved = level.getQuantityReserved() != null ? level.getQuantityReserved() : 0;
        int toRelease = Math.min(request.getQuantity(), reserved);

        level.setQuantityReserved(reserved - toRelease);
        level.setQuantityAvailable(level.getQuantityAvailable() + toRelease);
        level.setLastUpdated(OffsetDateTime.now());
        inventoryLevelRepository.save(level);

        return new ReserveResponseDTO(true, request.getProductId(), toRelease, "Released");
    }

    public InventoryLevelDTO adjust(AdjustRequestDTO request) {
        if (request.getDelta() == null) {
            throw new RuntimeException("Delta required for adjust");
        }

        InventoryLevel level = inventoryLevelRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Inventory level not found for product: " + request.getProductId()));

        int available = level.getQuantityAvailable() != null ? level.getQuantityAvailable() : 0;
        int newAvailable = available + request.getDelta();
        if (newAvailable < 0) {
            throw new RuntimeException("Adjustment would make available quantity negative");
        }

        level.setQuantityAvailable(newAvailable);
        level.setLastUpdated(OffsetDateTime.now());
        inventoryLevelRepository.save(level);

        return new InventoryLevelDTO(level.getId(), level.getProduct().getId(), level.getQuantityAvailable(), level.getQuantityReserved(), level.getReorderLevel(), level.getLastUpdated());
    }
}

