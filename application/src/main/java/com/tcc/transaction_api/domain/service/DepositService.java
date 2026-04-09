package com.tcc.transaction_api.domain.service;

import com.tcc.transaction_api.domain.dto.TransactionResponse;
import com.tcc.transaction_api.domain.exception.UserNotFoundException;
import com.tcc.transaction_api.domain.model.Transaction;
import com.tcc.transaction_api.domain.model.User;
import com.tcc.transaction_api.domain.repository.TransactionRepository;
import com.tcc.transaction_api.domain.repository.UserRepository;
import com.tcc.transaction_api.domain.util.FinancialValidator;
import com.tcc.transaction_api.domain.util.LimitApplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service responsible for deposit operations
 * Handles the logic of paying back special limit debt before adding to balance
 */
@Service
@RequiredArgsConstructor
public class DepositService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Processes a deposit for a user
     * The deposit first pays back special limit debt, then adds to balance
     * @param userId the user ID
     * @param amount the deposit amount in cents
     * @return the created transaction response
     * @throws UserNotFoundException if user is not found
     */
    @Transactional
    public TransactionResponse deposit(String userId, int amount) {
        // Validate amount
        FinancialValidator.validatePositiveAmount(amount);

        // Find user
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // Apply credit (pays back limit first, then adds to balance)
        LimitApplier.applyCredit(user, amount);

        // Update user timestamp
        user.setUpdatedAt(LocalDateTime.now());

        // Save user
        user = userRepository.save(user);

        // Create transaction record
        Transaction transaction = Transaction.builder()
            .id(UUID.randomUUID().toString())
            .userId(user.getId())
            .type(Transaction.TransactionType.C)
            .amount(amount)
            .description("Deposit")
            .balanceAfter(user.getBalance())
            .usedSpecialLimitAfter(user.getUsedSpecialLimit())
            .createdAt(LocalDateTime.now())
            .build();

        transaction = transactionRepository.save(transaction);

        return mapToTransactionResponse(transaction);
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

