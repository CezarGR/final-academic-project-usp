package com.tcc.transaction_api.domain.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Transaction domain model representing a financial transaction in the system.
 * Transactions can be deposits (C) or debits (D) from transfers.
 */
@Document(collection = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    private String id;

    /**
     * User ID associated with this transaction
     */
    private String userId;

    /**
     * Transaction type: C (Credit) or D (Debit)
     */
    private TransactionType type;

    /**
     * Transaction amount in cents
     */
    private int amount;

    /**
     * Transaction description (optional)
     */
    private String description;

    /**
     * Related user ID (for transfers: destination user for credit, origin user for debit)
     */
    private String relatedUserId;

    /**
     * Transfer ID (to group related transactions from the same transfer)
     */
    private String transferId;

    /**
     * Balance after this transaction in cents
     */
    private int balanceAfter;

    /**
     * Used special limit after this transaction in cents
     */
    private int usedSpecialLimitAfter;

    /**
     * Transaction timestamp
     */
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Transaction type enumeration
     */
    public enum TransactionType {
        /**
         * Credit transaction (deposit or transfer received)
         */
        C,
        /**
         * Debit transaction (transfer sent)
         */
        D
    }
}

