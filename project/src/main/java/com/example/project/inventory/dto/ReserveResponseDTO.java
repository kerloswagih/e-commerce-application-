package com.example.project.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReserveResponseDTO {
    private boolean success;
    private Long productId;
    private Integer reservedQuantity;
    private String message;
}

