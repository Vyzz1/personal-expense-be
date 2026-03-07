package com.huynh.personal_expense_be.modules.transaction.domain.event;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionDeletedEvent(
        String userId,
        BigDecimal amount,
        Instant occurredAt
) {
}

