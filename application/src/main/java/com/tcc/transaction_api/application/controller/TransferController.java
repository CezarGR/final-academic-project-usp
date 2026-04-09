package com.tcc.transaction_api.application.controller;

import com.tcc.transaction_api.domain.dto.TransactionResponse;
import com.tcc.transaction_api.domain.dto.TransferRequest;
import com.tcc.transaction_api.domain.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for transfer operations between users
 */
@RestController
@RequestMapping("/api/users/{originUserId}/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    /**
     * Processes a transfer from one user to another
     * @param originUserId the origin user ID (from path)
     * @param request the transfer request containing destination user ID, amount, and optional description
     * @return the created debit transaction response (origin user)
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> transfer(
            @PathVariable String originUserId,
            @Valid @RequestBody TransferRequest request) {
        TransactionResponse response = transferService.transfer(
            originUserId,
            request.getDestinationUserId(),
            request.getAmount(),
            request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

