package com.example.project.shop.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CheckoutResponseDTO {
    private Long orderId;
    private String status;
    private BigDecimal totalAmount;

    public CheckoutResponseDTO(Long orderId, String status, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.status = status;
        this.totalAmount = totalAmount;
    }
}

