package com.example.project.wallet.controller;

import com.example.project.wallet.dto.*;
import com.example.project.wallet.service.WalletService;
import com.example.project.exception.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for wallet operations.
 * Endpoints for:
 * - Creating wallets for new users
 * - Viewing wallet information and balance
 * - Performing wallet operations (deposit, withdrawal, payment, refund)
 *
 * Base path: /api/v1/wallets
 */
@RestController
@RequestMapping({"/v1/wallets", "/v1/wallet"})
@RequiredArgsConstructor
@Slf4j
public class WalletController {

    private final WalletService walletService;

    /**
     * Create a new wallet for a user.
     * Called when a new user registers in the auth service.
     *
     * POST /api/v1/wallets
     *
     * @param requestDTO Wallet creation request (userId, currency)
     * @return WalletResponseDTO with created wallet
     * @status 201 Created
     */
    @PostMapping
    public ResponseEntity<?> createWallet(@RequestBody WalletRequestDTO requestDTO) {
        log.info("API: Creating wallet for userId: {}", requestDTO.getUserId());

        try {
            WalletResponseDTO response = walletService.createWallet(
                requestDTO.getUserId(),
                requestDTO.getCurrency()
            );

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            log.error("Error creating wallet: {}", ex.getMessage(), ex);
            ApiResponse errorResponse = new ApiResponse(
                    null,
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST.value()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Get wallet details by wallet ID.
     *
     * GET /api/v1/wallets/{walletId}
     *
     * @param walletId Wallet ID
     * @return WalletResponseDTO with wallet details
     * @status 200 OK
     */
    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponseDTO> getWallet(@PathVariable Long walletId) {
        log.info("API: Getting wallet with id: {}", walletId);

        WalletResponseDTO response = walletService.getWallet(walletId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get wallet by user ID.
     * Returns the wallet associated with a specific user.
     *
     * GET /api/v1/wallets/user/{userId}
     *
     * @param userId User ID from auth service
     * @return WalletResponseDTO with wallet details
     * @status 200 OK
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getWalletByUserId(@PathVariable Long userId) {
        try {
            log.info("API: Getting wallet for userId: {}", userId);
            WalletResponseDTO response = walletService.getWalletByUserId(userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            log.error("Error getting wallet: {}", ex.getMessage(), ex);
            ApiResponse errorResponse = new ApiResponse(
                    null,
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST.value()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Check wallet balance.
     * Returns current balance and currency.
     *
     * GET /api/v1/wallets/{walletId}/balance
     *
     * @param walletId Wallet ID
     * @return BalanceResponseDTO with current balance
     * @status 200 OK
     */
    @GetMapping("/{walletId}/balance")
    public ResponseEntity<BalanceResponseDTO> getBalance(@PathVariable Long walletId) {
        log.info("API: Checking balance for walletId: {}", walletId);

        BalanceResponseDTO response = walletService.getBalance(walletId);
        return ResponseEntity.ok(response);
    }

    /**
     * Deposit money into wallet.
     * Adds funds to wallet and records deposit transaction.
     *
     * POST /api/v1/wallets/deposit
     *
     * @param depositRequest Deposit details (walletId, amount, referenceId)
     * @return TransactionResponseDTO with deposit transaction
     * @status 200 OK
     */
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody DepositRequestDTO depositRequest) {
        try {
            log.info("API: Deposit request for walletId: {}, amount: {}",
                depositRequest.getWalletId(), depositRequest.getAmount());

            TransactionResponseDTO response = walletService.deposit(depositRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            log.error("Error processing deposit: {}", ex.getMessage(), ex);
            ApiResponse errorResponse = new ApiResponse(
                    null,
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST.value()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Withdraw money from wallet.
     * Removes funds from wallet and records withdrawal transaction.
     * Validates sufficient balance before processing.
     *
     * POST /api/v1/wallets/withdraw
     *
     * @param withdrawalRequest Withdrawal details (walletId, amount, description)
     * @return TransactionResponseDTO with withdrawal transaction
     * @status 200 OK
     * @throws RuntimeException if insufficient balance
     */
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody WithdrawalRequestDTO withdrawalRequest) {
        try {
            log.info("API: Withdrawal request for walletId: {}, amount: {}",
                withdrawalRequest.getWalletId(), withdrawalRequest.getAmount());

            TransactionResponseDTO response = walletService.withdraw(withdrawalRequest);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            log.error("Error processing withdrawal: {}", ex.getMessage(), ex);
            ApiResponse errorResponse = new ApiResponse(
                    null,
                    ex.getMessage(),
                    HttpStatus.BAD_REQUEST.value()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Create a payment transaction.
     * Initial status: PENDING (waiting for order confirmation from shop service).
     * Shop service will call completePayment or fail the payment.
     *
     * POST /api/v1/wallets/{walletId}/payment
     *
     * @param walletId Wallet ID
     * @param amount Payment amount
     * @param referenceId Order ID or payment reference (query parameter)
     * @return TransactionResponseDTO with PENDING payment transaction
     * @status 200 OK
     */
    @PostMapping("/{walletId}/payment")
    public ResponseEntity<TransactionResponseDTO> createPayment(
        @PathVariable Long walletId,
        @RequestParam java.math.BigDecimal amount,
        @RequestParam String referenceId
    ) {
        log.info("API: Creating payment for walletId: {}, amount: {}, referenceId: {}",
            walletId, amount, referenceId);

        TransactionResponseDTO response = walletService.createPayment(walletId, amount, referenceId);
        return ResponseEntity.ok(response);
    }

    /**
     * Complete a pending payment transaction.
     * Called by shop service when order is confirmed.
     * Deducts payment amount from wallet balance.
     *
     * PUT /api/v1/wallets/payment/{transactionId}/complete
     *
     * @param transactionId Payment transaction ID
     * @return TransactionResponseDTO with COMPLETED payment transaction
     * @status 200 OK
     * @throws RuntimeException if payment not found, not pending, or insufficient balance
     */
    @PutMapping("/payment/{transactionId}/complete")
    public ResponseEntity<TransactionResponseDTO> completePayment(@PathVariable Long transactionId) {
        log.info("API: Completing payment transaction: {}", transactionId);

        TransactionResponseDTO response = walletService.completePayment(transactionId);
        return ResponseEntity.ok(response);
    }

    /**
     * Refund a completed payment.
     * Called by shop service when order is cancelled.
     * Returns payment amount to wallet and records refund transaction.
     *
     * POST /api/v1/wallets/payment/{transactionId}/refund
     *
     * @param transactionId Original payment transaction ID
     * @return TransactionResponseDTO with COMPLETED refund transaction
     * @status 200 OK
     * @throws RuntimeException if payment not found or not completed
     */
    @PostMapping("/payment/{transactionId}/refund")
    public ResponseEntity<TransactionResponseDTO> refund(@PathVariable Long transactionId) {
        log.info("API: Refunding payment transaction: {}", transactionId);

        TransactionResponseDTO response = walletService.refund(transactionId);
        return ResponseEntity.ok(response);
    }
}

