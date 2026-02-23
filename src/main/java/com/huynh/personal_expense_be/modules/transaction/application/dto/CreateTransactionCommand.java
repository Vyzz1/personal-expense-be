package com.huynh.personal_expense_be.modules.transaction.application.dto;

import com.huynh.personal_expense_be.modules.transaction.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CreateTransactionCommand(
        String description,
        BigDecimal amount,
        UUID categoryId,
        Instant occurredAt,
        TransactionType type,
        String userId
) {

}
