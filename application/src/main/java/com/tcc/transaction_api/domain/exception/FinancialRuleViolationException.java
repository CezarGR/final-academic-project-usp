package com.tcc.transaction_api.domain.exception;

/**
 * Exception thrown when a financial rule is violated
 * (e.g., transaction would leave balance below the allowed limit)
 */
public class FinancialRuleViolationException extends RuntimeException {

    public FinancialRuleViolationException(String message) {
        super(message);
    }

    public FinancialRuleViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}

