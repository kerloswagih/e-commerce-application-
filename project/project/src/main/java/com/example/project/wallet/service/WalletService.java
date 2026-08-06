package com.example.project.wallet.service;

import com.example.project.wallet.dto.*;
import com.example.project.wallet.entity.Wallet;
import com.example.project.wallet.entity.Transaction;
import com.example.project.wallet.repository.WalletRepository;
import com.example.project.feign.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service layer for wallet operations.
 * Manages wallet creation, balance operations, and transaction coordination.
 *
 * Responsibilities:
 * - Create wallets for new users
 * - Retrieve wallet information
 * - Process balance updates
 * - Coordinate with TransactionService for recording operations
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionService transactionService;
    private final UserServiceClient userServiceClient;

    /**
     * Create a new wallet for a user.
     * Called when user registers in the auth service.
     *
     * @param userId User ID from auth service
     * @param currency Currency for wallet (default: USD)
     * @return WalletResponseDTO with created wallet details
     * @throws RuntimeException if wallet already exists for user
     */
    public WalletResponseDTO createWallet(Long userId, String currency) {
        log.info("Creating wallet for userId: {}, currency: {}", userId, currency);

        // Check if wallet already exists
        if (walletRepository.existsByUserId(userId)) {
            log.warn("Wallet already exists for userId: {}", userId);
            throw new RuntimeException("Wallet already exists for this user");
        }

        // Create new wallet with zero balance
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCurrency(currency != null ? currency : "USD");
        wallet.setUpdatedAt(LocalDateTime.now());

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Wallet created successfully with id: {}", savedWallet.getId());

        return mapToResponseDTO(savedWallet);
    }

    /**
     * Get wallet by ID.
     *
     * @param walletId Wallet ID
     * @return WalletResponseDTO
     * @throws RuntimeException if wallet not found
     */
    public WalletResponseDTO getWallet(Long walletId) {
        log.info("Retrieving wallet with id: {}", walletId);

        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + walletId));

        return mapToResponseDTO(wallet);
    }

    /**
     * Get wallet by user ID.
     * Since wallet is unique per user, returns single wallet.
     *
     * @param userId User ID from auth service
     * @return WalletResponseDTO
     * @throws RuntimeException if wallet not found
     */
    public WalletResponseDTO getWalletByUserId(Long userId) {
        log.info("Retrieving wallet for userId: {}", userId);

        Wallet wallet = walletRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + userId));

        return mapToResponseDTO(wallet);
    }

    /**
     * Get current balance of a wallet.
     *
     * @param walletId Wallet ID
     * @return BalanceResponseDTO with current balance
     */
    @Transactional(readOnly = true)
    public BalanceResponseDTO getBalance(Long walletId) {
        log.info("Checking balance for walletId: {}", walletId);

        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Wallet not found with id: " + walletId));

        return new BalanceResponseDTO(
            wallet.getId(),
            wallet.getBalance(),
            wallet.getCurrency(),
            LocalDateTime.now()
        );
    }

    /**
     * Process a deposit transaction.
     * Adds money to wallet balance and records transaction.
     *
     * @param depositRequest Deposit details
     * @return TransactionResponseDTO with transaction details
     * @throws RuntimeException if wallet not found or invalid amount
     */
    public TransactionResponseDTO deposit(DepositRequestDTO depositRequest) {
        log.info("Processing deposit for walletId: {}, amount: {}",
            depositRequest.getWalletId(), depositRequest.getAmount());

        // Validate amount
        if (depositRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Deposit amount must be greater than zero");
        }

        // Get wallet
        Wallet wallet = walletRepository.findById(depositRequest.getWalletId())
            .orElseThrow(() -> new RuntimeException("Wallet not found"));

        // Update balance
        wallet.setBalance(wallet.getBalance().add(depositRequest.getAmount()));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        log.info("Deposit completed. New balance: {}", wallet.getBalance());

        // Create transaction record
        TransactionRequestDTO transactionRequest = new TransactionRequestDTO();
        transactionRequest.setWalletId(depositRequest.getWalletId());
        transactionRequest.setAmount(depositRequest.getAmount());
        transactionRequest.setType(Transaction.TransactionType.DEPOSIT);
        transactionRequest.setReferenceId(depositRequest.getReferenceId());
        transactionRequest.setDescription("Wallet Deposit");

        return transactionService.createTransaction(transactionRequest, Transaction.TransactionStatus.COMPLETED);
    }

    /**
     * Process a withdrawal transaction.
     * Removes money from wallet balance and records transaction.
     *
     * @param withdrawalRequest Withdrawal details
     * @return TransactionResponseDTO with transaction details
     * @throws RuntimeException if wallet not found, invalid amount, or insufficient balance
     */
    public TransactionResponseDTO withdraw(WithdrawalRequestDTO withdrawalRequest) {
        log.info("Processing withdrawal for walletId: {}, amount: {}",
            withdrawalRequest.getWalletId(), withdrawalRequest.getAmount());

        // Validate amount
        if (withdrawalRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Withdrawal amount must be greater than zero");
        }

        // Get wallet
        Wallet wallet = walletRepository.findById(withdrawalRequest.getWalletId())
            .orElseThrow(() -> new RuntimeException("Wallet not found"));

        // Check sufficient balance
        if (wallet.getBalance().compareTo(withdrawalRequest.getAmount()) < 0) {
            log.warn("Insufficient balance for withdrawal. Current: {}, Requested: {}",
                wallet.getBalance(), withdrawalRequest.getAmount());
            throw new RuntimeException("Insufficient balance for withdrawal");
        }

        // Update balance
        wallet.setBalance(wallet.getBalance().subtract(withdrawalRequest.getAmount()));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        log.info("Withdrawal completed. New balance: {}", wallet.getBalance());

        // Create transaction record
        TransactionRequestDTO transactionRequest = new TransactionRequestDTO();
        transactionRequest.setWalletId(withdrawalRequest.getWalletId());
        transactionRequest.setAmount(withdrawalRequest.getAmount());
        transactionRequest.setType(Transaction.TransactionType.WITHDRAWAL);
        transactionRequest.setDescription(withdrawalRequest.getDescription());

        return transactionService.createTransaction(transactionRequest, Transaction.TransactionStatus.COMPLETED);
    }

    /**
     * Process a payment transaction.
     * Deducts payment amount from wallet (payment for purchase/service).
     * Initially created as PENDING, can be marked as COMPLETED/FAILED by external service.
     *
     * @param walletId Wallet ID
     * @param amount Payment amount
     * @param referenceId External reference (order ID, etc.)
     * @return TransactionResponseDTO with transaction details (PENDING status)
     */
    public TransactionResponseDTO createPayment(Long walletId, BigDecimal amount, String referenceId) {
        log.info("Creating payment transaction for walletId: {}, amount: {}, referenceId: {}",
            walletId, amount, referenceId);

        // Validate amount
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Payment amount must be greater than zero");
        }

        // Check wallet exists
        if (!walletRepository.existsById(walletId)) {
            throw new RuntimeException("Wallet not found");
        }

        // Create transaction record (PENDING - waiting for order confirmation)
        TransactionRequestDTO transactionRequest = new TransactionRequestDTO();
        transactionRequest.setWalletId(walletId);
        transactionRequest.setAmount(amount);
        transactionRequest.setType(Transaction.TransactionType.PAYMENT);
        transactionRequest.setReferenceId(referenceId);
        transactionRequest.setDescription("Payment for order: " + referenceId);

        return transactionService.createTransaction(transactionRequest, Transaction.TransactionStatus.PENDING);
    }

    /**
     * Complete a pending payment transaction.
     * Called when order is confirmed in shop service.
     * Deducts amount from wallet balance.
     *
     * @param transactionId Transaction ID
     * @return Updated TransactionResponseDTO with COMPLETED status
     * @throws RuntimeException if transaction not found, already completed, or insufficient balance
     */
    public TransactionResponseDTO completePayment(Long transactionId) {
        log.info("Completing payment transaction: {}", transactionId);

        Transaction transaction = transactionService.getTransactionEntity(transactionId);

        if (!transaction.getType().equals(Transaction.TransactionType.PAYMENT)) {
            throw new RuntimeException("Transaction is not a payment");
        }

        if (!transaction.getStatus().equals(Transaction.TransactionStatus.PENDING)) {
            throw new RuntimeException("Payment is already " + transaction.getStatus());
        }

        Wallet wallet = transaction.getWallet();

        // Check sufficient balance
        if (wallet.getBalance().compareTo(transaction.getAmount()) < 0) {
            log.warn("Insufficient balance for payment completion. Current: {}, Required: {}",
                wallet.getBalance(), transaction.getAmount());

            // Mark transaction as failed
            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transactionService.updateTransaction(transaction);

            throw new RuntimeException("Insufficient balance for payment");
        }

        // Deduct amount and complete
        wallet.setBalance(wallet.getBalance().subtract(transaction.getAmount()));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        // Mark transaction as completed
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transactionService.updateTransaction(transaction);

        log.info("Payment completed. New balance: {}", wallet.getBalance());

        return transactionService.mapToResponseDTO(transaction);
    }

    /**
     * Refund a completed payment transaction.
     * Called when order is cancelled or payment needs to be reversed.
     * Returns amount to wallet balance and creates REFUND transaction record.
     *
     * @param paymentTransactionId Original payment transaction ID
     * @return TransactionResponseDTO for the new refund transaction
     * @throws RuntimeException if transaction not found or not a completed payment
     */
    public TransactionResponseDTO refund(Long paymentTransactionId) {
        log.info("Processing refund for transaction: {}", paymentTransactionId);

        Transaction paymentTransaction = transactionService.getTransactionEntity(paymentTransactionId);

        if (!paymentTransaction.getType().equals(Transaction.TransactionType.PAYMENT)) {
            throw new RuntimeException("Can only refund payment transactions");
        }

        if (!paymentTransaction.getStatus().equals(Transaction.TransactionStatus.COMPLETED)) {
            throw new RuntimeException("Can only refund completed payments");
        }

        Wallet wallet = paymentTransaction.getWallet();

        // Add amount back to wallet
        wallet.setBalance(wallet.getBalance().add(paymentTransaction.getAmount()));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        log.info("Refund processed. Amount added: {}, New balance: {}",
            paymentTransaction.getAmount(), wallet.getBalance());

        // Create refund transaction record
        TransactionRequestDTO refundRequest = new TransactionRequestDTO();
        refundRequest.setWalletId(wallet.getId());
        refundRequest.setAmount(paymentTransaction.getAmount());
        refundRequest.setType(Transaction.TransactionType.REFUND);
        refundRequest.setReferenceId(paymentTransaction.getReferenceId());
        refundRequest.setDescription("Refund for payment: " + paymentTransaction.getReferenceId());

        return transactionService.createTransaction(refundRequest, Transaction.TransactionStatus.COMPLETED);
    }

    /**
     * Utility method to convert Wallet entity to ResponseDTO.
     */
    private WalletResponseDTO mapToResponseDTO(Wallet wallet) {
        return new WalletResponseDTO(
            wallet.getId(),
            wallet.getUserId(),
            wallet.getBalance(),
            wallet.getCurrency(),
            wallet.getUpdatedAt()
        );
    }
}

