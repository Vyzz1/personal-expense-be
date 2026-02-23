package com.huynh.personal_expense_be.modules.transaction.application.dto;

import com.huynh.personal_expense_be.modules.category.application.dto.CategoryResponse;
import com.huynh.personal_expense_be.modules.transaction.domain.Transaction;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse (

        String id,
        String description,
        BigDecimal amount,
        CategoryResponse category,
        Instant occurredAt,
        String type,
        Instant createdAt,
        Instant updatedAt
){
    public  static TransactionResponse from(Transaction transaction) {
                return new TransactionResponse(
                        transaction.getId().toString(),
                        transaction.getDescription(),
                        transaction.getAmount(),
                        CategoryResponse.from(transaction.getCategory()),
                        transaction.getOccurredAt(),
                        transaction.getType().toString(),
                        transaction.getCreatedAt(),
                        transaction.getUpdatedAt()
                );
        }
}
