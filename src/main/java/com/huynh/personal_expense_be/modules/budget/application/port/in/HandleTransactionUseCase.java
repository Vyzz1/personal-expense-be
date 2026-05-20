package com.huynh.personal_expense_be.modules.budget.application.port.in;

import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionChunkCreatedEvent;
import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionCreatedEvent;
import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionDeletedEvent;
import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionUpdatedEvent;

public interface HandleTransactionUseCase {
    void handleTransactionCreated(TransactionCreatedEvent event);
    void handleListTransactionCreated(TransactionChunkCreatedEvent event);
    void handleTransactionUpdated(TransactionUpdatedEvent event);
    void handleTransactionDeleted(TransactionDeletedEvent event);
}
