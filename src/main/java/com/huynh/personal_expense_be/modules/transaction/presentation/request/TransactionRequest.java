package com.huynh.personal_expense_be.modules.transaction.presentation.request;

import com.huynh.personal_expense_be.modules.transaction.domain.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Request body for creating or updating a transaction")
public record TransactionRequest(
        @Schema(description = "Short description of the transaction", example = "Lunch at restaurant")
        @NotBlank String description,

        @Schema(description = "Transaction amount (must be positive)", example = "150000")
        @NotNull @Positive BigDecimal amount,

        @Schema(description = "ID of the category", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        @NotNull UUID categoryId,

        @Schema(description = "When the transaction occurred (ISO-8601). Defaults to now if omitted.", example = "2024-01-15T10:30:00Z")
        Instant occurredAt,

        @Schema(description = "Transaction type", example = "EXPENSE", allowableValues = {"INCOME", "EXPENSE"})
        TransactionType type
) {
}
