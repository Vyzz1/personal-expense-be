package com.huynh.personal_expense_be.modules.expense.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record UpdateExpenseCommand(
        String userId,
        BigDecimal newAmount,
        BigDecimal oldAmount,
        Instant occurredAt
) {
}
