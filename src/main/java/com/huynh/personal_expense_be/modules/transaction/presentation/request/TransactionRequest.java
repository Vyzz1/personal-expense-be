package com.huynh.personal_expense_be.modules.transaction.presentation.request;

import com.huynh.personal_expense_be.modules.transaction.domain.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionRequest(
        @NotBlank String description,
        @Positive BigDecimal amount,
        @NotNull UUID categoryId,
        Instant occurredAt,
        TransactionType type
) {
}
