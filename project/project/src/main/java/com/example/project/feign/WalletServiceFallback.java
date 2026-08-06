package com.example.project.feign;

import com.example.project.wallet.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Fallback class for WalletServiceClient
 * Provides default responses when Wallet Service is unavailable
 */
@Component
@Slf4j
public class WalletServiceFallback implements WalletServiceClient {

    @Override
    public ResponseEntity<WalletResponseDTO> createWallet(WalletRequestDTO requestDTO) {
        log.warn("Wallet Service is unavailable - returning fallback response for createWallet");
        return ResponseEntity.status(503).build();
    }

    @Override
    public ResponseEntity<WalletResponseDTO> getWallet(Long walletId) {
        log.warn("Wallet Service is unavailable - returning fallback response for getWallet: {}", walletId);
        return ResponseEntity.status(503).build();
    }

    @Override
    public ResponseEntity<WalletResponseDTO> getWalletByUserId(Long userId) {
        log.warn("Wallet Service is unavailable - returning fallback response for getWalletByUserId: {}", userId);
        return ResponseEntity.status(503).build();
    }

    @Override
    public ResponseEntity<BalanceResponseDTO> getBalance(Long walletId) {
        log.warn("Wallet Service is unavailable - returning fallback response for getBalance: {}", walletId);
        return ResponseEntity.status(503).build();
    }

    @Override
    public ResponseEntity<TransactionResponseDTO> createPayment(Long walletId, BigDecimal amount, String referenceId) {
        log.warn("Wallet Service is unavailable - returning fallback response for createPayment: {}", walletId);
        return ResponseEntity.status(503).build();
    }

    @Override
    public ResponseEntity<TransactionResponseDTO> completePayment(Long transactionId) {
        log.warn("Wallet Service is unavailable - returning fallback response for completePayment: {}", transactionId);
        return ResponseEntity.status(503).build();
    }

    @Override
    public ResponseEntity<TransactionResponseDTO> refund(Long transactionId) {
        log.warn("Wallet Service is unavailable - returning fallback response for refund: {}", transactionId);
        return ResponseEntity.status(503).build();
    }
}

