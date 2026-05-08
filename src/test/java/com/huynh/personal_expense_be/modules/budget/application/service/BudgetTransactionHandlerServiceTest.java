package com.huynh.personal_expense_be.modules.budget.application.service;

import com.huynh.personal_expense_be.modules.budget.application.port.out.BudgetNotificationPort;
import com.huynh.personal_expense_be.modules.budget.application.port.out.BudgetPersistencePort;
import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionChunkCreatedEvent;
import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionCreatedEvent;
import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionDeletedEvent;
import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionUpdatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BudgetTransactionHandlerServiceTest {

    @Mock
    private BudgetPersistencePort budgetPersistencePort;

    @Mock
    @SuppressWarnings("unused")
    private BudgetNotificationPort budgetNotificationPort;

    @InjectMocks
    private BudgetTransactionHandlerService budgetTransactionHandlerService;

    @Test
    void handleTransactionCreated_success() {
        UUID categoryId = UUID.randomUUID();
        Instant now = Instant.now();
        String period = parsePeriod(now);
        
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                UUID.randomUUID(), "user-1", categoryId, new BigDecimal("100.0"), now);

        budgetTransactionHandlerService.handleTransactionCreated(event);

        verify(budgetPersistencePort, times(1)).ensureBudgetExists("user-1", categoryId, period);
        verify(budgetPersistencePort, times(1)).incrementSpentAmount("user-1", categoryId, period, new BigDecimal("100.0"));
    }

    @Test
    void handleTransactionCreated_nullDateAndCategory_defaultsHandled() {
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                UUID.randomUUID(), "user-1", null, new BigDecimal("100.0"), null);

        String period = parsePeriod(null);
        
        budgetTransactionHandlerService.handleTransactionCreated(event);

        UUID uncategorizedId = new UUID(0L, 0L);
        verify(budgetPersistencePort, times(1)).ensureBudgetExists("user-1", uncategorizedId, period);
        verify(budgetPersistencePort, times(1)).incrementSpentAmount("user-1", uncategorizedId, period, new BigDecimal("100.0"));
    }

    @Test
    void handleListTransactionCreated_success() {
        UUID categoryId1 = UUID.randomUUID();
        UUID categoryId2 = UUID.randomUUID();
        Instant now = Instant.now();
        String period = parsePeriod(now);
        
        TransactionCreatedEvent ev1 = new TransactionCreatedEvent(UUID.randomUUID(), "user-1", categoryId1, new BigDecimal("10.0"), now);
        TransactionCreatedEvent ev2 = new TransactionCreatedEvent(UUID.randomUUID(), "user-1", categoryId2, new BigDecimal("20.0"), now);

        TransactionChunkCreatedEvent chunkEvent = new TransactionChunkCreatedEvent(List.of(ev1, ev2));

        budgetTransactionHandlerService.handleListTransactionCreated(chunkEvent);

        verify(budgetPersistencePort, times(1)).ensureBudgetExists("user-1", categoryId1, period);
        verify(budgetPersistencePort, times(1)).incrementSpentAmount("user-1", categoryId1, period, new BigDecimal("10.0"));
        
        verify(budgetPersistencePort, times(1)).ensureBudgetExists("user-1", categoryId2, period);
        verify(budgetPersistencePort, times(1)).incrementSpentAmount("user-1", categoryId2, period, new BigDecimal("20.0"));
    }

    @Test
    void handleTransactionUpdated_success() {
        UUID categoryId = UUID.randomUUID();
        Instant now = Instant.now();
        String period = parsePeriod(now);
        
        TransactionUpdatedEvent event = new TransactionUpdatedEvent(
                "user-1", categoryId, new BigDecimal("100.0"), new BigDecimal("150.0"), now);

        budgetTransactionHandlerService.handleTransactionUpdated(event);

        // delta = 150 - 100 = 50
        verify(budgetPersistencePort, times(1)).incrementSpentAmount("user-1", categoryId, period, new BigDecimal("50.0"));
    }

    @Test
    void handleTransactionDeleted_success() {
        UUID categoryId = UUID.randomUUID();
        Instant now = Instant.now();
        String period = parsePeriod(now);
        
        TransactionDeletedEvent event = new TransactionDeletedEvent(
                "user-1", categoryId, new BigDecimal("100.0"), now);

        budgetTransactionHandlerService.handleTransactionDeleted(event);

        // delta = -100
        verify(budgetPersistencePort, times(1)).incrementSpentAmount("user-1", categoryId, period, new BigDecimal("-100.0"));
    }

    private String parsePeriod(Instant occurredAt) {
        if (occurredAt == null) {
            return java.time.YearMonth.now(ZoneId.of("UTC")).toString();
        }
        return java.time.YearMonth.from(occurredAt.atZone(ZoneId.of("UTC"))).toString();
    }
}
