package com.tcc.transaction_api.domain.util;

import com.tcc.transaction_api.domain.model.User;

/**
 * Utility class for calculating user balances.
 * All monetary values are in cents.
 */
public class BalanceCalculator {

    /**
     * Calculates the available balance for a user (balance + available special limit)
     * @param user the user to calculate balance for
     * @return available balance in cents
     */
    public static int calculateAvailableBalance(User user) {
        return user.getBalance() + (user.getSpecialLimit() - user.getUsedSpecialLimit());
    }

    /**
     * Checks if user has sufficient balance for a transaction
     * @param user the user to check
     * @param amount the transaction amount in cents
     * @return true if user has sufficient balance
     */
    public static boolean hasSufficientBalance(User user, int amount) {
        return calculateAvailableBalance(user) >= amount;
    }

    /**
     * Calculates how much of the special limit is available
     * @param user the user to check
     * @return available special limit in cents
     */
    public static int getAvailableSpecialLimit(User user) {
        return user.getSpecialLimit() - user.getUsedSpecialLimit();
    }
}

