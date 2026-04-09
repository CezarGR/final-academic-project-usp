package com.tcc.transaction_api.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for error responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private String message;
    private String error;
    private int status;
    private LocalDateTime timestamp;

    public static ErrorResponse of(String message, String error, int status) {
        return ErrorResponse.builder()
            .message(message)
            .error(error)
            .status(status)
            .timestamp(LocalDateTime.now())
            .build();
    }
}

