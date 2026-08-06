package com.example.project.wallet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction entity representing a wallet transaction.
 * Records all balance changes: deposits, withdrawals, payments, refunds.
 *
 * Provides audit trail for all wallet operations.
 *
 * Fields:
 * - id: Unique transaction identifier
 * - wallet_id: FK to wallets table
 * - amount: Transaction value
 * - type: DEPOSIT, WITHDRAWAL, PAYMENT, REFUND
 * - status: PENDING, COMPLETED, FAILED
 * - reference_id: External reference (order ID, etc.)
 * - created_at: Transaction timestamp
 */
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Foreign key to wallets table.
     * Identifies which wallet this transaction belongs to.
     * Using EAGER fetch to avoid lazy loading issues in service layer.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    /**
     * Transaction amount.
     * Precision: 15 digits total, 2 decimal places.
     */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * Transaction type enumeration.
     * - DEPOSIT: Money added to wallet (from user/payment)
     * - WITHDRAWAL: Money removed from wallet (cashout)
     * - PAYMENT: Money deducted for purchase/service
     * - REFUND: Money returned after cancellation
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    /**
     * Transaction status enumeration.
     * - PENDING: Awaiting processing/approval
     * - COMPLETED: Successfully processed
     * - FAILED: Transaction failed/rejected
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.PENDING;

    /**
     * External reference identifier.
     * Examples:
     * - Order ID from shop service
     * - Payment gateway reference
     * - Refund reference
     * Useful for reconciliation and tracking across microservices.
     */
    @Column(name = "reference_id", length = 100)
    private String referenceId;

    /**
     * Description of the transaction.
     * e.g., "Payment for Order #12345", "Wallet Deposit"
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * Timestamp when transaction was created.
     */
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Enum for transaction types.
     */
    public enum TransactionType {
        DEPOSIT,    // Money added
        WITHDRAWAL, // Money removed
        PAYMENT,    // Payment deducted
        REFUND      // Money returned
    }

    /**
     * Enum for transaction statuses.
     */
    public enum TransactionStatus {
        PENDING,    // Awaiting processing
        COMPLETED,  // Successfully processed
        FAILED      // Failed/rejected
    }
}


