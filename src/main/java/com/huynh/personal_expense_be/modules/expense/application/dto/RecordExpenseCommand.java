package com.huynh.personal_expense_be.modules.expense.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RecordExpenseCommand(
        String userId,
        BigDecimal amount,
        UUID categoryId,
        UUID transactionId,
        Instant occurredAt
) {
}
