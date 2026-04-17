package com.huynh.personal_expense_be.modules.transaction.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.huynh.personal_expense_be.shared.domain.DomainEvent;

public record TransactionCreatedEvent(
        UUID transactionId,
        String userId,
        UUID categoryId,
        BigDecimal amount,
        Instant occurredAt
) implements DomainEvent {
}
