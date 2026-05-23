package com.huynh.personal_expense_be.modules.transaction.infrastructure.batch;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.listener.ItemWriteListener;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.stereotype.Component;

import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionChunkCreatedEvent;
import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionCreatedEvent;
import com.huynh.personal_expense_be.modules.transaction.infrastructure.persistence.TransactionJpaEntity;
import com.huynh.personal_expense_be.shared.application.port.out.OutboxRepositoryPort;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TransactionItemWriterListener implements ItemWriteListener<TransactionJpaEntity> {

    private final OutboxRepositoryPort outboxRepositoryPort;
    private final Logger log = LoggerFactory.getLogger(TransactionItemWriterListener.class);
    
     /**
     * Runs INSIDE the chunk transaction (same TX as JpaItemWriter).
     * Saves a single TransactionChunkCreatedEvent per chunk to the outbox,
     * ensuring atomicity: transactions + outbox message are committed together.
     * The OutboxWorker will later publish the event to update expense/budget.
     */
    @Override
    public void afterWrite(Chunk<? extends TransactionJpaEntity> chunk) {
        log.info("Saving outbox message for chunk with {} items (inside TX)", chunk.getItems().size());

        List<TransactionCreatedEvent> events = chunk.getItems().stream()
                .map(TransactionBatchMapper::toTransactionCreatedEvent)
                .toList();

        TransactionChunkCreatedEvent chunkEvent = new TransactionChunkCreatedEvent(events, Instant.now());
        outboxRepositoryPort.saveOutboxMessage(chunkEvent, "Transaction-Batch");

        log.info("Outbox message saved for chunk with {} items", events.size());
    }

}
