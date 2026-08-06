package com.example.project.wallet.controller;

import com.example.project.wallet.dto.TransactionResponseDTO;
import com.example.project.wallet.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for transaction operations.
 * Endpoints for:
 * - Viewing transaction details
 * - Retrieving transaction history
 * - Filtering transactions by status
 *
 * Base path: /api/v1/transactions
 */
@RestController
@RequestMapping("/v1/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Get transaction details by ID.
     *
     * GET /api/v1/transactions/{transactionId}
     *
     * @param transactionId Transaction ID
     * @return TransactionResponseDTO with transaction details
     * @status 200 OK
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponseDTO> getTransaction(@PathVariable Long transactionId) {
        log.info("API: Getting transaction with id: {}", transactionId);

        TransactionResponseDTO response = transactionService.getTransaction(transactionId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get transaction history for a wallet.
     * Returns transactions in descending order (most recent first).
     * Supports pagination.
     *
     * GET /api/v1/transactions/wallet/{walletId}?page=0&size=10
     *
     * @param walletId Wallet ID
     * @param page Page number (0-indexed, default: 0)
     * @param size Page size (default: 10)
     * @return Page of TransactionResponseDTO with transaction history
     * @status 200 OK
     */
    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<Page<TransactionResponseDTO>> getWalletTransactionHistory(
        @PathVariable Long walletId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        log.info("API: Getting transaction history for walletId: {}, page: {}, size: {}",
            walletId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionResponseDTO> response = transactionService.getWalletTransactionHistory(walletId, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * Get pending transactions for a wallet.
     * Returns only PENDING transactions that are awaiting processing/approval.
     * Useful for monitoring incomplete operations.
     *
     * GET /api/v1/transactions/wallet/{walletId}/pending?page=0&size=10
     *
     * @param walletId Wallet ID
     * @param page Page number (0-indexed, default: 0)
     * @param size Page size (default: 10)
     * @return Page of TransactionResponseDTO with pending transactions
     * @status 200 OK
     */
    @GetMapping("/wallet/{walletId}/pending")
    public ResponseEntity<Page<TransactionResponseDTO>> getPendingTransactions(
        @PathVariable Long walletId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        log.info("API: Getting pending transactions for walletId: {}, page: {}, size: {}",
            walletId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionResponseDTO> response = transactionService.getPendingTransactions(walletId, pageable);

        return ResponseEntity.ok(response);
    }

    /**
     * Count pending transactions for a wallet.
     * Useful for validation and status checks.
     *
     * GET /api/v1/transactions/wallet/{walletId}/pending-count
     *
     * @param walletId Wallet ID
     * @return Number of pending transactions
     * @status 200 OK
     */
    @GetMapping("/wallet/{walletId}/pending-count")
    public ResponseEntity<Long> countPendingTransactions(@PathVariable Long walletId) {
        log.info("API: Counting pending transactions for walletId: {}", walletId);

        long count = transactionService.countPendingTransactions(walletId);
        return ResponseEntity.ok(count);
    }
}

