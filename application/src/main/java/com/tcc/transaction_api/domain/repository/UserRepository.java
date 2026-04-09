package com.tcc.transaction_api.domain.repository;

import com.tcc.transaction_api.domain.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for User entity
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Finds a user by document
     * @param document the document to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByDocument(String document);
}

