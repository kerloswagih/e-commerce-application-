package com.example.project.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class ProductResponseDTO {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private OffsetDateTime createdAt;
}

