package com.example.project.wallet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Wallet entity representing a user's wallet account.
 * One-to-one relationship with user (identified by user_id from auth service).
 *
 * Fields:
 * - id: Unique wallet identifier
 * - user_id: Reference to user in auth service (not a foreign key to avoid tight coupling)
 * - balance: Current account balance
 * - currency: Currency code (e.g., USD, EUR)
 * - updated_at: Timestamp of last balance update
 */
@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User ID from auth service (project_db).
     * Not a foreign key to maintain microservice independence.
     * Each user has exactly one wallet (UNIQUE constraint in DB).
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    /**
     * Current account balance.
     * Precision: 15 digits total, 2 decimal places.
     * e.g., 9999999999999.99
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * Currency code (ISO 4217).
     * Default: USD
     * Examples: USD, EUR, GBP, INR, etc.
     */
    @Column(nullable = false, length = 3)
    private String currency = "USD";

    /**
     * Timestamp when wallet was last updated.
     * Auto-updated on transaction or balance change.
     */
    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt = LocalDateTime.now();


    /**
     * Updates the wallet's updated_at timestamp.
     * Called before persistence to track changes.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

