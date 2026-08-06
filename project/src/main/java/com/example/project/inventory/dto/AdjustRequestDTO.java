package com.example.project.inventory.dto;

import lombok.Data;

@Data
public class AdjustRequestDTO {
    private Long productId;
    private Integer delta; // positive or negative
    private String reason;
}

