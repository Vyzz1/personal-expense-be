package com.huynh.personal_expense_be.modules.expense.infrastructure.event;

import com.huynh.personal_expense_be.modules.expense.application.dto.RecordExpenseCommand;
import com.huynh.personal_expense_be.modules.expense.application.port.in.RecordMonthlyExpenseUseCase;
import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionCreatedEventListener {

    private final RecordMonthlyExpenseUseCase recordMonthlyExpenseUseCase;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        log.info("Handling TransactionCreatedEvent for transactionId={}, userId={}",
                event.transactionId(), event.userId());

        RecordExpenseCommand command = new RecordExpenseCommand(
                event.userId(),
                event.amount(),
                event.occurredAt()
        );

        recordMonthlyExpenseUseCase.recordMonthlyExpense(command);
    }
}

