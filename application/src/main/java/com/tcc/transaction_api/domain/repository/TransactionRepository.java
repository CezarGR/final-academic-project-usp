package com.tcc.transaction_api.domain.repository;

import com.tcc.transaction_api.domain.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * Repository interface for Transaction entity
 */
@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {

    /**
     * Página de transações do usuário, mais recentes primeiro.
     */
    Page<Transaction> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    /**
     * Página de transações no intervalo de datas, mais recentes primeiro.
     */
    Page<Transaction> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        String userId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );
}

