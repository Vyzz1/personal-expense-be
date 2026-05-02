package com.huynh.personal_expense_be.modules.budget.application.service;

import com.huynh.personal_expense_be.modules.budget.application.port.in.HandleTransactionUseCase;
import com.huynh.personal_expense_be.modules.budget.application.port.out.BudgetPersistencePort;
import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionChunkCreatedEvent;
import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionCreatedEvent;
import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionDeletedEvent;
import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class BudgetTransactionHandlerService implements HandleTransactionUseCase {

    private final BudgetPersistencePort budgetPersistencePort;

    private static final String UNCATEGORIZED_ID = "UNCATEGORIZED";

    @Override
    @Transactional
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        String period = parsePeriod(event.occurredAt());
        String categoryId = normalizeCategoryId(event.categoryId() != null ? event.categoryId().toString() : null);
        
        budgetPersistencePort.ensureBudgetExists(event.userId(), categoryId, period);
        budgetPersistencePort.incrementSpentAmount(event.userId(), categoryId, period, event.amount());
    }

    @Override
    @Transactional
    public void handleListTransactionCreated(TransactionChunkCreatedEvent event) {
        if (event.events() != null) {
            for (TransactionCreatedEvent txEvent : event.events()) {
                handleTransactionCreated(txEvent);
            }
        }
    }

    @Override
    @Transactional
    public void handleTransactionUpdated(TransactionUpdatedEvent event) {
        String period = parsePeriod(event.occurredAt());
        String categoryId = normalizeCategoryId(event.categoryId() != null ? event.categoryId().toString() : null); 
        
        BigDecimal delta = event.newAmount().subtract(event.oldAmount());
        
        budgetPersistencePort.incrementSpentAmount(event.userId(), categoryId, period, delta);
    }

    @Override
    @Transactional
    public void handleTransactionDeleted(TransactionDeletedEvent event) {
        String period = parsePeriod(event.occurredAt()); 
        String categoryId = normalizeCategoryId(event.categoryId() != null ? event.categoryId().toString() : null);
        
        // Subtract amount
        BigDecimal delta = event.amount().negate();
        budgetPersistencePort.incrementSpentAmount(event.userId(), categoryId, period, delta);
    }

    private String parsePeriod(java.time.Instant occurredAt) {
        if (occurredAt == null) {
            return YearMonth.now(ZoneId.of("UTC")).toString();
        }
        return YearMonth.from(occurredAt.atZone(ZoneId.of("UTC"))).toString();
    }

    private String normalizeCategoryId(String categoryId) {
        return (categoryId == null || categoryId.trim().isEmpty()) ? UNCATEGORIZED_ID : categoryId;
    }
}
