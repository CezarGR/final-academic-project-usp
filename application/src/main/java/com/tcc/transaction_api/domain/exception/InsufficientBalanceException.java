package com.tcc.transaction_api.domain.exception;

/**
 * Exception thrown when a user does not have sufficient balance (including special limit)
 * to perform a transaction
 */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String message) {
        super(message);
    }

    public InsufficientBalanceException(String message, Throwable cause) {
        super(message, cause);
    }
}

