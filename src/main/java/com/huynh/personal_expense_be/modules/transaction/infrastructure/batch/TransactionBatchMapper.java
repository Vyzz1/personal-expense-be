package com.huynh.personal_expense_be.modules.transaction.infrastructure.batch;


import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionCreatedEvent;
import com.huynh.personal_expense_be.modules.transaction.infrastructure.persistence.TransactionJpaEntity;

public class TransactionBatchMapper {

    public static TransactionCreatedEvent toTransactionCreatedEvent(TransactionJpaEntity  entity) {
        return new TransactionCreatedEvent(
                entity.getId(),
                entity.getUserId(),
                entity.getCategory() != null ? entity.getCategory().getId() : null,
                entity.getAmount(),
                entity.getOccurredAt()
        );

    }
}
