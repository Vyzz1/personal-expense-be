package com.huynh.personal_expense_be.modules.transaction.infrastructure.batch;

import com.huynh.personal_expense_be.modules.transaction.application.dto.TransactionCsv;
import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionChunkCreatedEvent;
import com.huynh.personal_expense_be.modules.transaction.domain.event.TransactionCreatedEvent;
import com.huynh.personal_expense_be.modules.transaction.infrastructure.persistence.TransactionJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionChunkListener implements ChunkListener<TransactionCsv, TransactionJpaEntity>{

    private final ApplicationEventPublisher eventPublisher;


    @Override
    public void afterChunk(Chunk<TransactionJpaEntity>  chunk) {
        log.info("2. Finished processing chunk with {} items", chunk.getItems().size());
        log.info("3. Publishing event for chunk with {} items", chunk.getItems().size());

        List<TransactionCreatedEvent> events = chunk.getItems().stream()
                .map(TransactionBatchMapper::toTransactionCreatedEvent)
                .toList();
        TransactionChunkCreatedEvent chunkEvent = new TransactionChunkCreatedEvent(events);
        eventPublisher.publishEvent(chunkEvent);

        log.info("4. Finished publishing event for chunk with {} items", chunk.getItems().size());

        ChunkListener.super.afterChunk(chunk);
    }

    @Override
    public void beforeChunk(Chunk<TransactionCsv> chunk) {
       log.info("1. Starting to process chunk with {} items", chunk.getItems().size());
        ChunkListener.super.beforeChunk(chunk);
    }


}
