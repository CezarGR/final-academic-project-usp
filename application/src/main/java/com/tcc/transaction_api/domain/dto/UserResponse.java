package com.tcc.transaction_api.domain.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
// import java.util.List;

/**
 * DTO for user response containing user information and last transactions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;
    private String name;
    private String document;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    /**
     * Special limit in cents
     */
    private int specialLimit;

    /**
     * Current balance in cents
     */
    private int balance;

    // /**
    //  * Last 10 transactions
    //  */
    // private List<TransactionResponse> lastTransactions;
}

