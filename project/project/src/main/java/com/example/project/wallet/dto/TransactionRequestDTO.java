package com.example.project.wallet.dto;

import com.example.project.wallet.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDTO {
    private Long walletId;
    private BigDecimal amount;
    private Transaction.TransactionType type;
    private String referenceId;
    private String description;
}

