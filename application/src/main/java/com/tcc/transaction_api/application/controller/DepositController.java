package com.tcc.transaction_api.application.controller;

import com.tcc.transaction_api.domain.dto.DepositRequest;
import com.tcc.transaction_api.domain.dto.TransactionResponse;
import com.tcc.transaction_api.domain.service.DepositService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for deposit operations
 */
@RestController
@RequestMapping("/api/users/{userId}/deposits")
@RequiredArgsConstructor
public class DepositController {

    private final DepositService depositService;

    /**
     * Processes a deposit for a user
     * The deposit first pays back special limit debt, then adds to balance
     * @param userId the user ID
     * @param request the deposit request containing the amount
     * @return the created transaction response
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable String userId,
            @Valid @RequestBody DepositRequest request) {
        TransactionResponse response = depositService.deposit(userId, request.getAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

