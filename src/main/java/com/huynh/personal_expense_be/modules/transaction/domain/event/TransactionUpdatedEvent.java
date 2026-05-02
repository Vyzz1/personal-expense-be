package com.huynh.personal_expense_be.modules.transaction.domain.event;

import com.huynh.personal_expense_be.shared.domain.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionUpdatedEvent (
        String userId,
        UUID categoryId,
        BigDecimal oldAmount,
        BigDecimal newAmount,
        Instant occurredAt
) implements DomainEvent {
}

