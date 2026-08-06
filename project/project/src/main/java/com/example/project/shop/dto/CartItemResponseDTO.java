package com.example.project.shop.dto;

import lombok.Data;

@Data
public class CartItemResponseDTO {
    private Long id;
    private Long productId;
    private Integer quantity;

    public CartItemResponseDTO(Long id, Long productId, Integer quantity) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
    }
}

