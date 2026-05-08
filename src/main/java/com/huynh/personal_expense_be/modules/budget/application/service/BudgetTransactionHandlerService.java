package com.huynh.personal_expense_be.modules.budget.application.service;

import com.huynh.personal_expense_be.modules.budget.application.port.in.HandleTransactionUseCase;
import com.huynh.personal_expense_be.modules.budget.application.port.out.BudgetNotificationPort;
import com.huynh.personal_expense_be.modules.budget.application.port.out.BudgetPersistencePort;
import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionChunkCreatedEvent;
import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionCreatedEvent;
import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionDeletedEvent;
import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionUpdatedEvent;
import com.huynh.personal_expense_be.shared.dto.SseNotificationEventType;
import com.huynh.personal_expense_be.shared.dto.SseNotificationMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetTransactionHandlerService implements HandleTransactionUseCase {

    private final BudgetPersistencePort budgetPersistencePort;

    private final BudgetNotificationPort budgetNotificationPort;

    private static final UUID UNCATEGORIZED_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private static final Logger logger = LoggerFactory.getLogger(BudgetTransactionHandlerService.class);

    @Override
    @Transactional
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        handleTransactionCreatedInternal(event);
        sendBudgetNotification(event.userId(), "Budget updated due to new transaction of amount " + event.amount());
    }

    private void handleTransactionCreatedInternal(TransactionCreatedEvent event) {
        String period = parsePeriod(event.occurredAt());
        UUID categoryId = normalizeCategoryId(event.categoryId());

        budgetPersistencePort.ensureBudgetExists(event.userId(), categoryId, period);

        int rows = budgetPersistencePort.incrementSpentAmount(
                event.userId(), categoryId, period, event.amount()
        );

        if (rows == 0) {
            logger.info("No budget found...");
        }
    }


    @Override
    @Transactional
    public void handleListTransactionCreated(TransactionChunkCreatedEvent event) {

        logger.info("Handling TransactionChunkCreatedEvent for user {} with {} transactions", event.events().getFirst().userId(), event.events().size());

        for (TransactionCreatedEvent txEvent : event.events()) {
            handleTransactionCreatedInternal(txEvent);
        }
        sendBudgetNotification(
                event.events().getFirst().userId(),
                "Budget updated due to " + event.events().size() + " new transactions"
        );
    }

    @Override
    @Transactional
    public void handleTransactionUpdated(TransactionUpdatedEvent event) {
        String period = parsePeriod(event.occurredAt());
        UUID categoryId = normalizeCategoryId(event.categoryId() != null ? event.categoryId() : null);

        BigDecimal delta = event.newAmount().subtract(event.oldAmount());

        logger.info("Handling TransactionUpdatedEvent for user {}, category {}, period {}. Old amount: {}, New amount: {}, Delta: {}",
                event.userId(), categoryId, period, event.oldAmount(), event.newAmount(), delta);

        int rows = budgetPersistencePort.incrementSpentAmount(event.userId(), categoryId, period, delta);
        if (rows == 0) {
            logger.info("No budget found to update spent amount for user {}, category {}, period {}",
                    event.userId(), categoryId, period);
        }

        sendBudgetNotification(event.userId(), "Budget updated due to transaction update. Amount changed by " + delta);
    }

    @Override
    @Transactional
    public void handleTransactionDeleted(TransactionDeletedEvent event) {
        String period = parsePeriod(event.occurredAt());
        UUID categoryId = normalizeCategoryId(event.categoryId() != null ? event.categoryId() : null);

        // Subtract amount
        BigDecimal delta = event.amount().negate();

        logger.info("Handling TransactionDeletedEvent for user {}, category {}, period {}. Amount to subtract: {}",
                event.userId(), categoryId, period, event.amount());

        int rows = budgetPersistencePort.incrementSpentAmount(event.userId(), categoryId, period, delta);
        if (rows == 0) {
            logger.info("No budget found to delete spent amount for user {}, category {}, period {}",
                    event.userId(), categoryId, period);
        }
        sendBudgetNotification(event.userId(), "Budget updated due to transaction deletion. Amount decreased by " + event.amount());
    }

    private String parsePeriod(java.time.Instant occurredAt) {
        if (occurredAt == null) {
            return YearMonth.now(ZoneId.of("UTC")).toString();
        }
        return YearMonth.from(occurredAt.atZone(ZoneId.of("UTC"))).toString();
    }

    private UUID normalizeCategoryId(UUID categoryId) {
        return categoryId == null ? UNCATEGORIZED_ID : categoryId;
    }

    private void sendBudgetNotification(String userId, String message) {
        budgetNotificationPort.sendBudgetNotification(userId,
                SseNotificationMessage.builder()
                        .id(UUID.randomUUID().toString())
                        .eventType(SseNotificationEventType.BUDGET_UPDATED)
                        .message(message)
                        .timestamp(Instant.now())
                        .build()
        );
    }
}
