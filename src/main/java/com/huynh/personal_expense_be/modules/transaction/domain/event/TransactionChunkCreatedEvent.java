package com.huynh.personal_expense_be.modules.transaction.domain.event;

import java.time.Instant;
import java.util.List;

import com.huynh.personal_expense_be.shared.domain.DomainEvent;

public record TransactionChunkCreatedEvent (
        List<TransactionCreatedEvent> events,
        Instant occurredAt
) implements DomainEvent {
}
