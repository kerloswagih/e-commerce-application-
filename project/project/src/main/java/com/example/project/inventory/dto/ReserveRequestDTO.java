package com.example.project.inventory.dto;

import lombok.Data;

@Data
public class ReserveRequestDTO {
    private String requestId;
    private String orderId;
    private Long productId;
    private Integer quantity;
}

