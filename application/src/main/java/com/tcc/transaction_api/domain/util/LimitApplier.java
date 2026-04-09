package com.tcc.transaction_api.domain.util;

import com.tcc.transaction_api.domain.model.User;

/**
 * Utility class for applying special limit rules.
 * Handles the logic of using and paying back special limit.
 * All monetary values are in cents.
 */
public class LimitApplier {

    /**
     * Applies a debit to the user, using balance first, then special limit if needed.
     * Updates the user's balance and usedSpecialLimit accordingly.
     * @param user the user to apply debit to
     * @param amount the debit amount in cents
     */
    public static void applyDebit(User user, int amount) {
        int remainingAmount = amount;

        // First, use available balance
        if (user.getBalance() > 0) {
            int balanceToUse = Math.min(user.getBalance(), remainingAmount);
            user.setBalance(user.getBalance() - balanceToUse);
            remainingAmount -= balanceToUse;
        }

        // If there's still amount to debit, use special limit
        if (remainingAmount > 0) {
            int availableLimit = user.getSpecialLimit() - user.getUsedSpecialLimit();
            int limitToUse = Math.min(availableLimit, remainingAmount);
            user.setUsedSpecialLimit(user.getUsedSpecialLimit() + limitToUse);
        }
    }

    /**
     * Applies a credit to the user, paying back special limit debt first, then adding to balance.
     * Updates the user's balance and usedSpecialLimit accordingly.
     * @param user the user to apply credit to
     * @param amount the credit amount in cents
     */
    public static void applyCredit(User user, int amount) {
        int remainingAmount = amount;

        // First, pay back special limit debt
        if (user.getUsedSpecialLimit() > 0) {
            int debtToPay = Math.min(user.getUsedSpecialLimit(), remainingAmount);
            user.setUsedSpecialLimit(user.getUsedSpecialLimit() - debtToPay);
            remainingAmount -= debtToPay;
        }

        // If there's still amount remaining, add to balance
        if (remainingAmount > 0) {
            user.setBalance(user.getBalance() + remainingAmount);
        }
    }

    /**
     * Calculates the new balance after applying a debit (without modifying the user)
     * @param user the user to calculate for
     * @param amount the debit amount in cents
     * @return new balance in cents
     */
    public static int calculateBalanceAfterDebit(User user, int amount) {
        int remainingAmount = amount;
        int newBalance = user.getBalance();

        // First, use available balance
        if (newBalance > 0) {
            int balanceToUse = Math.min(newBalance, remainingAmount);
            newBalance -= balanceToUse;
            remainingAmount -= balanceToUse;
        }

        return newBalance;
    }

    /**
     * Calculates the new used special limit after applying a debit (without modifying the user)
     * @param user the user to calculate for
     * @param amount the debit amount in cents
     * @return new used special limit in cents
     */
    public static int calculateUsedLimitAfterDebit(User user, int amount) {
        int remainingAmount = amount;
        int newUsedLimit = user.getUsedSpecialLimit();

        // First, use available balance
        if (user.getBalance() > 0) {
            int balanceToUse = Math.min(user.getBalance(), remainingAmount);
            remainingAmount -= balanceToUse;
        }

        // If there's still amount to debit, use special limit
        if (remainingAmount > 0) {
            int availableLimit = user.getSpecialLimit() - newUsedLimit;
            int limitToUse = Math.min(availableLimit, remainingAmount);
            newUsedLimit += limitToUse;
        }

        return newUsedLimit;
    }
}

