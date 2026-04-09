package com.tcc.transaction_api.domain.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tcc.transaction_api.domain.dto.UserCreateRequest;
import com.tcc.transaction_api.domain.dto.UserResponse;
import com.tcc.transaction_api.domain.exception.UserNotFoundException;
import com.tcc.transaction_api.domain.model.Transaction;
import com.tcc.transaction_api.domain.model.User;
import com.tcc.transaction_api.domain.repository.TransactionRepository;
import com.tcc.transaction_api.domain.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service responsible for user-related operations
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private static final int DEFAULT_SPECIAL_LIMIT = 1000; // in cents
    private static final int LAST_TRANSACTIONS_LIMIT = 10;

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Creates a new user with default special limit
     * @param request the user creation request
     * @return the created user response
     */
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        User user = User.builder()
            .name(request.getName())
            .document(request.getDocument())
            .birthDate(request.getBirthDate())
            .balance(0)
            .specialLimit(DEFAULT_SPECIAL_LIMIT)
            .usedSpecialLimit(0)
            .createdAt(LocalDateTime.now())
            .build();

        user = userRepository.save(user);
        return mapToUserResponse(user);
    }

    /**
     * Finds a user by ID and returns user information with last 10 transactions
     * @param userId the user ID
     * @return the user response
     * @throws UserNotFoundException if user is not found
     */
    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // var pageable = PageRequest.of(
        //     0,
        //     LAST_TRANSACTIONS_LIMIT,
        //     Sort.by(Sort.Direction.DESC, "createdAt")
        // );
        // List<Transaction> lastTransactions = transactionRepository
        //     .findByUserIdOrderByCreatedAtDesc(userId, pageable)
        //     .getContent();

        UserResponse response = mapToUserResponse(user);
        // response.setLastTransactions(
        //     lastTransactions.stream()
        //         .map(this::mapToTransactionResponse)
        //         .collect(Collectors.toList())
        // );

        return response;
    }

    /**
     * Maps User entity to UserResponse DTO
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .document(user.getDocument())
            .birthDate(user.getBirthDate())
            .specialLimit(user.getSpecialLimit())
            .balance(user.getBalance())
            .build();
    }

    /**
     * Maps Transaction entity to TransactionResponse DTO
     */
    private com.tcc.transaction_api.domain.dto.TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return com.tcc.transaction_api.domain.dto.TransactionResponse.builder()
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

    /**
     * Finds a user by ID (internal use)
     * @param userId the user ID
     * @return the user entity
     * @throws UserNotFoundException if user is not found
     */
    public User findUserById(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }
}

