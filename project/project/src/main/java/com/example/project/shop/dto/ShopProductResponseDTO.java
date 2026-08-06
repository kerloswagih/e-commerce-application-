package com.example.project.shop.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ShopProductResponseDTO {
    private Long id;
    private Long inventoryProductId;
    private String title;
    private BigDecimal price;
    private Boolean isActive;

    public ShopProductResponseDTO(Long id, Long inventoryProductId, String title, BigDecimal price, Boolean isActive) {
        this.id = id;
        this.inventoryProductId = inventoryProductId;
        this.title = title;
        this.price = price;
        this.isActive = isActive;
    }
}

