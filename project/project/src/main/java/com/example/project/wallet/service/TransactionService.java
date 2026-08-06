package com.example.project.wallet.service;

import com.example.project.wallet.dto.TransactionRequestDTO;
import com.example.project.wallet.dto.TransactionResponseDTO;
import com.example.project.wallet.entity.Wallet;
import com.example.project.wallet.entity.Transaction;
import com.example.project.wallet.repository.TransactionRepository;
import com.example.project.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service layer for transaction operations.
 * Manages transaction creation, retrieval, and history tracking.
 *
 * Responsibilities:
 * - Create transaction records
 * - Retrieve transaction details
 * - Query transaction history
 * - Track transaction status changes
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    /**
     * Create a new transaction record.
     * Called by WalletService when balance changes occur.
     *
     * @param requestDTO Transaction details
     * @param status Initial transaction status
     * @return TransactionResponseDTO with created transaction
     * @throws RuntimeException if wallet not found
     */
    public TransactionResponseDTO createTransaction(TransactionRequestDTO requestDTO,
                                                     Transaction.TransactionStatus status) {
        log.info("Creating transaction: type={}, amount={}, status={}",
            requestDTO.getType(), requestDTO.getAmount(), status);

        // Get wallet
        Wallet wallet = walletRepository.findById(requestDTO.getWalletId())
            .orElseThrow(() -> new RuntimeException("Wallet not found: " + requestDTO.getWalletId()));

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setAmount(requestDTO.getAmount());
        transaction.setType(requestDTO.getType());
        transaction.setStatus(status);
        transaction.setReferenceId(requestDTO.getReferenceId());
        transaction.setDescription(requestDTO.getDescription());
        transaction.setCreatedAt(LocalDateTime.now());

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created with id: {}", savedTransaction.getId());

        return mapToResponseDTO(savedTransaction);
    }

    /**
     * Get transaction by ID.
     *
     * @param transactionId Transaction ID
     * @return TransactionResponseDTO
     * @throws RuntimeException if transaction not found
     */
    @Transactional(readOnly = true)
    public TransactionResponseDTO getTransaction(Long transactionId) {
        log.info("Retrieving transaction with id: {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));

        return mapToResponseDTO(transaction);
    }

    /**
     * Get transaction entity (internal use).
     * Used by WalletService to access entity for updates.
     *
     * @param transactionId Transaction ID
     * @return Transaction entity
     * @throws RuntimeException if transaction not found
     */
    protected Transaction getTransactionEntity(Long transactionId) {
        return transactionRepository.findById(transactionId)
            .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
    }

    /**
     * Get transaction history for a wallet with pagination.
     * Returns most recent transactions first.
     *
     * @param walletId Wallet ID
     * @param pageable Pagination parameters
     * @return Page of transactions
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponseDTO> getWalletTransactionHistory(Long walletId, Pageable pageable) {
        log.info("Retrieving transaction history for walletId: {}", walletId);

        // Verify wallet exists
        if (!walletRepository.existsById(walletId)) {
            throw new RuntimeException("Wallet not found: " + walletId);
        }

        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId, pageable)
            .map(this::mapToResponseDTO);
    }

    /**
     * Get pending transactions for a wallet.
     * Useful for finding incomplete operations that need attention.
     *
     * @param walletId Wallet ID
     * @param pageable Pagination parameters
     * @return Page of pending transactions
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponseDTO> getPendingTransactions(Long walletId, Pageable pageable) {
        log.info("Retrieving pending transactions for walletId: {}", walletId);

        if (!walletRepository.existsById(walletId)) {
            throw new RuntimeException("Wallet not found: " + walletId);
        }

        return transactionRepository.findByWalletIdAndStatusOrderByCreatedAtDesc(
            walletId,
            Transaction.TransactionStatus.PENDING,
            pageable
        ).map(this::mapToResponseDTO);
    }

    /**
     * Update transaction status.
     * Called when transaction status changes (e.g., PENDING -> COMPLETED).
     *
     * @param transaction Transaction entity to update
     * @return Updated TransactionResponseDTO
     */
    protected TransactionResponseDTO updateTransaction(Transaction transaction) {
        log.info("Updating transaction {} to status: {}", transaction.getId(), transaction.getStatus());

        Transaction updated = transactionRepository.save(transaction);
        return mapToResponseDTO(updated);
    }

    /**
     * Count pending transactions for a wallet.
     * Useful for validation (e.g., preventing multiple simultaneous payments).
     *
     * @param walletId Wallet ID
     * @return Number of pending transactions
     */
    @Transactional(readOnly = true)
    public long countPendingTransactions(Long walletId) {
        return transactionRepository.countByWalletIdAndStatus(
            walletId,
            Transaction.TransactionStatus.PENDING
        );
    }

    /**
     * Utility method to convert Transaction entity to ResponseDTO.
     */
    protected TransactionResponseDTO mapToResponseDTO(Transaction transaction) {
        return new TransactionResponseDTO(
            transaction.getId(),
            transaction.getWallet().getId(),
            transaction.getAmount(),
            transaction.getType(),
            transaction.getStatus(),
            transaction.getReferenceId(),
            transaction.getDescription(),
            transaction.getCreatedAt()
        );
    }
}

