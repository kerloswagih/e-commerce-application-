package com.example.project.shop.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ShopProductRequestDTO {
    private Long inventoryProductId;
    private String title;
    private BigDecimal price;
}

