package com.huynh.personal_expense_be.shared.application.port.out;

import com.huynh.personal_expense_be.shared.domain.DomainEvent;


public interface OutboxRepositoryPort {
    void saveOutboxMessage(DomainEvent event, String module);

}
