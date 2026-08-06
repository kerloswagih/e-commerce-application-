package com.example.project.wallet.dto;

import com.example.project.wallet.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDTO {
    private Long id;
    private Long walletId;
    private BigDecimal amount;
    private Transaction.TransactionType type;
    private Transaction.TransactionStatus status;
    private String referenceId;
    private String description;
    private LocalDateTime createdAt;
}

