package com.tcc.transaction_api.domain.util;

import com.tcc.transaction_api.domain.exception.FinancialRuleViolationException;
import com.tcc.transaction_api.domain.exception.InsufficientBalanceException;
import com.tcc.transaction_api.domain.model.User;

/**
 * Utility class for validating financial operations.
 * Ensures all financial rules are respected before executing transactions.
 * All monetary values are in cents.
 */
public class FinancialValidator {

    /**
     * Validates if a user has sufficient balance (including special limit) for a transaction
     * @param user the user to validate
     * @param amount the transaction amount in cents
     * @throws InsufficientBalanceException if user doesn't have sufficient balance
     */
    public static void validateSufficientBalance(User user, int amount) {
        if (!BalanceCalculator.hasSufficientBalance(user, amount)) {
            int available = BalanceCalculator.calculateAvailableBalance(user);
            throw new InsufficientBalanceException(
                String.format("Insufficient balance. Available: %d cents, Required: %d cents", available, amount)
            );
        }
    }

    /**
     * Validates that a transaction won't violate financial rules.
     * A transaction should never leave the balance below the allowed limit.
     * @param user the user to validate for
     * @param amount the transaction amount in cents
     * @throws FinancialRuleViolationException if the transaction would violate financial rules
     */
    public static void validateFinancialRules(User user, int amount) {
        // Calculate what the balance would be after the debit
        int balanceAfter = LimitApplier.calculateBalanceAfterDebit(user, amount);
        int usedLimitAfter = LimitApplier.calculateUsedLimitAfterDebit(user, amount);

        // The balance should never go below the negative of the special limit
        // In other words, the used special limit should never exceed the special limit
        if (usedLimitAfter > user.getSpecialLimit()) {
            throw new FinancialRuleViolationException(
                String.format("Transaction would exceed special limit. Current limit: %d cents, Would use: %d cents",
                    user.getSpecialLimit(), usedLimitAfter)
            );
        }

        // Additional validation: balance should not be negative beyond the special limit
        if (balanceAfter < 0) {
            throw new FinancialRuleViolationException(
                String.format("Transaction would result in invalid balance: %d cents", balanceAfter)
            );
        }
    }

    /**
     * Validates the amount is positive
     * @param amount the amount to validate
     * @throws IllegalArgumentException if amount is not positive
     */
    public static void validatePositiveAmount(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
    }
}

