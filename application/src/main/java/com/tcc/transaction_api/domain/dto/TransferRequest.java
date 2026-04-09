package com.tcc.transaction_api.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for transfer request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    @NotBlank(message = "Destination user ID is required")
    private String destinationUserId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Integer amount; // in cents

    private String description; // optional
}

