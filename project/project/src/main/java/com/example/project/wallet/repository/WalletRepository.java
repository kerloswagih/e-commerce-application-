package com.example.project.wallet.repository;

import com.example.project.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Wallet entity operations.
 * Provides database CRUD and custom query methods for wallet management.
 */
@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    /**
     * Find a wallet by user ID (from auth service).
     * Since wallet is unique per user, returns Optional with at most one result.
     *
     * @param userId User ID from auth service
     * @return Optional containing wallet if exists
     */
    Optional<Wallet> findByUserId(Long userId);

    /**
     * Check if a wallet exists for a user.
     * Useful for validation before operations.
     *
     * @param userId User ID from auth service
     * @return true if wallet exists for user
     */
    boolean existsByUserId(Long userId);
}

