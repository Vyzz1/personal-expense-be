package com.huynh.personal_expense_be.modules.transaction.infrastructure.batch;

import com.huynh.personal_expense_be.modules.transaction.application.dto.TransactionCsv;
import com.huynh.personal_expense_be.modules.transaction.infrastructure.persistence.TransactionJpaEntity;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.core.listener.ItemWriteListener;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionChunkListener implements ChunkListener<TransactionCsv, TransactionJpaEntity>,
        ItemWriteListener<TransactionJpaEntity> {

    private final Logger log = LoggerFactory.getLogger(TransactionChunkListener.class);

   

    @Override
    public void beforeChunk(Chunk<TransactionCsv> chunk) {
        log.info("Starting to process chunk with {} items", chunk.getItems().size());
        ChunkListener.super.beforeChunk(chunk);
    }

    @Override
    public void afterChunk(Chunk<TransactionJpaEntity> chunk) {
        log.info("Chunk committed: {} items written", chunk.getItems().size());
        ChunkListener.super.afterChunk(chunk);
    }
}
