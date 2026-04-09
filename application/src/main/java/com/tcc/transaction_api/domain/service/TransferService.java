package com.tcc.transaction_api.domain.service;

import com.tcc.transaction_api.domain.dto.TransactionResponse;
import com.tcc.transaction_api.domain.exception.InsufficientBalanceException;
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
 * Service responsible for transfer operations between users
 * Handles all validations and creates transaction records for both users
 */
@Service
@RequiredArgsConstructor
public class TransferService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Processes a transfer from one user to another
     * Creates debit transaction for origin user and credit transaction for destination user
     * @param originUserId the origin user ID
     * @param destinationUserId the destination user ID
     * @param amount the transfer amount in cents
     * @param description optional description
     * @return the debit transaction response (origin user)
     * @throws UserNotFoundException if any user is not found
     * @throws InsufficientBalanceException if origin user doesn't have sufficient balance
     */
    @Transactional
    public TransactionResponse transfer(String originUserId, String destinationUserId, int amount, String description) {
        // Validate amount
        FinancialValidator.validatePositiveAmount(amount);

        // Find origin user
        User originUser = userRepository.findById(originUserId)
            .orElseThrow(() -> new UserNotFoundException("Origin user not found with ID: " + originUserId));

        // Find destination user
        User destinationUser = userRepository.findById(destinationUserId)
            .orElseThrow(() -> new UserNotFoundException("Destination user not found with ID: " + destinationUserId));

        // Validate origin user has sufficient balance
        FinancialValidator.validateSufficientBalance(originUser, amount);

        // Validate financial rules
        FinancialValidator.validateFinancialRules(originUser, amount);

        // Generate transfer ID to group related transactions
        String transferId = UUID.randomUUID().toString();

        // Apply debit to origin user (uses balance first, then special limit)
        LimitApplier.applyDebit(originUser, amount);
        originUser.setUpdatedAt(LocalDateTime.now());
        originUser = userRepository.save(originUser);

        // Apply credit to destination user (pays back limit first, then adds to balance)
        LimitApplier.applyCredit(destinationUser, amount);
        destinationUser.setUpdatedAt(LocalDateTime.now());
        destinationUser = userRepository.save(destinationUser);

        // Create debit transaction for origin user
        Transaction debitTransaction = Transaction.builder()
            .id(UUID.randomUUID().toString())
            .userId(originUser.getId())
            .type(Transaction.TransactionType.D)
            .amount(amount)
            .description(description != null ? description : "Transfer to " + destinationUser.getName())
            .relatedUserId(destinationUser.getId())
            .transferId(transferId)
            .balanceAfter(originUser.getBalance())
            .usedSpecialLimitAfter(originUser.getUsedSpecialLimit())
            .createdAt(LocalDateTime.now())
            .build();

        // Create credit transaction for destination user
        Transaction creditTransaction = Transaction.builder()
            .id(UUID.randomUUID().toString())
            .userId(destinationUser.getId())
            .type(Transaction.TransactionType.C)
            .amount(amount)
            .description(description != null ? description : "Transfer from " + originUser.getName())
            .relatedUserId(originUser.getId())
            .transferId(transferId)
            .balanceAfter(destinationUser.getBalance())
            .usedSpecialLimitAfter(destinationUser.getUsedSpecialLimit())
            .createdAt(LocalDateTime.now())
            .build();

        // Save both transactions
        transactionRepository.save(debitTransaction);
        transactionRepository.save(creditTransaction);

        return mapToTransactionResponse(debitTransaction);
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

