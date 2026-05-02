package com.huynh.personal_expense_be.modules.budget.infrastructure.event;


import com.huynh.personal_expense_be.modules.budget.application.port.in.HandleTransactionUseCase;
import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionChunkCreatedEvent;
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

    private final HandleTransactionUseCase handleTransactionUseCase;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        log.info("Received event: {}", event.getClass().getSimpleName());
        handleTransactionUseCase.handleTransactionCreated(event);
    }

     @Async
     @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
     public void handleListTransactionCreated(TransactionChunkCreatedEvent event) {
         log.info("Handling TransactionChunkCreatedEvent with {} events", event.events() != null ? event.events().size() : 0);
         handleTransactionUseCase.handleListTransactionCreated(event);
     }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionUpdated(TransactionUpdatedEvent event) {
        log.info("Handling TransactionUpdatedEvent for userId={}, oldAmount={}, newAmount={}",
                event.userId(), event.oldAmount(), event.newAmount());
        handleTransactionUseCase.handleTransactionUpdated(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionDeleted(TransactionDeletedEvent event) {
        log.info("Handling TransactionDeletedEvent for userId={}, amount={}",
                event.userId(), event.amount());
        handleTransactionUseCase.handleTransactionDeleted(event);

    }
}

