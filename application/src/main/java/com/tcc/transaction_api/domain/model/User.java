package com.tcc.transaction_api.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User domain model representing a user in the system.
 * Each user has a balance, special limit, and transaction history.
 */
@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;

    /**
     * User's full name
     */
    private String name;

    /**
     * User's document (CPF, CNPJ, etc.)
     */
    private String document;

    /**
     * User's birth date
     */
    private LocalDate birthDate;

    /**
     * Current balance in cents
     */
    @Builder.Default
    private int balance = 0;

    /**
     * Special limit in cents (default: 1000 cents = 10.00)
     */
    @Builder.Default
    private int specialLimit = 1000;

    /**
     * Amount of special limit currently used in cents
     */
    @Builder.Default
    private int usedSpecialLimit = 0;

    /**
     * Creation timestamp
     */
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Last update timestamp
     */
    private LocalDateTime updatedAt;

    /**
     * Calculates the available balance including special limit
     * @return available balance in cents (balance + available special limit)
     */
    public int getAvailableBalance() {
        return balance + (specialLimit - usedSpecialLimit);
    }

    /**
     * Checks if user has used the special limit
     * @return true if usedSpecialLimit > 0
     */
    public boolean hasUsedSpecialLimit() {
        return usedSpecialLimit > 0;
    }
}

