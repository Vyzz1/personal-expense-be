package com.huynh.personal_expense_be.modules.transaction.domain.event;

import com.huynh.personal_expense_be.shared.domain.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionUpdatedEvent (
        String userId,
        BigDecimal oldAmount,
        BigDecimal newAmount,
        Instant occurredAt
) implements DomainEvent {
}

