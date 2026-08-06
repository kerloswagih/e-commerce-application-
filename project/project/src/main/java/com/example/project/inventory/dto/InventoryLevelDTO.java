package com.example.project.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class InventoryLevelDTO {
    private Long id;
    private Long productId;
    private Integer quantityAvailable;
    private Integer quantityReserved;
    private Integer reorderLevel;
    private OffsetDateTime lastUpdated;
}

