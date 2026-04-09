package com.tcc.transaction_api.domain.exception;

/**
 * Exception thrown when the days limit for transaction history query exceeds the maximum allowed (90 days)
 */
public class InvalidDaysLimitException extends RuntimeException {

    public InvalidDaysLimitException(String message) {
        super(message);
    }

    public InvalidDaysLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}

