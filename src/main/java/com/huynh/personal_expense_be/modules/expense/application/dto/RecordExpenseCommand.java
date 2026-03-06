package com.huynh.personal_expense_be.modules.expense.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record RecordExpenseCommand(
        String userId,
        BigDecimal amount,
        Instant occurredAt
) {
}
