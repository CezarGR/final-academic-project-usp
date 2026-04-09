package com.tcc.transaction_api.domain.exception;

/**
 * Exception thrown when a user is not found in the system
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

