package com.tcc.transaction_api.domain.exception;

/**
 * Raised when an operation exceeds a configured time limit. Mapped to HTTP 408 Request Timeout
 * so Prometheus/Micrometer records {@code status="408"} on {@code http.server.requests}.
 */
public class OperationTimeoutException extends RuntimeException {

    public OperationTimeoutException(String message) {
        super(message);
    }

    public OperationTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
