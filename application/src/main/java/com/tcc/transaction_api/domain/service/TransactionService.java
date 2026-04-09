package com.tcc.transaction_api.domain.service;

import com.tcc.transaction_api.domain.dto.TransactionResponse;
import com.tcc.transaction_api.domain.exception.InvalidDaysLimitException;
import com.tcc.transaction_api.domain.exception.UserNotFoundException;
import com.tcc.transaction_api.domain.model.Transaction;
import com.tcc.transaction_api.domain.repository.TransactionRepository;
import com.tcc.transaction_api.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Service responsible for transaction history operations
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final int MAX_DAYS_LIMIT = 90;

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    /**
     * Histórico paginado; consulta limitada ao pageable (padrão típico: 10 itens na primeira página).
     *
     * @param userId   id do usuário
     * @param days     filtro opcional de dias (máx. 90); null ou ≤0 ignora o filtro de data
     * @param pageable página e tamanho (ex.: page=0, size=10)
     */
    public Page<TransactionResponse> getTransactionHistory(String userId, Integer days, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }

        if (days != null && days > MAX_DAYS_LIMIT) {
            throw new InvalidDaysLimitException(
                String.format("Days limit cannot exceed %d days. Provided: %d days", MAX_DAYS_LIMIT, days)
            );
        }

        Page<Transaction> page;
        if (days != null && days > 0) {
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(days);
            page = transactionRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                userId, startDate, endDate, pageable
            );
        } else {
            page = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        return page.map(this::mapToTransactionResponse);
    }

    /**
     * Maps Transaction entity to TransactionResponse DTO
     */
    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
            .id(transaction.getId())
            .userId(transaction.getUserId())
            .type(transaction.getType().name())
            .amount(transaction.getAmount())
            .description(transaction.getDescription())
            .relatedUserId(transaction.getRelatedUserId())
            .balanceAfter(transaction.getBalanceAfter())
            .usedSpecialLimitAfter(transaction.getUsedSpecialLimitAfter())
            .createdAt(transaction.getCreatedAt())
            .build();
    }
}

