package com.example.project.wallet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponseDTO {
    private Long walletId;
    private BigDecimal balance;
    private String currency;
    private LocalDateTime asOf;
}

