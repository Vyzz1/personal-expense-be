package com.huynh.personal_expense_be.shared.domain;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredAt();
}
