package com.tcc.transaction_api.application.exception;

import com.tcc.transaction_api.domain.dto.ErrorResponse;
import com.tcc.transaction_api.domain.exception.FinancialRuleViolationException;
import com.tcc.transaction_api.domain.exception.InsufficientBalanceException;
import com.tcc.transaction_api.domain.exception.InvalidDaysLimitException;
import com.tcc.transaction_api.domain.exception.OperationTimeoutException;
import com.tcc.transaction_api.domain.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for standardized error responses
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles UserNotFoundException
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        ErrorResponse error = ErrorResponse.of(
            ex.getMessage(),
            "USER_NOT_FOUND",
            HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handles InsufficientBalanceException
     */
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalanceException(InsufficientBalanceException ex) {
        ErrorResponse error = ErrorResponse.of(
            ex.getMessage(),
            "INSUFFICIENT_BALANCE",
            HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles InvalidDaysLimitException
     */
    @ExceptionHandler(InvalidDaysLimitException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDaysLimitException(InvalidDaysLimitException ex) {
        ErrorResponse error = ErrorResponse.of(
            ex.getMessage(),
            "INVALID_DAYS_LIMIT",
            HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles FinancialRuleViolationException
     */
    @ExceptionHandler(FinancialRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleFinancialRuleViolationException(FinancialRuleViolationException ex) {
        ErrorResponse error = ErrorResponse.of(
            ex.getMessage(),
            "FINANCIAL_RULE_VIOLATION",
            HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String message = "Validation failed: " + errors.toString();
        ErrorResponse error = ErrorResponse.of(
            message,
            "VALIDATION_ERROR",
            HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles OperationTimeoutException (HTTP 408 — visível no Grafana como status 408)
     */
    @ExceptionHandler(OperationTimeoutException.class)
    public ResponseEntity<ErrorResponse> handleOperationTimeoutException(OperationTimeoutException ex) {
        ErrorResponse error = ErrorResponse.of(
            ex.getMessage(),
            "REQUEST_TIMEOUT",
            HttpStatus.REQUEST_TIMEOUT.value()
        );
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(error);
    }

    /**
     * Handles IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse error = ErrorResponse.of(
            ex.getMessage(),
            "INVALID_ARGUMENT",
            HttpStatus.BAD_REQUEST.value()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handles all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = ErrorResponse.of(
            "An unexpected error occurred: " + ex.getMessage(),
            "INTERNAL_SERVER_ERROR",
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

