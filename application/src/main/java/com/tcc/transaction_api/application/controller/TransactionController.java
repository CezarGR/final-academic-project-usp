package com.tcc.transaction_api.application.controller;

import com.tcc.transaction_api.domain.dto.TransactionResponse;
import com.tcc.transaction_api.domain.service.TransactionService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for transaction history operations
 */
@RestController
@RequestMapping("/api/users/{userId}/transactions")
@RequiredArgsConstructor
@Validated
public class TransactionController {

    private static final int MAX_PAGE_SIZE = 100;

    private final TransactionService transactionService;

    /**
     * Histórico paginado. Sem {@code page}/{@code size}: primeira página com 10 transações (mais recentes primeiro).
     *
     * @param userId id do usuário
     * @param days   opcional, filtro em dias (máx. 90)
     * @param page   índice da página (0-based), padrão 0
     * @param size   itens por página, padrão 10, máx. 100
     */
    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getTransactionHistory(
            @PathVariable String userId,
            @RequestParam(required = false) Integer days,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(MAX_PAGE_SIZE) int size) {

        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TransactionResponse> result = transactionService.getTransactionHistory(userId, days, pageable);
        return ResponseEntity.ok(result);
    }
}

