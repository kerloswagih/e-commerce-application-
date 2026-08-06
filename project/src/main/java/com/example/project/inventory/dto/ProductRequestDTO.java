package com.example.project.inventory.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequestDTO {
    private String sku;
    private String name;
    private String description;
    private Integer quantity;
    private BigDecimal price;
}

