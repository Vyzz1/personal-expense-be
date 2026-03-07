package com.huynh.personal_expense_be.modules.expense.infrastructure.event;

import com.huynh.personal_expense_be.modules.expense.application.dto.DeductExpenseCommand;
import com.huynh.personal_expense_be.modules.expense.application.dto.RecordExpenseCommand;
import com.huynh.personal_expense_be.modules.expense.application.dto.UpdateExpenseCommand;
import com.huynh.personal_expense_be.modules.expense.application.port.in.DeductMonthlyExpenseUseCase;
import com.huynh.personal_expense_be.modules.expense.application.port.in.RecordMonthlyExpenseUseCase;
import com.huynh.personal_expense_be.modules.expense.application.port.in.UpdateMonthlyExpenseUseCase;
import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionCreatedEvent;
import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionDeletedEvent;
import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventListener {

    private final RecordMonthlyExpenseUseCase recordMonthlyExpenseUseCase;
    private final DeductMonthlyExpenseUseCase deductMonthlyExpenseUseCase;
    private final UpdateMonthlyExpenseUseCase updateMonthlyExpenseUseCase;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        log.info("Handling TransactionCreatedEvent for transactionId={}, userId={}",
                event.transactionId(), event.userId());

        recordMonthlyExpenseUseCase.recordMonthlyExpense(
                new RecordExpenseCommand(event.userId(), event.amount(), event.occurredAt())
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionUpdated(TransactionUpdatedEvent event) {
        log.info("Handling TransactionUpdatedEvent for userId={}, oldAmount={}, newAmount={}",
                event.userId(), event.oldAmount(), event.newAmount());

        updateMonthlyExpenseUseCase.updateMonthlyExpense(
                new UpdateExpenseCommand(event.userId(), event.newAmount(), event.oldAmount(), event.occurredAt())
        );
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionDeleted(TransactionDeletedEvent event) {
        log.info("Handling TransactionDeletedEvent for userId={}, amount={}",
                event.userId(), event.amount());

        deductMonthlyExpenseUseCase.deductExpense(
                new DeductExpenseCommand(event.userId(), event.amount(), event.occurredAt())
        );
    }
}
