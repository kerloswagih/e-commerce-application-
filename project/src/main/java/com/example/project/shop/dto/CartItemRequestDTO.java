package com.example.project.shop.dto;

import lombok.Data;

@Data
public class CartItemRequestDTO {
    private Long id;
    private Long userId;
    private Long shopProductId;
    private Integer quantity;
}

