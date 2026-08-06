package com.example.project.wallet.repository;

import com.example.project.wallet.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Transaction entity operations.
 * Provides database CRUD and custom query methods for transaction history.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find all transactions for a specific wallet with pagination.
     * Useful for viewing transaction history.
     *
     * @param walletId Wallet ID
     * @param pageable Pagination parameters (page, size, sort)
     * @return Page of transactions for the wallet
     */
    Page<Transaction> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);

    /**
     * Find all transactions for a wallet by status (e.g., PENDING).
     * Useful for finding incomplete transactions.
     *
     * @param walletId Wallet ID
     * @param status Transaction status
     * @return Page of transactions matching criteria
     */
    Page<Transaction> findByWalletIdAndStatusOrderByCreatedAtDesc(
        Long walletId,
        Transaction.TransactionStatus status,
        Pageable pageable
    );

    /**
     * Count pending transactions for a wallet.
     * Useful for checking if there are incomplete operations.
     *
     * @param walletId Wallet ID
     * @return Number of pending transactions
     */
    long countByWalletIdAndStatus(Long walletId, Transaction.TransactionStatus status);
}

