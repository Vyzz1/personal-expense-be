package com.huynh.personal_expense_be.modules.transaction.domain.event;

import java.math.BigDecimal;
import java.time.Instant;

import com.huynh.personal_expense_be.shared.domain.DomainEvent;

public record TransactionDeletedEvent(
        String userId,
        BigDecimal amount,
        Instant occurredAt
) implements DomainEvent {
}

