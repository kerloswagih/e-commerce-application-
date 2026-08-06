package com.example.project.feign;

import com.example.project.wallet.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Feign Client for Wallet Service
 * Used by Shop Service to call Wallet Service endpoints for payment operations
 * Uses Eureka service discovery with client-side load balancing
 */
@FeignClient(
        name = "wallet-service",
        fallback = WalletServiceFallback.class
)
public interface WalletServiceClient {

    @PostMapping("/api/v1/wallets")
    ResponseEntity<WalletResponseDTO> createWallet(@RequestBody WalletRequestDTO requestDTO);

    /**
     * Get wallet details by wallet ID
     * GET /api/v1/wallets/{walletId}
     */
    @GetMapping("/api/v1/wallets/{walletId}")
    ResponseEntity<WalletResponseDTO> getWallet(@PathVariable("walletId") Long walletId);

    /**
     * Get wallet by user ID
     * GET /api/v1/wallets/user/{userId}
     */
    @GetMapping("/api/v1/wallets/user/{userId}")
    ResponseEntity<WalletResponseDTO> getWalletByUserId(@PathVariable("userId") Long userId);

    /**
     * Check wallet balance
     * GET /api/v1/wallets/{walletId}/balance
     */
    @GetMapping("/api/v1/wallets/{walletId}/balance")
    ResponseEntity<BalanceResponseDTO> getBalance(@PathVariable("walletId") Long walletId);

    /**
     * Create a payment transaction
     * POST /api/v1/wallets/{walletId}/payment
     */
    @PostMapping("/api/v1/wallets/{walletId}/payment")
    ResponseEntity<TransactionResponseDTO> createPayment(
            @PathVariable("walletId") Long walletId,
            @RequestParam BigDecimal amount,
            @RequestParam String referenceId
    );

    /**
     * Complete a pending payment transaction
     * PUT /api/v1/wallets/payment/{transactionId}/complete
     */
    @PutMapping("/api/v1/wallets/payment/{transactionId}/complete")
    ResponseEntity<TransactionResponseDTO> completePayment(@PathVariable("transactionId") Long transactionId);

    /**
     * Refund a completed payment
     * POST /api/v1/wallets/payment/{transactionId}/refund
     */
    @PostMapping("/api/v1/wallets/payment/{transactionId}/refund")
    ResponseEntity<TransactionResponseDTO> refund(@PathVariable("transactionId") Long transactionId);
}

